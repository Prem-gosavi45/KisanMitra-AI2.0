package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class AppLaunchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppLaunch() {
        composeTestRule.setContent {
            KisanMitraApp()
        }
        
        composeTestRule.waitForIdle()
        
        // Let's force it to wait for the landing screen if needed
        val tree = composeTestRule.onRoot().printToString()
        File("app_tree_output.txt").writeText(tree)
    }
}
