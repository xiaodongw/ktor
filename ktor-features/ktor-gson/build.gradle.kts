description = ""
kotlin.sourceSets.jvmMain {
    dependencies {
        api(project(":ktor-utils"))
        api("com.google.code.gson:gson:$gson_version")
    }
}
