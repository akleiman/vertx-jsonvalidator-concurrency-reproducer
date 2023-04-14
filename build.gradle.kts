plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.example.Main")
//    mainModule.set("untitled.main")
    applicationDefaultJvmArgs = listOf("--enable-preview")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

dependencies {
    implementation("io.vertx:vertx-json-schema:4.4.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
}

tasks.test {
    useJUnitPlatform()
}