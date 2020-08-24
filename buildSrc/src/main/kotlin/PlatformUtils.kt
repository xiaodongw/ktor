import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.delegates.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

val kotlin_version = "1.4.0"
val dokka_version = "0.9.17"
val jmh_plugin_version = "0.4.5"
val benchmarks_version = "0.2.0-dev-17"
val validator_version = "0.2.2"
val atomicfu_version = "0.14.4"
val android_gradle_version = "3.5.3"
val typesafe_config_version = "1.3.1"
val jetty_version = "9.4.31.v20200723"
val jetty_alpn_api_version = ""
val netty_version = "4.1.44.Final"
val netty_tcnative_version = "2.0.27.Final"
val javax_servlet_api = "4.0.0-b07"
val logback_version = "1.2.3"
val junit_version = "4.12"
val coroutines_version = "1.3.8-native-mt-1.4.0-rc"
val tomcat_version = "9.0.29"
val json_simple_version = "1.1.1"
val java_jwt_version = "3.9.0"
val jwks_rsa_version = "0.9.0"
val apacheds_version = "2.0.0-M24"
val gson_version = "2.8.6"
val kotlinx_html_version = "0.7.2"
val jackson_version = "2.10.2"
val jackson_kotlin_version = "2.10.2"
val serialization_version = "1.0.0-RC"

public val EXPERIMENTAL_ANNOTATIONS: List<String> = listOf(
    "kotlin.RequiresOptIn",
    "kotlin.ExperimentalUnsignedTypes",
    "io.ktor.util.KtorExperimentalAPI",
    "io.ktor.util.InternalAPI",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "io.ktor.utils.io.core.ExperimentalIoApi",
    "io.ktor.utils.io.core.internal.DangerousInternalIoApi",
    "kotlin.contracts.ExperimentalContracts"
)

val KotlinTarget.mainSources: List<KotlinSourceSet>
    get() = compilations
        .flatMap { it.kotlinSourceSets }
        .filter { it.name.endsWith("Main") }

val KotlinTarget.testSources: List<KotlinSourceSet>
    get() = compilations
        .flatMap { it.kotlinSourceSets }
        .filter { it.name.endsWith("Test") }


val List<KotlinTarget>.mainSources: List<KotlinSourceSet> get() = flatMap { it.mainSources }
val List<KotlinTarget>.testSources: List<KotlinSourceSet> get() = flatMap { it.testSources }

fun KotlinMultiplatformExtension.compositeTarget(name: String, childs: List<KotlinTarget>) {
    sourceSets {
        val main = create("${name}Main")
        val test = create("${name}Test")

        childs.mainSources.forEach { it.dependsOn(main) }
        childs.testSources.forEach { it.dependsOn(test) }
    }
}

val Project.bintrayUser: String?
    get() = findProject("bintrayUser") as? String ?: System.getenv("BINTRAY_USER") ?: ""

val Project.bintrayPassword: String?
    get() = findProject("bintrayApiKey") as? String ?: System.getenv("BINTRAY_API_KEY") ?: ""

val ProjectDelegate.buildSnapshotTrain: Boolean
    get() {
        val train = rootProject.properties["build_snapshot_train"]
        return train != null && train != ""
    }
