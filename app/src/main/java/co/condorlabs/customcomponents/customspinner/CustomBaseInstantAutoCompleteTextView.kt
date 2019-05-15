package co.condorlabs.customcomponents.customspinner

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.AutoCompleteTextView

/**
 * @author Oscar Gallon on 2019-05-14.
 */
class CustomBaseInstantAutoCompleteTextView(context: Context, attrs: AttributeSet) :
    AutoCompleteTextView(context, attrs) {

    init {
        isFocusable = false
    }

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        performFiltering(text, 0)
        showDropDown()

        return super.onTouchEvent(event)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && adapter != null) {
            performFiltering(text, 0)
        }
    }
}