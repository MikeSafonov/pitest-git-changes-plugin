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
    val pitestVersion = project.properties["pitestVersion"]!!
    implementation("org.pitest:pitest:$pitestVersion")
    implementation("org.pitest:pitest-entry:$pitestVersion")

    implementation(project(":pitest-git"))
    implementation(project(":pitest-git-changes-report-plugin"))

    val lombokVersion = "1.18.26"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("org.kohsuke:github-api:1.314")


    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    val junitVersion = "5.8.1"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:1.3.3")
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