package com.sipos.kebabsk.common

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrisTransportSecurityContractTest {
    @Test
    fun networkLoggingNeverIncludesBodiesOrUnredactedAuthorization() {
        val source = projectFile("app/src/main/java/com/sipos/kebabsk/di/AppModule.kt").readText()

        assertFalse(source.contains("HttpLoggingInterceptor.Level.BODY"))
        assertTrue(source.contains("HttpLoggingInterceptor.Level.BASIC"))
        assertTrue(source.contains("redactHeader(\"Authorization\")"))
        assertTrue(source.contains("if (BuildConfig.DEBUG)"))
    }

    @Test
    fun releaseBuildRequiresHttpsAndDisablesCleartext() {
        val gradle = projectFile("app/build.gradle.kts").readText()
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()

        assertTrue(gradle.contains("normalizedReleaseUrl.startsWith(\"https://\""))
        assertTrue(gradle.contains("manifestPlaceholders[\"usesCleartextTraffic\"] = false"))
        assertTrue(manifest.contains("android:usesCleartextTraffic=\"\${usesCleartextTraffic}\""))
    }

    private fun projectFile(relativePath: String): File {
        var directory = File(System.getProperty("user.dir") ?: ".")
        while (true) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate
            directory = directory.parentFile ?: break
        }

        error("Project file not found: $relativePath")
    }
}
