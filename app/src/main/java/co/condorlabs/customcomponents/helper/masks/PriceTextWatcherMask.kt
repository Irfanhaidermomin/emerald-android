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

package co.condorlabs.customcomponents.helper.masks

import android.text.Editable
import android.widget.EditText
import co.condorlabs.customcomponents.helper.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * @author Oscar Gallon on 12/20/18.
 */
class PriceTextWatcherMask(private val receiver: EditText) : TextWatcherAdapter() {

    private val formatter = NumberFormat.getNumberInstance(Locale.US)
    private var previousText: String = EMPTY

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        super.beforeTextChanged(s, start, count, after)
        previousText = s.toString()
    }

    override fun afterTextChanged(s: Editable?) {
        s?.toString()
            ?.let { text ->
                receiver.removeTextChangedListener(this)
                try {
                    if (text == "$" || text.isEmpty()) {
                        setSelectionAndListener()
                        return
                    }
                    val currentlyAmount = getPriceFromCurrency(text)

                    if (!isAllowedAmount(currentlyAmount)) {
                        receiver.setText(previousText)
                        setSelectionAndListener()
                        return
                    }

                    if (text.last() == '.') {
                        if (isMaxAmount(currentlyAmount)) {
                            receiver.setText(MONEY_MAX_AMOUNT.toDollarAmount())
                            setSelectionAndListener()
                            return
                        }
                        receiver.setText(text)
                        setSelectionAndListener()
                        return
                    }

                    if (text.last() == '0') {
                        when {
                            previousText.isEmpty() -> receiver.setText(text.toBigDecimal().toDollarAmount())
                            previousText.last() == '.' -> receiver.setText(text)
                            else -> {
                                receiver.setText(currentlyAmount.toDollarAmount())
                            }
                        }
                        setSelectionAndListener()
                        return
                    }
                    receiver.setText(currentlyAmount.toDollarAmount())
                } catch (exception: Throwable) {
                    receiver.setText(previousText)
                }
                setSelectionAndListener()
            }
    }

    private fun getPriceFromCurrency(text: String): BigDecimal {
        val df = DecimalFormat(MONEY_TWO_DECIMALS)
        df.roundingMode = RoundingMode.FLOOR
        val amount = formatter.parse(text.replace(NON_NUMERICAL_SYMBOLS.toRegex(), ""))

        return df.format(amount).replace(COMMA_AS_DECIMAL, ".").toBigDecimal()
    }

    private fun setSelectionAndListener() {
        receiver.setSelection(receiver.text.length)
        receiver.addTextChangedListener(this)
    }

    private fun isAllowedAmount(amount: Number): Boolean {
        return amount as BigDecimal <= MONEY_MAX_AMOUNT
    }

    private fun isMaxAmount(amount: BigDecimal): Boolean {
        return amount.compareTo(MONEY_MAX_AMOUNT) == 0
    }
}
