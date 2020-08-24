description = ""

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(project(":ktor-client:ktor-client-core"))
                implementation("com.googlecode.json-simple:json-simple:$json_simple_version")
            }
        }
        jvmTest {
            dependencies {
                api(project(":ktor-client:ktor-client-cio"))
                api(project(":ktor-server:ktor-server-test-host"))
            }
        }
    }
}
