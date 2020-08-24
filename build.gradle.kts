import org.jetbrains.kotlin.gradle.tasks.*

//buildscript {
/*
 * These property group is used to build ktor against Kotlin compiler snapshot.
 * How does it work:
 * When build_snapshot_train is set to true, kotlin_version property is overridden with kotlin_snapshot_version,
 * atomicfu_version, coroutines_version, serialization_version and kotlinx_io_version are overwritten by TeamCity environment.
 * Additionally, mavenLocal and Sonatype snapshots are added to repository list and stress tests are disabled.
 * DO NOT change the name of these properties without adapting kotlinx.train build chain.
 */
//    val prop = rootProject.properties["build_snapshot_train"]
//    val build_snapshot_train: Boolean = prop != null && prop != ""
//    ext.build_snapshot_train = prop != null && prop != ""
//    if (build_snapshot_train) {
//        val kotlin_version = rootProject.properties["kotlin_snapshot_version"]
//        extra["kotlin_version"] = kotlin_version
//        if (kotlin_version == null) {
//            throw IllegalArgumentException("'kotlin_snapshot_version' should be defined when building with snapshot compiler")
//        }
//        repositories {
//            mavenLocal()
//            maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
//        }
//
//        configurations.classpath {
//            resolutionStrategy.eachDependency {
//                if (requested.group == "org.jetbrains.kotlin") {
////                    details.useVersion kotlin_version
//                }
//            }
//        }
//    }

//    repositories {
//        mavenLocal()
//        jcenter()
//        google()
//        gradlePluginPortal()
//        maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
//        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
//        maven(url = "https://kotlin.bintray.com/kotlin-dev")
//        maven {
//            url = "https://kotlin.bintray.com/kotlin-dev"
//            credentials {
//                username =
//                    if (project.hasProperty("bintrayUser")) {
//                        project.property("bintrayUser")
//                    } else {
//                        System.getenv("BINTRAY_USER") ?: ""
//                    }
//                password =
//                    if (project.hasProperty("bintrayApiKey")) project.property("bintrayApiKey") else System.getenv("BINTRAY_API_KEY")
//                        ?: ""
//            }
//        }
//        maven(url = "https://dl.bintray.com/orangy/maven")
//    }

//    dependencies {
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
//        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")
//        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokka_version")
//        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfu_version")
//        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
//        classpath("com.android.tools.build:gradle:$android_gradle_version")
//    }
//}

//ext.configuredVersion = project.hasProperty("releaseVersion") ? project.releaseVersion : project.version
//ext.globalM2 = "$buildDir/m2"
//project.extra["publishLocal"] = project.hasProperty("publishLocal")

//apply from : 'gradle/verifier.gradle'

/**
 * `darwin` is subset of `posix`.
 * Don't create `posix` and `darwin` sourceSets in single project.
 */
//ext.skipPublish = ["ktor-server-benchmarks", "ktor-client-benchmarks"]
//ext.nonDefaultProjectStructure = ["ktor-bom"]

//def check (Object version, String libVersion, String libName) {
//    if (version != libVersion) {
//        throw new IllegalStateException ("Current deploy version is $version, but $libName version is not overridden ($libVersion)")
//    }
//}

//apply from : 'gradle/compatibility.gradle'

plugins {
    kotlin("plugin.serialization") version kotlin_version apply false
    id("binary-compatibility-validator")
}

allprojects {
    group = "io.ktor"
//    version = configuredVersion
//    project.ext.hostManager = new HostManager ()

//    if (buildSnapshotTrain) {
//        ext.kotlin_version =


//        println "Using Kotlin $kotlin_version for project $it"
//        def deployVersion = properties ['DeployVersion']
//        if (deployVersion != null) version = deployVersion
//
//        def skipSnapshotChecks = rootProject . properties ['skip_snapshot_checks'] != null
//        if (!skipSnapshotChecks) {
//            check(version, atomicfu_version, "atomicfu")
//            check(version, coroutines_version, "coroutines")
//            check(version, serialization_version, "serialization")
//        }
//        kotlin_version = rootProject.properties['kotlin_snapshot_version']
//    }

    repositories {
        mavenLocal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")

        maven(url = "https://dl.bintray.com/kotlin/kotlinx/") {
            credentials {
                username = bintrayUser
                password = bintrayPassword
            }
        }

        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlin-dev") {
            credentials {
                username = bintrayUser
                password = bintrayPassword
            }
        }

        mavenCentral()
        jcenter()
    }

    apply {
        plugin("org.jetbrains.kotlin.multiplatform")
    }

    kotlin {
        jvm()
        js {
            nodejs {
                testTask {
                    useMocha {
                        timeout = "10000"
                    }
                    debug = false
                }
            }

            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(File(project.rootProject.projectDir, "karma"))
                    }
                }
            }

            val main by compilations.getting
            main.kotlinOptions {
                metaInfo = true
                sourceMap = true
                moduleKind = "umd"
                this.main = "noCall"
                sourceMapEmbedSources = "always"
            }

            val test by compilations.getting
            test.kotlinOptions {
                metaInfo = true
                sourceMap = true
                moduleKind = "umd"
                this.main = "call"
                sourceMapEmbedSources = "always"
            }
        }

        val darwinTargets = listOf(
            iosArm64(),
            iosArm32(),
            iosX64(),
            macosX64(),
            tvosArm64(),
            tvosX64(),
            watchosArm32(),
            watchosArm64(),
            watchosX86()
        )

        val posixTargets = listOf(
            linuxX64(),
            mingwX64(),
            macosX64()
        )

        compositeTarget("darwin", darwinTargets)
        compositeTarget("posix", posixTargets)

        explicitApiWarning()

        sourceSets {
            val commonMain by getting {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                }
            }

            all {
                val srcDir = if (name.endsWith("Main")) "src" else "test"
                val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
                val platform = name.dropLast(4)

                kotlin.srcDirs("$platform/$srcDir")
                resources.srcDirs("$platform/${resourcesPrefix}resources")

                languageSettings.apply {
                    progressiveMode = true

                    EXPERIMENTAL_ANNOTATIONS.forEach { useExperimentalAnnotation(it) }

                    if (project.path.startsWith(":ktor-server:ktor-server") && project.name != "ktor-server-core") {
                        useExperimentalAnnotation("io.ktor.server.engine.EngineAPI")
                    }
                }
            }
        }

        configurations.create("testOutput")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xinline-classes"
    }
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")
apply<Train>()

//if (project.hasProperty("enable-coverage")) {
//    apply from : "gradle/jacoco.gradle"
//}
