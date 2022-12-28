plugins {
    kotlin("multiplatform") version "1.7.21"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
    signing
}

group = "dev.ryanandrew"
version = "0.0.2"

repositories {
    mavenCentral()
    gradlePluginPortal()
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

// Documentation

kover {
    xmlReport {
        onCheck.set(true)
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
    val versionFile = dokkaDir.resolve("version.json")
    if (dokkaDir.exists() && versionFile.exists()) {
        val prevVersion = (groovy.json.JsonSlurper().parse(versionFile) as Map<*, *>)["version"]
        val prevDir = tmpDocDir.resolve(prevVersion.toString())

        copy {
            from(dokkaDir)
            into(prevDir)
        }
        println("${dokkaDir.path} copied to ${tmpDocDir.path}")

        val old = dokkaDir.resolve("older")
        if (old.exists()) {
            println("older exists")
            copy {
                from(old)
                into(tmpDocDir)
            }
        }
    } else {
        println("${dokkaDir.path} did not exist!")
    }
}

tasks.dokkaHtml.configure {
    val projectVersion = project.version.toString()
    tmpDocDir.listFiles()?.filter { it.name == projectVersion }?.forEach { delete(it) }
    pluginConfiguration<org.jetbrains.dokka.versioning.VersioningPlugin, org.jetbrains.dokka.versioning.VersioningConfiguration> {
        version = projectVersion
        olderVersionsDir = tmpDocDir
        renderVersionsNavigationOnAllPages = true
    }
    outputDirectory.set(dokkaDir)
}

// Publishing
// Stub secrets to let the project sync and build without the publication values set up

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = try { ext[name]?.toString() } catch (t: Throwable) { null }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ryan-andrew/kutex")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("Kutex")
            description.set("A Kotlin Multiplatform object wrapper that provides safe access between coroutines")
            url.set("https://github.com/reyan-andrew/kutex")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("ryan-andrew")
                    name.set("Ryan")
                    email.set("ryan@ryanandrew.dev")
                }
            }
            scm {
                url.set("https://github.com/reyan-andrew/kutex")
            }
        }
    }
}

// Signing artifacts. Signing.* extra properties values will be used
signing {
    sign(publishing.publications)
}
