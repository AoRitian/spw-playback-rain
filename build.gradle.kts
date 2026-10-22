import com.xuncorp.spw.workshop.gradle.PluginPermission
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("com.xuncorp.spw.workshop") version "0.1.0-dev23"
}

group = "com.aoritian.playbackrain"
version = "1.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("com.github.Moriafly.spw-workshop-api:spw-workshop-api:0.1.0-dev23")
    compileOnly("org.jetbrains.skiko:skiko-awt:0.150.1")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.skiko:skiko-awt:0.150.1")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.150.1")
    testImplementation("org.jetbrains.compose.runtime:runtime-desktop:1.12.1")
}

spmod {
    PluginClass = "com.aoritian.playbackrain.PlaybackRainPlugin"
    PluginId = "com.aoritian.playbackrain"
    PluginName = "\u64AD\u653E\u9875\u96E8\u6EF4"
    PluginDescription = "\u64AD\u653E\u9875\u6B4C\u8BCD\u80CC\u666F\u96E8\u6EF4\u6548\u679C"
    PluginVersion = "1.0.3"
    PluginProvider = "aoritian"
    PluginHasConfig = true
    PluginPermissions = listOf(
        PluginPermission.CLASS_TRANSFORM
    )
}
