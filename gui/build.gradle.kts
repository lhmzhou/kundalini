import com.Config
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("javafx-gradle-plugin")
    id("com.github.johnrengelman.shadow")
    java
    application
}

dependencies {
    compile(project(":core"))
    compile("no.tornado:tornadofx:1.7.17")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${Config.coroutinesVersion}")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "com.kundalini.gui.kundaliniApp"
}

jfx {
    mainClass = "com.kundalini.gui.kundaliniApp"
    jfxAppOutputDir = "build/jfx/app"
    jfxMainAppJarName = "kundalini.jar"
    appName = "kundalini"
    isUpdateExistingJar = true
    bundler = "mac.app"
    bundleArguments = mapOf(
        // TODO: find a better way to do this instead of hard-coding
        "runtime" to "/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home"
    )
    isFailOnError = true
}

tasks {
    "shadowJar"(ShadowJar::class) {
        archiveBaseName.set("kundalini")
        archiveClassifier.set(null as? String?)
        archiveVersion.set(null as? String?)
    }
}

task("copyToLib", Copy::class) {
    into("$buildDir/lib")
    from(configurations.runtimeClasspath)
}