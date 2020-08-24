description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-server:ktor-server-core"))
            api(project(":ktor-server:ktor-server-host-common"))
            api(project(":ktor-network:ktor-network-tls"))
            api(project(":ktor-network:ktor-network-tls:ktor-network-tls-certificates"))
            api(project(":ktor-client:ktor-client-core"))
            api(project(":ktor-client:ktor-client-jetty"))
            api(project(":ktor-client:ktor-client-cio"))
            api(project(":ktor-client:ktor-client-tests"))

            // Not ideal, but prevents an additional artifact, and this is usually just included for testing,
            // so shouldn"t increase the size of the final artifact.
            api(project(":ktor-features:ktor-websockets"))

            api("ch.qos.logback:logback-classic:$logback_version")
            api("org.eclipse.jetty.http2:http2-client:$jetty_version")
            api("org.eclipse.jetty:jetty-client:$jetty_version")
            api("org.eclipse.jetty.http2:http2-http-client-transport:$jetty_version")

            api("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
            api("junit:junit:$junit_version")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutines_version")
        }
    }

    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
        }
    }
}
