plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
    implementation(libs.annotations)
    implementation(libs.configapi.format.yaml) {
        exclude("org.yaml", "snakeyaml")
    }
    implementation(libs.discord.webhooks) {
        exclude("org.slf4j", "slf4j-api")
    }

    compileOnlyApi(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly(libs.slf4j.simple)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    test {
        useJUnitPlatform()
    }
}
