description = ""
kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-server:ktor-server-host-common"))
            api(project(":ktor-http:ktor-http-cio"))

            compileOnly("javax.servlet:javax.servlet-api:$javax_servlet_api")
        }
    }

    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
        }
    }
}
