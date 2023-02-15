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
    implementation("io.dropwizard:dropwizard-core:4.0.0-beta.3")
    implementation("io.dropwizard:dropwizard-auth:4.0.0-beta.3")
    implementation("io.dropwizard:dropwizard-forms:4.0.0-beta.3")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("commons-io:commons-io:2.11.0")

    implementation("com.twelvemonkeys.servlet:servlet:3.8.3:jakarta@jar")
    implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.9.4")

    implementation("org.sejda.imageio:webp-imageio:0.1.6")

    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    implementation("org.yaml:snakeyaml:1.33")

    implementation("com.squareup.keywhiz:keywhiz-hkdf:0.10.1")

    // https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
    implementation("com.nimbusds:nimbus-jose-jwt:9.30.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
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
