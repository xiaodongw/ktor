description = ""
kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api("io.dropwizard.metrics:metrics-core:4.1.2")
                api("io.dropwizard.metrics:metrics-jvm:4.1.2")
            }
        }
    }
}
