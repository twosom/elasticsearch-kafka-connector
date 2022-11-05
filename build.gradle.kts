plugins {
    application
    id("java")
}

group = "org.icloud"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("org.icloud.pipeline.ElasticSearchSinkConnector")
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.kafka:connect-api:3.3.1")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.7")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}