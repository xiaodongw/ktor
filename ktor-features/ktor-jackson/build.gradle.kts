description = ""
kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api("com.fasterxml.jackson.core:jackson-databind$jackson_version")
                api("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_kotlin_version")
            }
        }
    }
}
