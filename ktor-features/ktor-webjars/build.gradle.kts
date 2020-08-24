description = ""

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api("org.webjars:webjars-locator-core:0.43")
            }
        }
        jvmTest {
            dependencies {
                api("org.webjars:jquery:3.3.1")
            }

        }
    }
}
