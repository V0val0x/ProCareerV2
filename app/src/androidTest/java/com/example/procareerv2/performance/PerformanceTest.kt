package com.example.procareerv2.performance

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.WindowMetrics
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.example.procareerv2.MainActivity
import com.example.procareerv2.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    private lateinit var device: UiDevice
    private val performanceMetrics = mutableMapOf<String, Long>()
    private val timeout = 5000L // 5 seconds timeout
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var reportDir: File
    private val allResults = StringBuilder()

    companion object {
        private const val TAG = "PerformanceTest"
        private const val SCROLL_STEPS = 10
        private const val FRAME_TIME_THRESHOLD = 16L // 60 FPS = 16.67ms per frame
        private const val TARGET_FPS = 60
        private const val MEASUREMENT_DURATION = 500L // 500ms для измерения FPS
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(InstrumentationRegistry.getInstrumentation().targetContext.packageName)), timeout)
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        reportDir = File(context.filesDir, "performance_reports")
        reportDir.mkdirs()
        logInfo("🔍 Performance reports will be saved to: ${reportDir.absolutePath}")
    }

    @Test
    fun measureTestListPerformance() {
        logInfo("\n=====================================")
        logInfo("📋 Testing Test List Performance")
        logInfo("=====================================")
        
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
        
        var activity: Activity? = null
        scenario.onActivity { activity = it }
        
        // Переход к списку тестов
        device.findObject(By.text("Тесты"))?.click()
        Thread.sleep(1000)
        
        val startMemory = getMemoryInfo()
        var totalDroppedFrames = 0L
        var avgFps = 0.0
        var measurementCount = 0
        
        // Измеряем скорость загрузки списка и FPS
        measureTime("test_list_load") {
            // Ждем появления списка
            device.wait(Until.findObject(By.res("com.example.procareerv2:id/testsList")), timeout)
            
            activity?.let {
                val (fps, dropped) = measureFrameMetrics(it)
                avgFps = fps
                totalDroppedFrames += dropped
                measurementCount++
                logInfo("📊 Initial FPS: $fps, Dropped Frames: $dropped")
            }
        }
        
        // Измеряем производительность прокрутки
        measureTime("test_list_scroll") {
            val testsList = device.findObject(By.res("com.example.procareerv2:id/testsList"))
            repeat(SCROLL_STEPS) { step ->
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    10
                )
                Thread.sleep(500)
                
                activity?.let {
                    val (fps, dropped) = measureFrameMetrics(it)
                    avgFps = (avgFps * measurementCount + fps) / (measurementCount + 1)
                    measurementCount++
                    totalDroppedFrames += dropped
                    logInfo("📊 Scroll Step ${step + 1} - FPS: $fps, Dropped Frames: $dropped")
                }
            }
        }
        
        val endMemory = getMemoryInfo()
        val memoryIncreaseMB = endMemory.first - startMemory.first
        
        val metrics = """
            📊 Test List Performance Metrics:
            ⏱️ List Load Time: ${performanceMetrics["test_list_load"]} ms
            ⏱️ Scroll Time: ${performanceMetrics["test_list_scroll"]} ms
            🎮 Average FPS: %.1f
            📉 Total Dropped Frames: $totalDroppedFrames
            💾 Memory Usage Increase: ${memoryIncreaseMB}MB
            
            Performance Ratings:
            ${ratePerformance(performanceMetrics["test_list_load"] ?: 0, "list_load")}
            ${rateScrollPerformance(totalDroppedFrames)}
            ${rateFPS(avgFps)}
            ${rateMemoryUsage(memoryIncreaseMB)}
        """.trimIndent().format(avgFps)
        
        logInfo("\n===== FINAL PERFORMANCE RESULTS =====")
        logInfo(metrics)
        logInfo("=====================================\n")
        
        saveMetricsToFile(metrics)
        scenario.close()
    }

    @Test
    fun measureScreenTransitionsPerformance() {
        logInfo("\n=====================================")
        logInfo("🔄 Testing Screen Transitions Performance")
        logInfo("=====================================")
        
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
        
        var activity: Activity? = null
        scenario.onActivity { activity = it }
        
        // Измеряем переход между основными экранами
        val screens = listOf("Тесты", "Вакансии", "Профиль")
        
        screens.forEach { screenName ->
            measureTime("transition_to_${screenName.lowercase()}") {
                device.findObject(By.text(screenName))?.click()
                Thread.sleep(500) // Даем время на анимацию
                
                activity?.let {
                    val (fps, dropped) = measureFrameMetrics(it)
                    logInfo("📊 Transition to $screenName - FPS: $fps, Dropped Frames: $dropped")
                }
            }
        }
        
        logResults()
    }

    @Test
    fun measureVacancyListPerformance() {
        logInfo("\n=====================================")
        logInfo("📋 Testing Vacancy List Performance")
        logInfo("=====================================")
        
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
        
        var activity: Activity? = null
        scenario.onActivity { activity = it }
        
        // Переход к списку вакансий
        device.findObject(By.text("Вакансии"))?.click()
        Thread.sleep(1000)
        
        val startMemory = getMemoryInfo()
        var totalDroppedFrames = 0L
        var avgFps = 0.0
        
        // Измеряем скорость загрузки списка вакансий
        measureTime("vacancy_list_load") {
            device.wait(Until.findObject(By.res("com.example.procareerv2:id/rvVacancies")), timeout)
            
            activity?.let {
                val (fps, dropped) = measureFrameMetrics(it)
                avgFps = fps
                totalDroppedFrames += dropped
                logInfo("📊 Initial FPS: $fps, Dropped Frames: $dropped")
            }
        }
        
        // Измеряем производительность прокрутки списка вакансий
        measureTime("vacancy_list_scroll") {
            val listSelector = UiScrollable(UiSelector().resourceId("com.example.procareerv2:id/rvVacancies"))
            
            repeat(SCROLL_STEPS) { step ->
                listSelector.scrollForward()
                Thread.sleep(300)
                
                activity?.let {
                    val (fps, dropped) = measureFrameMetrics(it)
                    avgFps = (avgFps + fps) / 2
                    totalDroppedFrames += dropped
                    logInfo("📊 Scroll Step ${step + 1} - FPS: $fps, Dropped Frames: $dropped")
                }
            }
        }
        
        val endMemory = getMemoryInfo()
        logMemoryUsage(startMemory, endMemory)
        logInfo("📊 Average FPS during scrolling: $avgFps")
        logInfo("📊 Total dropped frames: $totalDroppedFrames")
        
        logResults()
    }

    @Test
    fun measureVacancyDetailsPerformance() {
        logInfo("\n=====================================")
        logInfo("🔍 Testing Vacancy Details Performance")
        logInfo("=====================================")
        
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
        
        var activity: Activity? = null
        scenario.onActivity { activity = it }
        
        // Переход к списку вакансий
        device.findObject(By.text("Вакансии"))?.click()
        Thread.sleep(1000)
        
        // Измеряем скорость загрузки деталей вакансии
        measureTime("vacancy_details_load") {
            // Кликаем на первую вакансию в списке
            val firstVacancy = device.wait(
                Until.findObject(By.res("com.example.procareerv2:id/rvVacancies")
                    .hasChild(By.clazz("android.view.ViewGroup"))), timeout)
            firstVacancy?.click()
            
            // Ждем загрузки деталей
            device.wait(Until.findObject(By.res("com.example.procareerv2:id/vacancyDetailsScroll")), timeout)
            
            activity?.let {
                val (fps, dropped) = measureFrameMetrics(it)
                logInfo("📊 Details Load - FPS: $fps, Dropped Frames: $dropped")
            }
        }
        
        // Измеряем прокрутку деталей вакансии
        measureTime("vacancy_details_scroll") {
            val detailsSelector = UiScrollable(UiSelector().resourceId("com.example.procareerv2:id/vacancyDetailsScroll"))
            
            repeat(SCROLL_STEPS / 2) { step ->
                detailsSelector.scrollForward()
                Thread.sleep(300)
                
                activity?.let {
                    val (fps, dropped) = measureFrameMetrics(it)
                    logInfo("📊 Details Scroll Step ${step + 1} - FPS: $fps, Dropped Frames: $dropped")
                }
            }
        }
        
        logResults()
    }

    private fun measureFrameMetrics(activity: Activity, duration: Long = MEASUREMENT_DURATION): Pair<Double, Long> {
        val frameCount = AtomicInteger(0)
        val droppedFrames = AtomicInteger(0)
        val startTime = System.nanoTime()
        
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                val currentTime = System.nanoTime()
                if (currentTime - startTime <= duration * 1_000_000) {
                    frameCount.incrementAndGet()
                    val frameDuration = (currentTime - frameTimeNanos) / 1_000_000 // Convert to ms
                    if (frameDuration > FRAME_TIME_THRESHOLD) {
                        droppedFrames.incrementAndGet()
                    }
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }
        
        activity.runOnUiThread {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
        
        Thread.sleep(duration + 100) // Wait for measurement to complete
        
        val actualDuration = (System.nanoTime() - startTime) / 1_000_000 // Convert to ms
        val fps = frameCount.get() * 1000.0 / actualDuration
        
        return Pair(fps, droppedFrames.get().toLong())
    }

    private fun getMemoryInfo(): Pair<Long, Long> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usedMemInMB = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
        val availableMemInMB = memoryInfo.availMem / (1024 * 1024)
        
        return Pair(usedMemInMB, availableMemInMB)
    }

    private fun measureTime(operation: String, block: () -> Unit) {
        val startTime = System.currentTimeMillis()
        block()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        performanceMetrics[operation] = duration
        logInfo("⏱️ $operation took $duration ms")
    }

    private fun logInfo(message: String) {
        Log.i(TAG, message)
        println("I/$TAG: $message")
        allResults.append(message).append('\n')
    }

    private fun logError(message: String) {
        Log.e(TAG, message)
        println("E/$TAG: $message")
        allResults.append("ERROR: ").append(message).append('\n')
    }

    private fun logResults() {
        val metrics = buildString {
            appendLine("📊 Performance Metrics:")
            performanceMetrics.forEach { (key, value) ->
                appendLine("⏱️ $key: $value ms")
            }
            appendLine()
            appendLine("💾 Memory Usage Summary:")
            append(allResults.toString())
        }
        
        logInfo("\n===== FINAL PERFORMANCE RESULTS =====")
        logInfo(metrics)
        logInfo("=====================================\n")
        
        saveMetricsToFile(metrics)
    }

    private fun saveMetricsToFile(metrics: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val reportFile = File(reportDir, "performance_report_$timestamp.txt")
        
        try {
            reportFile.writeText(metrics)
            logInfo("📝 Report saved to: ${reportFile.absolutePath}")
        } catch (e: Exception) {
            logError("❌ Failed to save report: ${e.message}")
        }
    }

    private fun logMemoryUsage(startMemory: Pair<Long, Long>, endMemory: Pair<Long, Long>) {
        val memoryIncreaseMB = endMemory.first - startMemory.first
        val memoryUsage = buildString {
            appendLine("💾 Memory Usage:")
            appendLine("🔴 Used Memory: ${endMemory.first} MB (was ${startMemory.first} MB)")
            appendLine("🟢 Available Memory: ${endMemory.second} MB (was ${startMemory.second} MB)")
            appendLine("🔵 Memory Increase: $memoryIncreaseMB MB")
        }
        
        logInfo("\n$memoryUsage\n")
    }

    private fun rateFPS(fps: Double): String {
        val rating = when {
            fps >= 58.0 -> "⭐⭐⭐⭐⭐ Excellent (${fps.format(1)} FPS)"
            fps >= 45.0 -> "⭐⭐⭐⭐ Very Good (${fps.format(1)} FPS)"
            fps >= 30.0 -> "⭐⭐⭐ Good (${fps.format(1)} FPS)"
            fps >= 24.0 -> "⭐⭐ Fair (${fps.format(1)} FPS)"
            else -> "⭐ Needs Improvement (${fps.format(1)} FPS)"
        }
        return "🎮 FPS Rating: $rating"
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun countDroppedFrames(): Long {
        // Упрощенная оценка пропущенных кадров на основе времени выполнения
        var dropped = 0L
        try {
            val startTime = System.nanoTime()
            Thread.sleep(500) // Измеряем за полсекунды
            val endTime = System.nanoTime()
            
            val duration = (endTime - startTime) / 1_000_000 // в миллисекундах
            val expectedFrames = (duration / FRAME_TIME_THRESHOLD).toLong()
            val actualFrames = 30 // 60 FPS за полсекунды
            
            dropped = maxOf(0, expectedFrames - actualFrames)
        } catch (e: Exception) {
            logError("Failed to estimate dropped frames: ${e.message}")
        }
        return dropped
    }

    private fun rateScrollPerformance(droppedFrames: Long): String {
        val rating = when {
            droppedFrames < 5 -> "⭐⭐⭐⭐⭐ Excellent (< 5 dropped frames)"
            droppedFrames < 10 -> "⭐⭐⭐⭐ Very Good (< 10 dropped frames)"
            droppedFrames < 20 -> "⭐⭐⭐ Good (< 20 dropped frames)"
            droppedFrames < 30 -> "⭐⭐ Fair (< 30 dropped frames)"
            else -> "⭐ Needs Improvement (> 30 dropped frames)"
        }
        return "🎞️ Scroll Rating: $rating"
    }

    private fun ratePerformance(timeMs: Long, type: String): String {
        val rating = when (type) {
            "startup" -> when {
                timeMs < 1000 -> "⭐⭐⭐⭐⭐ Excellent (< 1s)"
                timeMs < 2000 -> "⭐⭐⭐⭐ Very Good (< 2s)"
                timeMs < 3000 -> "⭐⭐⭐ Good (< 3s)"
                timeMs < 4000 -> "⭐⭐ Fair (< 4s)"
                else -> "⭐ Needs Improvement (> 4s)"
            }
            "navigation" -> when {
                timeMs < 300 -> "⭐⭐⭐⭐⭐ Excellent (< 300ms)"
                timeMs < 500 -> "⭐⭐⭐⭐ Very Good (< 500ms)"
                timeMs < 700 -> "⭐⭐⭐ Good (< 700ms)"
                timeMs < 1000 -> "⭐⭐ Fair (< 1s)"
                else -> "⭐ Needs Improvement (> 1s)"
            }
            "list_load", "details_load" -> when {
                timeMs < 500 -> "⭐⭐⭐⭐⭐ Excellent (< 500ms)"
                timeMs < 1000 -> "⭐⭐⭐⭐ Very Good (< 1s)"
                timeMs < 1500 -> "⭐⭐⭐ Good (< 1.5s)"
                timeMs < 2000 -> "⭐⭐ Fair (< 2s)"
                else -> "⭐ Needs Improvement (> 2s)"
            }
            "search" -> when {
                timeMs < 100 -> "⭐⭐⭐⭐⭐ Excellent (< 100ms)"
                timeMs < 200 -> "⭐⭐⭐⭐ Very Good (< 200ms)"
                timeMs < 300 -> "⭐⭐⭐ Good (< 300ms)"
                timeMs < 500 -> "⭐⭐ Fair (< 500ms)"
                else -> "⭐ Needs Improvement (> 500ms)"
            }
            else -> "N/A"
        }
        return "⏱️ Time Rating: $rating"
    }

    private fun rateMemoryUsage(memoryIncreaseMB: Long): String {
        val rating = when {
            memoryIncreaseMB < 5 -> "⭐⭐⭐⭐⭐ Excellent (< 5MB)"
            memoryIncreaseMB < 10 -> "⭐⭐⭐⭐ Very Good (< 10MB)"
            memoryIncreaseMB < 20 -> "⭐⭐⭐ Good (< 20MB)"
            memoryIncreaseMB < 30 -> "⭐⭐ Fair (< 30MB)"
            else -> "⭐ Needs Improvement (> 30MB)"
        }
        return "💾 Memory Rating: $rating"
    }
}
