plugins {
    application
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

application {
    mainClass.set("net.pistonmaster.gallery.GalleryApplication")
}

group = "net.pistonmaster"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.dropwizard:dropwizard-core:4.0.0-beta.2")
    implementation("io.dropwizard:dropwizard-auth:4.0.0-beta.2")
    implementation("io.dropwizard:dropwizard-forms:4.0.0-beta.2")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    implementation("commons-io:commons-io:2.11.0")

    implementation("com.twelvemonkeys.servlet:servlet:3.8.2:jakarta@jar")
    implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.8.2")

    implementation("org.sejda.imageio:webp-imageio:0.1.6")

    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    implementation("org.yaml:snakeyaml:1.30")

    implementation("com.squareup.keywhiz:keywhiz-hkdf:0.10.1")

    // https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
    implementation("com.nimbusds:nimbus-jose-jwt:9.23")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    processResources {
        filter { line: String ->
            line.replace("@projectVersion@", "${rootProject.version}")
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
