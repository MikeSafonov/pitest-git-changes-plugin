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
    pitest(project(":pitest-git-changes-report-github-plugin"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:1.3.3")
}

tasks.test {
    useJUnitPlatform()
}

pitest {
    pitestVersion.set("1.11.1")
    features.set(listOf("+git-changes"))
    targetClasses.set(listOf("com.github.mikesafonov.test.*"))
    targetTests.set(listOf("com.github.mikesafonov.tests.*"))
    outputFormats.set(listOf("XML", "HTML", "GITHUB"))
    junit5PluginVersion.set("1.1.2")
    timestampedReports.set(false)
    pluginConfiguration.set(mapOf("SOME_TEST_VAR" to "aaa"))
}
