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
    features.set(listOf("+git-changes(target[HEAD^])"))
    targetClasses.set(listOf("com.github.mikesafonov.second.*"))
    targetTests.set(listOf("com.github.mikesafonov.second.*"))
    outputFormats.set(listOf("XML", "HTML", "GITHUB"))
    junit5PluginVersion.set("1.1.2")
    timestampedReports.set(false)
    if(System.getenv("CI").toBoolean()) {
        pluginConfiguration.set(
                mapOf(
                        "PROJECT_NAME" to "test-project-second",
                        "GITHUB_TOKEN" to System.getenv("GITHUB_TOKEN"),
                        "GITHUB_REPOSITORY_ID" to System.getenv("GITHUB_REPOSITORY_ID"),
                        "GITHUB_EVENT_PATH" to System.getenv("GITHUB_EVENT_PATH"),
                        "GITHUB_MUTANT_LEVEL" to "WARNING",
                        "GITHUB_FAIL_IF_MUTANTS_PRESENT" to "false"
                )
        )
    }
}
