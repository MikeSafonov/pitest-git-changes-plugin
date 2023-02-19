plugins {
    id("java")
}

group = "com.github.mikesafonov"

repositories {
    mavenCentral()
}

dependencies {
    val jgitVersion = "6.4.0.202211300538-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")

    val pitestVersion = "1.11.0"
    implementation("org.pitest:pitest:$pitestVersion")
    implementation("org.pitest:pitest-entry:$pitestVersion")

    val junitVersion = "5.8.1"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}