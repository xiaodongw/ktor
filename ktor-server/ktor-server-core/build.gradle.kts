description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-utils"))
            api(project(":ktor-http"))

            api("com.typesafe:config:$typesafe_config_version")
            api("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
        }
    }
    jvmTest {
        dependencies {
            api(project(":ktor-http:ktor-http-cio"))
            api(project(":ktor-network"))
        }
    }
}

//artifacts {
//    testOutput jarTest
//}
