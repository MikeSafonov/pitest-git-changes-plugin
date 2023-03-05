plugins {
    id("java-library")
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

    val lombokVersion = "1.18.26"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

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
