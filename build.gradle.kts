plugins {
    kotlin("multiplatform") version "1.7.21"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("org.jetbrains.dokka") version "1.7.20"
    id("maven-publish")
}

group = "dev.ryanandrew"
version = "1.0.13"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }

    }
    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

kover {
    xmlReport {
        onCheck.set(true)
    }
    filters {
        classes {
//            excludes += "**Test**"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ryan-andrew/kutex")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

// Dokka versioning

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:versioning-plugin:1.7.20")
    }
}

dependencies {
    dokkaPlugin("org.jetbrains.dokka:versioning-plugin:1.7.20")
}

val dokkaDir = buildDir.resolve("dokka").resolve("html")
val tmpDocDir = projectDir.resolve("docs_tmp")

tasks.register("printVersion"){
    println(version)
}

tasks.register("copyFromDocsToTmp"){
    println("!!!!!!!!!!!!!!!!!!!")
    if (dokkaDir.exists()) {
        println("dokka dir existed")
        dokkaDir.listFiles()?.firstOrNull()?.let {
            println("First dokka folder: ${it.path}")
            copy {
                from(it.parent)
                into(tmpDocDir)
            }
            println("${it.path} copied to ${tmpDocDir.path}")

            val old = it.resolve("older")
            if (old.exists()) {
                println("older exists")
                old.listFiles()?.forEach {
                    copy {
                        from(it.parent)
                        into(tmpDocDir)
                    }
                }
            }
        }
    } else {
        println("${dokkaDir.path} did not exist!")
    }
}

tasks.dokkaHtml.configure {
    val projectVersion = project.version.toString()
    // TODO still loses version before last AND loses all old versions if curr version is same as last during build
//    val projectVersion = project.version.toString()
//    val old = buildDir.resolve("dokka-old")
//    dokkaDir.listFiles()?.firstOrNull()?.apply {
//        copy {
//            from(path)
//            into(old.resolve(name))
//        }
//        resolve("older").listFiles()?.forEach {
//            copy {
//                from(it.path)
//                into(old.resolve(it.name))
//            }
//        }
//    }//
    tmpDocDir.listFiles()?.forEach { println("~~~~~~~${it.name}") }
    tmpDocDir.listFiles()?.filter { it.name == projectVersion }?.forEach { delete(it) }
    tmpDocDir.listFiles()?.forEach { println("~~~~~~~${it.name}") }
    pluginConfiguration<org.jetbrains.dokka.versioning.VersioningPlugin, org.jetbrains.dokka.versioning.VersioningConfiguration> {
        version = projectVersion
        olderVersionsDir = tmpDocDir
        renderVersionsNavigationOnAllPages = true
    }
    outputDirectory.set(dokkaDir.resolve(projectVersion))
}