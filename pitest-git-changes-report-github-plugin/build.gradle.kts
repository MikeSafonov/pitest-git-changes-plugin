plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "com.github.mikesafonov"
version = project.properties["pluginVersion"]!!

repositories {
    mavenCentral()
}

tasks.jar {
    enabled = true
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(libs.pitest.core)
    implementation(libs.pitest.entry)

    implementation(project(":pitest-git"))
    implementation(project(":pitest-git-changes-report-plugin"))

    implementation(libs.github)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.logback)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("pitest-git-changes-report-github-plugin")
                url.set("https://github.com/MikeSafonov/pitest-git-changes-plugin")
                description.set("Pitest plugin for publishing mutation report to Github PR")
                organization {
                    name.set("com.github.mikesafonov")
                    url.set("https://github.com/MikeSafonov")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/MikeSafonov/pitest-git-changes-plugin/issues")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/MikeSafonov/pitest-git-changes-plugin/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                scm {
                    url.set("https://github.com/MikeSafonov/pitest-git-changes-plugin")
                    connection.set("scm:git:git://github.com/MikeSafonov/pitest-git-changes-plugin.git")
                    developerConnection.set("scm:git:ssh://git@github.com:MikeSafonov/pitest-git-changes-plugin.git")
                }
                developers {
                    developer {
                        name.set("Mike Safonov")
                        organization.set("com.github.mikesafonov")
                        organizationUrl.set("https://github.com/MikeSafonov")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

pitest {
    pitestVersion.set("1.11.7")
    junit5PluginVersion.set("1.1.2")
    timestampedReports.set(false)
    features.set(listOf("+git-changes(target[HEAD^])"))
    outputFormats.set(listOf("GITHUB", "XML"))
    if(System.getenv("CI").toBoolean()) {
        pluginConfiguration.set(
                mapOf(
                        "PROJECT_NAME" to project.name,
                        "GITHUB_TOKEN" to System.getenv("GITHUB_TOKEN"),
                        "GITHUB_REPOSITORY_ID" to System.getenv("GITHUB_REPOSITORY_ID"),
                        "GITHUB_EVENT_PATH" to System.getenv("GITHUB_EVENT_PATH"),
                        "GITHUB_MUTANT_LEVEL" to "WARNING",
                        "GITHUB_FAIL_IF_MUTANTS_PRESENT" to "false"
                )
        )
    }
}