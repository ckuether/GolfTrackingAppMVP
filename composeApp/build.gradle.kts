import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

//            // Google Maps
            implementation(libs.maps.compose)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(projects.coreUi)
            implementation(project(Modules.locationDomain))
            implementation(project(Modules.locationPresentation))
            implementation(project(Modules.roundOfGolfDomain))
            implementation(project(Modules.roundOfGolfPresentation))

            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.compose.ui.backhandler)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.navigation.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Room Database
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.coil.compose)
            implementation(libs.coil.svg)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.arccosmvp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.arccosmvp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        
        // Inject Google Maps API key from gradle.properties
        val mapsApiKey = project.findProperty("GOOGLE_MAPS_API_KEY") as String? ?: ""
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    add("kspCommonMainMetadata", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Copy shared module's compose resources with proper namespace
val copySharedResources by tasks.registering(Copy::class) {
    from("../shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources")
    into("$buildDir/generated/assets/copyDebugComposeResourcesToAndroidAssets/composeResources/arccosmvp.shared.generated.resources")
    dependsOn(
        ":shared:prepareComposeResourcesTaskForCommonMain",
        ":shared:copyNonXmlValueResourcesForCommonMain",
        ":shared:convertXmlValueResourcesForCommonMain"
    )
}

// Ensure shared resources are copied before merging assets
afterEvaluate {
    tasks.findByName("mergeDebugAssets")?.dependsOn(copySharedResources)
}