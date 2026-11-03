plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.wang.example"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = namespace
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        resValue("string", "app_name", "圆角iv示例")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf(
            "-Xcontext-parameters",//支持context（双上下文）
            "-Xwhen-guards",//支持when语句的子表达式
            "-Xjvm-default=all",//kotlin默认方法编译为java默认方法
            "-XXLanguage:+ExplicitBackingFields",//显式后备字段类型
        )
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
    }
}

dependencies {
    implementation(project(":round"))

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.glide)
    implementation(libs.constraintlayout)
}
