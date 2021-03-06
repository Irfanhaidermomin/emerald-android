/*
 * Copyright 2019 CondorLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.condorlabs.customcomponents.customcheckbox

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import co.condorlabs.customcomponents.*
import co.condorlabs.customcomponents.customedittext.ValueChangeListener
import co.condorlabs.customcomponents.formfield.FormField
import co.condorlabs.customcomponents.formfield.Selectable
import co.condorlabs.customcomponents.formfield.ValidationResult

abstract class BaseCheckboxFormField(context: Context, attrs: AttributeSet) :
    TextInputLayout(context, attrs), FormField<List<Selectable>>, CompoundButton.OnCheckedChangeListener {

    protected var mValueChangeListener: ValueChangeListener<List<Selectable>>? = null

    private var mSelectables: List<Selectable>? = null
    private var mLabelText: String? = EMPTY

    private val mTVLabel = TextView(context, attrs).apply {
        id = R.id.tvLabel
    }

    private val mLayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.BaseCheckboxFormField,
            DEFAULT_STYLE_ATTR, DEFAULT_STYLE_RES
        )
        isRequired = typedArray.getBoolean(R.styleable.BaseCheckboxFormField_is_required, false)
        mLabelText = typedArray.getString(R.styleable.BaseCheckboxFormField_title)

        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setup()
    }

    override fun getErrorValidateResult(): ValidationResult {
        return ValidationResult(false, String.format(MESSAGE_FORMAT_ERROR, mLabelText))
    }

    override fun isValid(): ValidationResult {
        when {
            isRequired -> {
                if (mSelectables?.filter { !it.value }?.size ?: ZERO == mSelectables?.size ?: ZERO) {
                    return getErrorValidateResult()
                } else {
                    clearError()
                }
            }
        }
        return ValidationResult(true, EMPTY)
    }

    override fun showError(message: String) {
        this.isErrorEnabled = true
        this.error = message
    }

    override fun clearError() {
        this.isErrorEnabled = false
        this.error = EMPTY
    }

    override fun setup() {
        mLabelText?.let {
            mTVLabel.text = it
        }
    }

    override fun getValue(): List<Selectable> {
        return mSelectables ?: arrayListOf()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val checkbox = buttonView?.let { it } ?: return

        val selectableChecked = mSelectables?.find {
            it.label == checkbox.text
        }?.let { it } ?: return

        selectableChecked.value = !selectableChecked.value

        val isValid = isValid()

        if (isValid.error.isNotEmpty()) {
            showError(isValid.error)
        } else {
            clearError()
        }

        val selectables = mSelectables?.let { it } ?: return

        mValueChangeListener?.onValueChange(selectables)
    }

    override fun setValueChangeListener(valueChangeListener: ValueChangeListener<List<Selectable>>) {
        mValueChangeListener = valueChangeListener
    }

    fun setSelectables(selectables: List<Selectable>) {
        mSelectables = selectables
        addCheckboxes()
    }

    private fun setPaddingTop(): Int {
        val paddingDp = PADDING_TOP
        val density = context.resources.displayMetrics.density
        return (paddingDp * density).toInt()
    }

    private fun setFont() {
        val font = Typeface.createFromAsset(context.assets, OPEN_SANS_SEMI_BOLD)
        typeface = font
    }

    private fun setStyleCheckBox(): ColorStateList {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.defaultButtonBorderColor),
            ContextCompat.getColor(context, R.color.primaryColor)
        )

        return ColorStateList(states, colors)
    }

    private fun addCheckboxes() {
        removeAllViews()
        setFont()
        addView(mTVLabel, mLayoutParams)
        mSelectables?.forEachIndexed { index, selectable ->
            addView(CheckBox(context).apply {
                id = index
                text = selectable.label
                isChecked = selectable.value
                gravity = Gravity.TOP
                setPadding(
                    DEFAULT_PADDING, setPaddingTop(),
                    DEFAULT_PADDING,
                    DEFAULT_PADDING
                )
                buttonTintList = setStyleCheckBox()
                setOnCheckedChangeListener(this@BaseCheckboxFormField)
            }, mLayoutParams)
        }
    }
}
