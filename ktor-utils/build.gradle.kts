import org.jetbrains.kotlin.gradle.plugin.mpp.*

val ideaActive: Boolean by project.extra

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":ktor-io"))
            }
        }
        commonTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
            }
        }
    }
}
