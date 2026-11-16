// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    id("com.google.protobuf") version "0.10.0" apply false
}

subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        toolVersion.set("2.0.0-alpha.5") // keep in sync with the detekt plugin version in libs.versions.toml
        buildUponDefaultConfig.set(true)
        config.from(rootProject.file("config/detekt/detekt.yml"))
        val baselineName = project.path.trimStart(':').replace(':', '-').ifEmpty { "root" } + ".xml"
        baseline.set(rootProject.layout.projectDirectory.file("config/detekt/baseline/$baselineName"))
    }

    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        jvmTarget.set("11")
        reports {
            html.required.set(true)
            checkstyle.required.set(false)
            sarif.required.set(false)
            markdown.required.set(false)
        }
    }
    tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
        jvmTarget.set("11")
    }

    // Keep detekt out of `check` so local builds stay fast; run it explicitly.
    plugins.withId("lifecycle-base") {
        tasks.named("check").configure {
            setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name.startsWith("detekt") })
        }
    }
}