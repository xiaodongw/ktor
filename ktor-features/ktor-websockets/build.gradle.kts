description = ""
kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-http:ktor-http-cio"))
        }
    }
    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-jetty"))
            api(project(":ktor-server:ktor-server-netty"))
            api(project(":ktor-server:ktor-server-tomcat"))
            api(project(":ktor-server:ktor-server-cio"))
            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
        }
    }
}
