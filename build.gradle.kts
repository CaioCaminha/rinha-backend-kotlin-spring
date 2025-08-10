import org.graalvm.buildtools.gradle.dsl.NativeImageOptions

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

			// Existing configuration
			buildArgs.add("-R:MaximumHeapSizePercent=90")
			buildArgs.add("--initialize-at-build-time=kotlin.DeprecationLevel")
			buildArgs.add("--trace-class-initialization=kotlin.DeprecationLevel")
			configurationFileDirectories.from(file("src/main/resources/META-INF/native-image"))
		}
	}
}

dependencies {

//	Http4K Dependencies
//	implementation(platform("org.http4k:http4k-bom:6.15.1.0"))
//	implementation("org.http4k:http4k-core")
//	implementation("org.http4k:http4k-format-moshi")
////	implementation("org.http4k:http4k-server-netty")
//	implementation("org.http4k:http4k-server-servlet")

	//Undertow Dependencies
	implementation("io.undertow:undertow-core:2.3.18.Final")
	implementation("io.undertow:undertow-servlet:2.3.18.Final")

	// Serialization Dependencies
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.7.3")

	//Coroutines Dependencies
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	//Spring / Webflux Dependencies
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
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
