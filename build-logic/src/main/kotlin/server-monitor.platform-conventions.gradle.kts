plugins {
    id("server-monitor.common-conventions")
    id("io.github.goooler.shadow")
}

project.extra["platform.name"] = "unknown"
project.extra["relocation"] = "false"

dependencies {
    implementation(project(":server-monitor-core"))
}

tasks {
    build {
        dependsOn(shadowJar)
        doLast {
            val filepath = getArtifactFilepath()
            filepath.parentFile.mkdirs()
            shadowJar.get().archiveFile.get().asFile.copyTo(filepath, true)
        }
    }

    clean {
        doLast {
            getArtifactFilepath().delete()
        }
    }

    shadowJar {
        if (project.extra["relocation"] == "true") {
            mapOf(
                "kotlin" to "kotlin",
                "club.minnced.discord" to "discord",
                "okhttp3" to "okhttp3",
                "okio" to "okio",
                "org.json" to "json",
                "org.intellij.lang.annotations" to "intellij.annotations",
                "org.jetbrains.annotations" to "annotations",
                "com.github.siroshun09.configapi" to "configapi"
            ) .forEach {
                relocate(it.key, "${project.group}.lib.${it.value}")
            }
        }
    }
}

fun getArtifactFilepath(): File {
    return rootProject.layout.buildDirectory.dir("libs").get().file("ServerMonitor-${project.extra["platform.name"]}-${project.version}.jar").asFile
}
