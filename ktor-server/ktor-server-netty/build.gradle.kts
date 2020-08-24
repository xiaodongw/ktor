description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-server:ktor-server-host-common"))

            api("io.netty:netty-codec-http2:$netty_version")
            api("org.eclipse.jetty.alpn:alpn-api:$jetty_alpn_api_version")

            api("io.netty:netty-transport-native-kqueue:$netty_version")
            api("io.netty:netty-transport-native-epoll:$netty_version")
        }
    }
    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-test-host"))
            api(project(":ktor-server:ktor-server-core"))

            api("io.netty:netty-tcnative:$netty_tcnative_version")
            api("io.netty:netty-tcnative-boringssl-static:$netty_tcnative_version")
        }
    }
}

//def enableAlpnProp = project.hasProperty("enableAlpn")
//if (enableAlpnProp) {
//    def nativeClassifier;
//    def osName = System.getProperty("os.name").toLowerCase()
//
//    if (osName.contains("win")) {
//        nativeClassifier = "windows-x86_64"
//    } else if (osName.contains("linux")) {
//        nativeClassifier = "linux-x86_64"
//    } else if (osName.contains("mac")) {
//        nativeClassifier = "osx-x86_64"
//    } else {
//        throw new InvalidUserDataException("Unsupoprted os family $osName")
//    }
//
//    dependencies {
//        compile group: "io.netty", name: "netty-tcnative-boringssl-static", version: netty_tcnative_version, classifier: nativeClassifier
//    }
//}
//
//dependencies {
//    jvmTestApi project(path: ":ktor-server:ktor-server-core", configuration: "testOutput")
//}
