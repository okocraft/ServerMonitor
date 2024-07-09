plugins {
    id("server-monitor.platform-conventions")
}

project.extra["platform.name"] = "Paper"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.platform.paper)
    testImplementation(libs.platform.paper)
}

tasks {
    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand("projectVersion" to project.version)
        }
    }

    jar {
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
}
