/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

repositories {
    jcenter()
    mavenCentral()
}

apply {
    plugin("kotlin")
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.4.0"))
}
