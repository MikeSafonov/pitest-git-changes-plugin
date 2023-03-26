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
    val jgitVersion = "5.10.0.202012080955-r"
    api("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")

    val pitestVersion = project.properties["pitestVersion"]!!
    implementation("org.pitest:pitest:$pitestVersion")
    implementation("org.pitest:pitest-entry:$pitestVersion")

    val lombokVersion = "1.18.26"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("pitest-git")
                url.set("https://github.com/MikeSafonov/pitest-git-changes-plugin")
                description.set("Pitest git integration")
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