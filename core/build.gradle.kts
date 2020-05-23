import com..Config

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.coroutinesVersion}")
    implementation("com.squareup.okhttp3:okhttp:${Config.okHttpVersion}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Config.okHttpVersion}")
    implementation("com.h2database:h2:1.4.200")
    implementation("com.beust:klaxon:5.0.1")
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("org.hibernate:hibernate-core:5.4.10.Final")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
