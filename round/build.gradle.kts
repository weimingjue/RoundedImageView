plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    `maven-publish`
}

android {
    namespace = "com.wang.round"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.weimingjue"
                artifactId = "RoundedImageView"
                version = "1.0.1"
            }
        }
    }
}

dependencies {
    api(libs.appcompat)
    api(libs.kotlin.stdlib.jdk8)
}
