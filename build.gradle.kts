import com.google.protobuf.gradle.id
import config.Versions

//
// https://github.com/google/protobuf-gradle-plugin/blob/master/examples/exampleProject/build.gradle
plugins {
    `maven-publish`
    id("com.google.protobuf")
}

dependencies {
    api("com.google.protobuf:protobuf-java:${Versions.PROTOBUF_VERSION}")

    // implementation("io.grpc:grpc-netty:1.15.1")
    implementation("io.grpc:grpc-stub:${Versions.GRPC_VERSION}")
    implementation("io.grpc:grpc-protobuf:${Versions.GRPC_VERSION}")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.PROTOBUF_VERSION}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC_VERSION}"
        }
    }

    // https://github.com/google/protobuf-gradle-plugin/issues/303
    // https://github.com/google/protobuf-gradle-plugin/issues/682
    //generateProtoTasks.setProperty("generatedFilesBaseDir", "$projectDir/src")
    generateProtoTasks {
        ofSourceSet("main").forEach  { task ->
            task.plugins {
                id("grpc") {
                    outputSubDir = "java"
                }
            }
        }
    }
}

/*val syncGeneratedProto by tasks.registering(Sync::class) {
    val srcDir = "${projectDir}/build/generated/source/proto/main/java"
    val destDir = "${projectDir}/src/main/java"
    from(srcDir)
    into(destDir)
}

val delGeneratedProto by tasks.registering(Delete::class) {
    val srcDir = "${projectDir}/build/generated/source/proto/main/java"
    delete(srcDir)
}

tasks {
    named("generateProto") {
        finalizedBy(syncGeneratedProto)
    }
    named("syncGeneratedProto") {
        finalizedBy(delGeneratedProto)
    }
    named("compileJava") {
        dependsOn(delGeneratedProto)
    }
}*/

/*
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", sourcesJar)
}*/

publishing {
    repositories {
        maven {
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                url = uri("http://localhost:8081/repository/maven-snapshots/")
            } else {
                url = uri("http://localhost:8081/repository/maven-releases/")
            }

            credentials {
                username = "admin"
                password = "123456"
            }
            isAllowInsecureProtocol = true
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            group = project.group
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}

tasks.named("publish") {
    dependsOn("build")
}
java {
    withSourcesJar()
}