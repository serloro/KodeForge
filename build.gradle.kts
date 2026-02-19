plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.kodeforge"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // Targets adicionales se agregarán según necesidad
    // macosX64()
    // macosArm64()
    // mingwX64()
    // linuxX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // Provee Dispatchers.Main en JVM (Swing/AWT)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                // SubEthaSMTP para servidor SMTP fake
                implementation("org.subethamail:subethasmtp:3.1.7")
                // JSch para conexiones SFTP
                implementation("com.github.mwiede:jsch:0.2.16")
                
                // ===== DRIVERS JDBC PARA BASES DE DATOS =====
                // SQLite - Base de datos embebida
                implementation("org.xerial:sqlite-jdbc:3.45.1.0")
                // PostgreSQL - Base de datos relacional
                implementation("org.postgresql:postgresql:42.7.1")
                // MySQL - Base de datos relacional
                implementation("com.mysql:mysql-connector-j:8.2.0")
                // MariaDB - Fork de MySQL
                implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
                // SQL Server - Base de datos de Microsoft
                implementation("com.microsoft.sqlserver:mssql-jdbc:12.6.0.jre11")
                // Oracle - Base de datos empresarial (driver libre)
                implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
            }
        }
    }
}

// Configurar aplicación ejecutable
tasks.register<JavaExec>("runDemo") {
    group = "application"
    description = "Ejecuta la demostración de T0 - Workspace Portable JSON Layer"
    dependsOn("jvmMainClasses")
    classpath = files(
        kotlin.jvm().compilations.getByName("main").output.allOutputs,
        kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles
    )
    mainClass.set("com.kodeforge.MainKt")
    workingDir = projectDir
}

tasks.register<JavaExec>("runSchedulerDemo") {
    group = "application"
    description = "Ejecuta la demostración del Scheduler Secuencial MVP"
    dependsOn("jvmMainClasses")
    classpath = files(
        kotlin.jvm().compilations.getByName("main").output.allOutputs,
        kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles
    )
    mainClass.set("com.kodeforge.SchedulerDemoKt")
    workingDir = projectDir
}

// Configurar aplicación Compose Desktop
compose.desktop {
    application {
        mainClass = "com.kodeforge.ui.MainKt"
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
            )
            packageName = "KodeForge"
            packageVersion = "1.0.0"
            description = "KodeForge - Herramienta de desarrollo multiplataforma"
            vendor = "KodeForge"

            windows {
                // Configuración específica para Windows
                menuGroup = "KodeForge"
                perUserInstall = true
                // iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            }

            macOS {
                // iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
            }
        }
    }
}

