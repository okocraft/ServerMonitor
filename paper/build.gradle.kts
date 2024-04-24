plugins {
    id ("server-monitor.platform-conventions")
}

project.extra["platform.name"] = "Paper"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.platform.paper)
}

tasks {
    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand("projectVersion" to project.version)
        }
    }
}
