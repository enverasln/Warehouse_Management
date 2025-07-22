package tr.com.cetinkaya.feature_common.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateMaskWatcher(private val editText: EditText) : TextWatcher {

    private var isUpdating = false


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s.isNullOrEmpty()) return

        isUpdating = true
        val formattedDate = formatInputAsDate(s.toString())

        editText.setText(formattedDate)
        editText.setSelection(formattedDate.length)
        isUpdating = false
    }

    private fun formatInputAsDate(input: String): String {
        val digits = input.replace(Regex("\\D"), "") // Sadece sayıları al
        val sb = StringBuilder()

        for (i in digits.indices) {
            if (i == 2 || i == 4) sb.append(".") // Otomatik olarak "." ekler
            sb.append(digits[i])
        }

        return sb.toString().take(10) // Maksimum 10 karakter olacak şekilde sınırlandır
    }
}