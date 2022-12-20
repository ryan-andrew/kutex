# Kutex
[![codecov](https://codecov.io/gh/ryan-andrew/kutex/branch/main/graph/badge.svg?token=GBP3D28FE6)](https://codecov.io/gh/ryan-andrew/kutex)
### A Kotlin Multiplatform object wrapper that provides safe access between coroutines

Normally, we have to keep a separate locking object in order to ensure safe
access to the objects we want to protect. This is clumsy and can lead to errors.
- You have to ensure you **always** wrap every call with the locking mechanism
- You can forget to lock the object, leading to strange, hard to track down issues
- Someone else could come along and unsafely access something

**_Ideally, it should be difficult or impossible to access an object that should
be kept behind a lock without using said lock. That's the purpose of Kutex._**

#### Normal, unsafe object locking:
```kotlin
object UnsafeExample {
    val listMutex = Mutex()
    val myList = mutableListOf("some", "strings")

    suspend fun addStringToListFromRandomCoroutine(string: String) {
        listMutex.withLock {
            myList.add(string)
        }
    }

    suspend fun performCalculationsOnList() {
        listMutex.withLock {
            myList.forEach {
                delay(1000)
            }
        }
    }

    fun evil() {
        myList.add("hope you aren't iterating!")
    }
}
```
#### With Kutex:
With Kutex, you can't accidentally do anything unsafe

```kotlin
object SafeExample {
    val myListKutex = kutexOf(mutableListOf("some", "strings"))

    suspend fun addStringToListFromRandomCoroutine(string: String) {
        myListKutex.withLock {
            value.add(string)
        }
    }

    suspend fun performCalculationsOnList() {
        myListKutex.withLock {
            value.forEach {
                delay(1000)
            }
        }
    }

    suspend fun evil() {
        myListKutex.withLock { 
            value.add("I cannot access the underlying object in an unsafe way :'(")
        }
    }
}
```

## Examples

### Immutable

```kotlin
val myListKutex = kutexOf(mutableListOf("some", "strings"))

suspend fun lockExamples() {
    // Can return from the scope
    myListKutex.withLock {
        if ("someString" !in value) return
    }

    // Can assign vals outside the scope from within the scope
    val someString: String
    myListKutex.withLock {
        someString = value.first()
    }
    println(someString)

    // Can get from the Kutex's value
    val firstItem = myListKutex.withLock { value.firstOrNull() }

    // Can get information about the Kutex's value
    val isEmpty = myListKutex.withLock { value.isEmpty() }
}

fun tryLockExamples() {
    myListKutex.tryWithLock {
        if ("someString" !in value) return
    }.onAlreadyLocked {
        println("myListKutex was locked when we attempted to acquire lock")
    }

    val first = myListKutex.tryWithLock { value.first() }.getOrNull()
    if (first == null) {
        println("myListKutex was locked when we attempted to acquire lock")
    } else {
        println("First item in myListKutex is $first!")
    }

    val thing = myListKutex.tryWithLock { value.first() }
        .getOrElse { "some default string for when already locked" }
    println(thing)
}
```

### Mutable
```kotlin
val countKutex = mutableKutexOf(100)

suspend fun test() {
    countKutex.withLock {
        if (value < 100) return
    }

    val count: Int
    countKutex.withLock {
        count = value
    }
    println(count)

    val currCount = countKutex.withLock { value }

    val isAtLeast100 = countKutex.withLock { value >= 100 }

    // increment count safely
    countKutex.withLock { value++ }

    // reset count to zero
    countKutex.withLock { value = 0 }
}

fun tryLockExamples() {
    countKutex.tryWithLock {
        if (value < 100) return
    }.onAlreadyLocked {
        println("myListKutex was locked when we attempted to acquire lock")
    }

    val currCount = countKutex.tryWithLock { value }.getOrNull()
    if (currCount == null) {
        println("countKutex was locked when we attempted to acquire lock")
    } else {
        println("currCount is $currCount!")
    }

    val thing = countKutex.tryWithLock { value }.getOrElse { -1 }
    println(thing)

    // Attempt to increment, doesn't matter if we can't
    countKutex.tryWithLock {
        value++
    }

    // Attempt to set value
    countKutex.tryWithLock {
        value = 0
    }.onAlreadyLocked {
        println("We couldn't acquire lock!")
    }
}
```

More examples and usages can be seen in the [unit tests](src/commonTest/kotlin/dev.ryanandrew.kutex) and documentation.

## FAQs

### Uh... why is each extension function in its own file?
This is done primarily so that consumers of the library have better unit tests. 
Testing frameworks like [Mockk](https://github.com/mockk/mockk) support mocking extension functions, but they
are awkward due to the way they are handled by Kotlin on the backend. For each
file with top-level extensions, there secretly exists a class called `<filename>Kt`,
and the extension functions and properties all exist as static methods within this class.
In Mockk's case, mocking a static method mocks the class to which it belongs. 
This means that [**all** extensions in a file will be mocked if any of them are](https://mockk.io/#extension-functions). 
This can lead to good-looking tests with invalid results. For this reason, I like to 
put extension methods in their own files. Feel free to mock them individually with 
no ill-effects :)

### I've cloned the project and the IDE says there are errors!

There aren't, really, and they won't stop you from building. It's a bug with how the checker is reading the `OptIn`
annotation, unfortunately. The bug is tracked [here](https://youtrack.jetbrains.com/issue/KTIJ-20071),
[here](https://youtrack.jetbrains.com/issue/KTIJ-22253), and on a few other older tickets, but it looks like they're 
very low priority. This is the same way that [Kotlin's standard library uses the 
annotation](https://github.com/JetBrains/kotlin/blob/v1.7.22/libraries/stdlib/src/kotlin/collections/Maps.kt#L8). I will
keep it this way (i.e., top-level declaration), since there's no need to expose that annotation to consumers of the library.
