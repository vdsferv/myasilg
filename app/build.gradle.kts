
// build.gradle.kts (Module: app)
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// [추가!] local.properties 파일 로드
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}


android {
    namespace = "com.example.mysilgurae"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mysilgurae"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // [추가!] BuildConfig 필드 생성
        // local.properties에서 키를 읽어오고, 없다면 "EMPTY"로 설정
        buildConfigField("String", "REAL_ESTATE_API_KEY", "\"${localProperties.getProperty("REAL_ESTATE_API_KEY", "EMPTY")}\"")

        // [추가!] AndroidManifest에서 사용할 수 있도록 키를 resValue로 등록
        resValue("string", "google_maps_key", localProperties.getProperty("GOOGLE_MAPS_API_KEY", "EMPTY"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // MainActivity에서 ActivityMainBinding을 사용하기 위해 반드시 필요한 설정입니다.
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // 기본 라이브러리
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.0")

    // ViewModel & LiveData (데이터 관리)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")

    // Google Maps (지도)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Location (위치 정보)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Retrofit (네트워크 통신)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 테스트 라이브러리 (기본)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // [추가!] MPAndroidChart 라이브러리
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
