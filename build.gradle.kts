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
                // SQLite JDBC para Query Runner
                implementation("org.xerial:sqlite-jdbc:3.45.1.0")
                // JSch para conexiones SFTP
                implementation("com.github.mwiede:jsch:0.2.16")
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

