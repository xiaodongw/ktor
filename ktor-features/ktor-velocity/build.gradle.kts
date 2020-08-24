description = ""
kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api("org.apache.velocity:velocity-engine-core:[2.0, 2.1)")
            }
        }
    }
}
