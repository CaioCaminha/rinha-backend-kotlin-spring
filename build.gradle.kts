plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.serialization") version "1.9.0"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.caminha"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

graalvmNative {
	binaries {
		named("main") {
			configurationFileDirectories.from(file("src/main/resources/META-INF/native-image"))
		}
	}
}

dependencies {
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.netty:netty-resolver-dns-native-macos:4.1.94.Final") {
		artifact {
			classifier = "osx-aarch_64" // For M1/M2 Macs
			// OR
			// classifier = "osx-x86_64" // For Intel Macs
		}
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
