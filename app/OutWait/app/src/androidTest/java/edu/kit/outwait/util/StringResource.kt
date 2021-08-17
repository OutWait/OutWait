package edu.kit.outwait.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

object StringResource {
     fun getResourceString(id: Int): String? {
        val targetContext: Context =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        return targetContext.resources.getString(id)
    }
}
