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

package co.condorlabs.customcomponents.customspinner

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import co.condorlabs.customcomponents.R
import co.condorlabs.customcomponents.helper.STATE_SPINNER_HINT_POSITION
import co.condorlabs.customcomponents.helper.ZERO

/**
 * @author Oscar Gallon on 2/26/19.
 */
class StateSpinnerAdapter(
    context: Context, resourceId: Int,
    private var mHint: String = context.getString(R.string.spinner_default_hint),
    private val mStates: ArrayList<String> = ArrayList()
) : ArrayAdapter<String>(context, resourceId, mStates) {


    fun replaceStates(states: List<String>) {
        mStates.clear()
        mStates.add(mHint)
        mStates.addAll(states)
        notifyDataSetChanged()
    }

    override fun isEnabled(position: Int): Boolean {
        return !isHintPosition(position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val optionTextView = (view as? TextView)?.let { it } ?: return view

        optionTextView.setTextColor(
            if (isHintPosition(position)) {
                Color.GRAY
            } else {
                Color.BLACK
            }
        )

        return optionTextView
    }

    private fun isHintPosition(position: Int): Boolean = position == STATE_SPINNER_HINT_POSITION
}
