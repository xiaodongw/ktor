description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-features:ktor-auth"))
            implementation("com.googlecode.json-simple:json-simple:$json_simple_version")
            api("com.auth0:java-jwt:$java_jwt_version")
            api("com.auth0:jwks-rsa:$jwks_rsa_version")
        }
    }
    jvmTest {
        dependencies {
            api("com.nhaarman:mockito-kotlin:1.6.0")
        }
    }
}

