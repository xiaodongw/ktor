import org.gradle.api.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*

/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

class Train : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.applyPlugin()
    }

    fun Project.applyPlugin() {
        allprojects {
            setProperty("kotlin_version", "")
        }

        println("Hacking test tasks, removing stress and flaky tests")
        disableFlakyTests()


        println("Manifest of kotlin-compiler-embeddable.jar")
//        printManifest()
    }
}

fun Project.disableFlakyTests() {
    allprojects {
        tasks.withType<Test> {
            exclude("**/*ServerSocketTest*")
            exclude("**/*NettyStressTest*")
            exclude("**/*CIOMultithreadedTest*")
            exclude("**/*testBlockingConcurrency*")
            exclude("**/*testBigFile*")
            exclude("**/*numberTest*")
            exclude("**/*testWithPause*")
            exclude("**/*WebSocketTest*")
            exclude("**/*PostTest*")
            exclude("**/*testCustomUrls*")
            exclude("**/*testStaticServeFromDir*")
            exclude("**/*testRedirect*")
            exclude("**/*CIOHttpsTest*")
        }
    }
}

fun Project.printManifest() {
    val clientProject = subprojects.find { it.name == "ktor-client" } ?: error("Fail to find project")
    val kotlinCompilerClasspath by clientProject.configurations
    val files = kotlinCompilerClasspath.resolvedConfiguration.files

    files.filter { it.name.contains("kotlin-compiler-embeddable") }.forEach {
        val manifest = zipTree(it).matching {
            include("META-INF/MANIFEST.MF")
        }.files.first()

        manifest.readLines().forEach {
            println(it)
        }
    }
}

