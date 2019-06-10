package co.condorlabs.customcomponents.test.util

import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import co.condorlabs.customcomponents.*
import co.condorlabs.customcomponents.customedittext.BaseEditTextFormField
import co.condorlabs.customcomponents.customtextview.CustomTextView
import junit.framework.Assert
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher

fun isTextDisplayed(resourceId: Int) {
    var isDisplayed = true
    Espresso.onView(ViewMatchers.withText(resourceId))
        .withFailureHandler { error, _ ->
            isDisplayed = error is AmbiguousViewMatcherException
        }
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    Assert.assertTrue(isDisplayed)
}

fun isTextDisplayed(text: String?) {
    var isDisplayed = true
    Espresso.onView(ViewMatchers.withSubstring(text))
        .withFailureHandler { error, _ ->
            isDisplayed = error is AmbiguousViewMatcherException
        }
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    Assert.assertTrue(isDisplayed)
}

fun clickWithId(id: Int) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(ViewActions.click())
}

fun clickWithText(text: String) {
    Espresso.onView(ViewMatchers.withSubstring(text))
        .perform(ViewActions.click())
}

fun isTextInLines(lines: Int): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            return (item as TextView).lineCount == lines
        }

        override fun describeTo(description: Description) {
            description.appendText("isTextInLines")
        }
    }
}

fun BaseEditTextFormField.text(): String {
    return this.editText?.text.toString()
}

fun clickDrawable(): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return CLICK_DRAWABLE_DESCRIPTION
        }

        override fun getConstraints(): Matcher<View> {
            return allOf(
                isAssignableFrom(TextView::class.java),
                object : BoundedMatcher<View, TextView>(TextView::class.java) {
                    override fun matchesSafely(aTextView: TextView): Boolean {
                        if (aTextView.requestFocusFromTouch())
                            for (drawableItem in aTextView.compoundDrawables)
                                if (drawableItem != null)
                                    return true

                        return false
                    }

                    override fun describeTo(description: Description) {
                        description.appendText(CLICK_DRAWABLE_DESCRIPTION_APPEND)
                    }
                })
        }

        override fun perform(uiController: UiController?, view: View?) {
            val aTextView = view as TextView
            if (aTextView != null && aTextView.requestFocusFromTouch()) {
                val drawables = aTextView.compoundDrawables

                val tvLocation = Rect()
                aTextView.getHitRect(tvLocation)

                val textViewBounds = arrayOfNulls<Point>(TEXT_VIEW_BOUNDS_SIDES)
                textViewBounds[0] = Point(tvLocation.left, tvLocation.centerY())
                textViewBounds[1] = Point(tvLocation.centerX(), tvLocation.top)
                textViewBounds[2] = Point(tvLocation.right, tvLocation.centerY())
                textViewBounds[3] = Point(tvLocation.centerX(), tvLocation.bottom)

                for (location in DRAWABLE_START_INDEX..DRAWABLE_END_INDEX)
                    if (drawables[location] != null) {
                        val bounds = drawables[location].bounds
                        textViewBounds[location]?.offset(
                            bounds.width() / 2,
                            bounds.height() / 2
                        )
                        if (aTextView.dispatchTouchEvent(
                                MotionEvent.obtain(
                                    android.os.SystemClock.uptimeMillis(),
                                    android.os.SystemClock.uptimeMillis(),
                                    MotionEvent.ACTION_DOWN,
                                    textViewBounds[location]?.x?.toFloat() ?: ZERO_FLOAT,
                                    textViewBounds[location]?.y?.toFloat() ?: ZERO_FLOAT,
                                    ZERO
                                )
                            )
                        )
                            aTextView.dispatchTouchEvent(
                                MotionEvent.obtain(
                                    android.os.SystemClock.uptimeMillis(),
                                    android.os.SystemClock.uptimeMillis(),
                                    MotionEvent.ACTION_UP,
                                    textViewBounds[location]?.x?.toFloat() ?: ZERO_FLOAT,
                                    textViewBounds[location]?.y?.toFloat() ?: ZERO_FLOAT,
                                    ZERO
                                )
                            )
                    }
            }
        }
    }
}

fun setNumberPickerValue(value: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return NUMBER_PICKER_VALUE_SETTER_DESCRIPTION
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(NumberPicker::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            (view as NumberPicker).value = value
        }
    }
}

fun customWithHint(matcher: Matcher<String>): Matcher<View> =
    object : BoundedMatcher<View, BaseEditTextFormField>(BaseEditTextFormField::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with hint: ")
            matcher.describeTo(description)
        }

        override fun matchesSafely(item: BaseEditTextFormField): Boolean {
            val parent = item.parent.parent
            return if (parent is BaseEditTextFormField) matcher.matches(
                parent.hint
            ) else matcher.matches(item.hint)
        }
    }

fun fontSizeMatcher(size: Float, font: Typeface): Matcher<View> =
    object : BoundedMatcher<View, CustomTextView>(CustomTextView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with size: ").appendText(size.toString())
    }

    override fun matchesSafely(item: CustomTextView): Boolean {
        return item.textSize == size && item.typeface == font
    }
}
