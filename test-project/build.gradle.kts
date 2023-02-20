plugins {
    id("java")
    id("application")
    id("info.solidsoft.pitest") version "1.9.11"

}

group = "com.github.mikesafonov"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    val junitVersion = "5.8.1"

    pitest(project(":pitest-git-changes-plugin"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:1.3.3")
}

tasks.test {
    useJUnitPlatform()
}

pitest {
    pitestVersion.set("1.11.0")
    features.set(listOf("+git-changes(source[test] target[main] repository[${project.parent!!.rootDir.absolutePath}])"))
    targetClasses.set(listOf("com.github.mikesafonov.test.*"))
    targetTests.set(listOf("com.github.mikesafonov.test.*"))
    outputFormats.set(listOf("XML", "HTML"))
    junit5PluginVersion.set("1.1.2")
    timestampedReports.set(false)
}