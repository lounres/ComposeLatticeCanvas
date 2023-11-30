import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(libs.plugins) {
        alias(kotlin.multiplatform)
        alias(compose)
        alias(dokka)
    }
    `maven-publish`
}

val jvmTargetVersion : String by properties

kotlin {
    explicitApi = ExplicitApiMode.Warning

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = jvmTargetVersion
                freeCompilerArgs += listOf(
                    "-Xlambdas=indy"
                )
            }
        }
        testRuns.all {
            executionTask {
                useJUnitPlatform()
            }
        }
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//    }

//    androidTarget()
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//    macosArm64()

    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.ExperimentalUnsignedTypes")
                enableLanguageFeature("ContextReceivers")
            }
        }
        commonMain {
            dependencies {
                implementation(kone.misc.lattices)
                implementation(compose.foundation)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

dependencies {
    dokkaPlugin(libs.dokka.mathjax)
}

task<Jar>("dokkaJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier = "javadoc"
    afterEvaluate {
        val dokkaHtml by tasks.getting
        dependsOn(dokkaHtml)
        from(dokkaHtml)
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            artifact(tasks.named<Jar>("dokkaJar"))
        }
    }
}