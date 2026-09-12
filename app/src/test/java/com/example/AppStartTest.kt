package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric

@RunWith(RobolectricTestRunner::class)
class AppStartTest {
    @Test
    fun testAppStart() {
        val activity = Robolectric.setupActivity(MainActivity::class.java)
        assert(activity != null)
    }
}
