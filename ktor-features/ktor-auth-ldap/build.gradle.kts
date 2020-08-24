description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-features:ktor-auth"))
        }
    }
    jvmTest {
        dependencies {
            api("org.apache.directory.server:apacheds-server-integ:$apacheds_version")
            api("org.apache.directory.server:apacheds-core-integ:$apacheds_version")
        }
    }
}
