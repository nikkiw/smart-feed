import org.gradle.api.tasks.testing.TestReport

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.benchmark) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka)
//    jacoco
}


subprojects {
    apply(plugin = "org.jetbrains.dokka")
}
//jacoco {
//    toolVersion = libs.versions.jacoco.get()
//}
//
//// Паттерны исключений: не учитывать сгенерированные Hilt/Room/etc.
//val coverageExcludes = listOf(
//    // Android‐сгенерированные
//    "**/R.class",
//    "**/R\$*.class",
//    "**/BuildConfig.*",
//    "**/Manifest*.*",
//    "**/*Test*.*",
//    "android/**",
//
//    // Dagger/Hilt
//    "dagger/**",
//    "hilt_aggregated_deps/**",
//    "**/Hilt_*.*",
//    "**/Dagger*.*",
//    "**/*_Factory.*",
//    "**/*_MembersInjector.*",
//
//    // Room
//    "**/*_Impl.class",
//    "**/androidx/room/**",
//
//    // DataBinding (если используется)
//    "**/androidx/databinding/**"
//)
//
//tasks.register<JacocoReport>("jacocoRootReport") {
//    group = "verification"
//    description = "Сводный Jacoco‐отчёт по всем подпроектам (по всем найденным test<Variant>UnitTest таскам)."
//
//    // 1) Найдём и подцепим все таски типа Test, в имени которых есть "UnitTest"
//    //    Это могут быть testDebugUnitTest, testReleaseUnitTest, testFreeDebugUnitTest и т. д.
//    subprojects.forEach { subproj ->
//        // Отбираем таски, унаследованные от Test и с именем, содержащим "UnitTest"
//        subproj.tasks.matching { task ->
//            // Класс Test содержится в полном пути имени задачи, но мы смотрим по имени
//            task is Test && task.name.contains("UnitTest")
//        }.forEach { testTask ->
//            // Делаем зависимость: jacocoRootReport зависит от всех найденных UnitTest-тасок
//            dependsOn(testTask)
//        }
//    }
//
//    // 2) Сбор всех .exec файлов. Ищем в подпроектах:
//    //    - build/jacoco/<имя‐таски>.exec
//    //    — универсально: ищем все *.exec внутри subproj/build/jacoco
//    val execFiles = subprojects.flatMap { subproj ->
//        fileTree("${subproj.buildDir}/jacoco") {
//            include("*.exec")
//        }.files
//    }
//    executionData.setFrom(files(execFiles))
//
//    // 3) Сбор директорий с .class из подпроектов:
//    //    - Для Android‐модулей: build/intermediates/javac/<variant>/classes (обычно variant="debug")
//    //    - Для Java/Kotlin-модулей: build/classes/java/main
//    val classDirs = subprojects.flatMap { subproj ->
//        // Пытаемся найти android‐путь
//        val androidClasses = file("${subproj.buildDir}/intermediates/javac/debug/classes")
//        if (androidClasses.exists()) {
//            listOf(fileTree(androidClasses) { exclude(coverageExcludes) })
//        } else {
//            // Если нет android‐директории, пробуем стандартное Java-каталог
//            val javaClasses = file("${subproj.buildDir}/classes/java/main")
//            if (javaClasses.exists()) {
//                listOf(fileTree(javaClasses) { exclude(coverageExcludes) })
//            } else {
//                emptyList()
//            }
//        }
//    }
//    classDirectories.setFrom(files(classDirs))
//
//    // 4) Сбор исходников из каждого модуля: src/main/java и src/main/kotlin (если существует)
//    val sourceDirs = subprojects.flatMap { subproj ->
//        listOf(
//            "${subproj.projectDir}/src/main/java",
//            "${subproj.projectDir}/src/main/kotlin"
//        )
//    }.filter { File(it).exists() }
//    sourceDirectories.setFrom(files(sourceDirs))
//
//    // 5) Форматы отчёта
//    reports {
//        html.required.set(true)  // Генерировать HTML‐отчёт
//        xml.required.set(true)   // Генерировать XML‐отчёт (для CI/SonarQube)
//        csv.required.set(false)
//    }
//}

val devDebugCombinedReport by tasks.registering(TestReport::class) {
    // Куда складывать готовый HTML
    destinationDirectory.set(layout.buildDirectory.dir("reports/devDebugCombined"))

    // Откуда брать XML с результатами
    reportOn(
        // unit-тесты
        fileTree("$buildDir/test-results/testDevDebugUnitTest"),
        // androidTest результаты
        fileTree("$buildDir/outputs/androidTest-results/connected")
    )

    // Чтобы агрегатор запускался только после тестов
    dependsOn(
//        tasks.named("testDevDebugUnitTest"),
        tasks.named("connectedDevDebugAndroidTest")
    )
}