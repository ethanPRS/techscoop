import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.gradle)
    id("com.google.gms.google-services")
}

fun readNewsApiKey(): String {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        val props = Properties()
        localFile.inputStream().use { props.load(it) }
        props.getProperty("NEWS_API_KEY")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    }
    return (project.findProperty("NEWS_API_KEY") as String?)?.trim().orEmpty()
}

val newsApiKey: String = readNewsApiKey()

fun loadTeamKeystoreProperties(): Properties {
    val props = Properties()
    val file = file("team-keystore.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

val teamKeystoreProps = loadTeamKeystoreProperties()

android {
    namespace = "com.estudiante.techscoop"
    compileSdk = 35

    signingConfigs {
        create("teamDebug") {
            val storeFileName = teamKeystoreProps.getProperty("storeFile", "team-debug.keystore")
            storeFile = file(storeFileName)
            storePassword = teamKeystoreProps.getProperty("storePassword")
            keyAlias = teamKeystoreProps.getProperty("keyAlias")
            keyPassword = teamKeystoreProps.getProperty("keyPassword")
        }
    }

    defaultConfig {
        applicationId = "com.juanpabloramos.techscoop"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NEWS_API_KEY", "\"$newsApiKey\"")
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("teamDebug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Evita conflictos de archivos duplicados en META-INF que provienen de las
    // dependencias transitivas de JUnit Jupiter (mockk, espresso, etc.)
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.coil)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.mockk.android)
}
