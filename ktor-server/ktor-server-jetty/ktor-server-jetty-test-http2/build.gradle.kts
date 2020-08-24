
kotlin.sourceSets {
    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-test-host"))
            api("org.eclipse.jetty:jetty-servlet:$jetty_version")
            api(project(":ktor-server:ktor-server-core"))
            api(project(":ktor-server:ktor-server-jetty"))
        }
    }
}

//configure(tasks.jvmTest) {
//    useJUnit()
//
//    systemProperty "enable.http2", "true"
//    exclude("**/*StressTest*")
//}
