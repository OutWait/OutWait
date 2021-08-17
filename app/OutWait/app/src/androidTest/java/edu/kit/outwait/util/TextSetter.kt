package edu.kit.outwait.util

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf


object TextSetter {
    fun setTextEditText(

        newText: String?
    ): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(com.lukelorusso.codeedittext.CodeEditText::class.java))
            }

            override fun getDescription(): String {
                return "Update the text from the custom EditText"
            }

            override fun perform(uiController: UiController, view: View) {
                if (newText != null) {
                    (view as com.lukelorusso.codeedittext.CodeEditText).text=newText
                }
            }
        }
    }
}
