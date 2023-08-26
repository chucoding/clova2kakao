plugins {
    id("java")
}

group = "com.chucoding"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    val customJar by creating(Jar::class) {
        manifest {
            attributes["Main-Class"] = "com.chucoding.Main"
        }
        from(sourceSets.main.get().output)
    }

    named("build") {
        dependsOn(customJar)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}