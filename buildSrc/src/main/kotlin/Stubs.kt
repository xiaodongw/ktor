import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

public val Project.kotlin: KotlinMultiplatformExtension get() = the()

public fun Project.kotlin(configure: Action<KotlinMultiplatformExtension>) {
    configure.execute(kotlin)
}

public fun NamedDomainObjectContainerScope<KotlinSourceSet>.jvmMain(block: KotlinSourceSet.() -> Unit) {
    val jvmMain by getting(block)
}

public fun NamedDomainObjectContainerScope<KotlinSourceSet>.jvmTest(block: KotlinSourceSet.() -> Unit) {
    val jvmTest by getting(block)
}

public val NamedDomainObjectContainerScope<KotlinSourceSet>.jvmMain: KotlinSourceSet
    get() {
        val jvmMain by getting
        return jvmMain
    }

public val NamedDomainObjectContainerScope<KotlinSourceSet>.jvmTest: KotlinSourceSet
    get() {
        val jvmTest by getting
        return jvmTest
    }
public val NamedDomainObjectContainerScope<KotlinSourceSet>.commonMain: KotlinSourceSet
    get() {
        val commonMain by getting
        return commonMain
    }

public val NamedDomainObjectContainerScope<KotlinSourceSet>.commonTest: KotlinSourceSet
    get() {
        val commonTest by getting
        return commonTest
    }
