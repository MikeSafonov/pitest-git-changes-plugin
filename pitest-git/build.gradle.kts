plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

tasks.jar {
    enabled = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    val jgitVersion = "5.10.0.202012080955-r"
    api("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")

    val pitestVersion = project.properties["pitestVersion"]!!
    implementation("org.pitest:pitest:$pitestVersion")
    implementation("org.pitest:pitest-entry:$pitestVersion")

    val lombokVersion = "1.18.26"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}
