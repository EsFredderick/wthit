import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

fun Project.setupPlatform() {
    val rootSourceSets = rootProject.extensions.getByType<SourceSetContainer>()

    extensions.configure<SourceSetContainer> {
        named("main") {
            resources.srcDir(rootProject.file("src/resources/resources"))
            rootSourceSets.forEach {
                compileClasspath += it.output
                runtimeClasspath += it.output
            }
        }
    }

    tasks.named<Jar>("jar") {
        rootSourceSets.forEach {
            from(it.output)
        }
    }

    tasks.named<Jar>("sourcesJar") {
        rootSourceSets.forEach {
            from(it.allSource)
        }
    }
}
