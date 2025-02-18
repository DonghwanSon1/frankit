plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	id ("org.flywaydb.flyway") version "8.5.13"
	kotlin("plugin.jpa") version "2.1.0"
	kotlin("kapt") version "2.1.0"
}

group = "com.project"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(23))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// QueryDSL
	implementation ("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	kapt ("com.querydsl:querydsl-apt:5.0.0:jakarta")

	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Flyway
	implementation ("org.flywaydb:flyway-core")
	implementation ("org.flywaydb:flyway-mysql")

	// Swagger
	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.mockk:mockk:1.13.7")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	runtimeOnly("com.mysql:mysql-connector-j")
}

kotlin {
	jvmToolchain(23)
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs(
		"-XX:+EnableDynamicAgentLoading",
		"-Xshare:off"
		)
}