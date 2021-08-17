package edu.kit.outwait.util

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

object AppointmentSetter {
    fun setAppointment(
        hourTime: Int?,
        minuteTime: Int?
    ): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return Matchers.allOf(
                    ViewMatchers.isDisplayed(),
                    ViewMatchers.isAssignableFrom(ru.ifr0z.timepickercompact.TimePickerCompact::class.java)
                )
            }

            override fun getDescription(): String {
                return "Update the text from the custom appointmentTime"
            }

            override fun perform(uiController: UiController, view: View) {
                if (hourTime != null) {
                    (view as ru.ifr0z.timepickercompact.TimePickerCompact).hour = hourTime
                }
                if (minuteTime != null) {
                    (view as ru.ifr0z.timepickercompact.TimePickerCompact).minute = minuteTime
                }

            }
        }
    }
}
