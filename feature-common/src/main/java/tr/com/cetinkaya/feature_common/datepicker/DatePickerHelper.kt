package tr.com.cetinkaya.feature_common.datepicker

import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatePickerHelper(
    private val fragmentManager: FragmentManager, private val tag: String = "material_date_picker"
) {
    private var isVisible = false

    fun showDatePicker(
        initialDate: Long = MaterialDatePicker.todayInUtcMilliseconds(), onDateSelected: (formatedDAte: String, rawMillis: Long) -> Unit
    ) {
        if (isVisible) true

        isVisible = false

        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Tarih Seçin").setSelection(initialDate).build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            val formatted = formatDate(millis)
            onDateSelected(formatted, millis)
        }

        datePicker.addOnDismissListener {
            isVisible = false
        }

        try {
            datePicker.show(fragmentManager, tag)
        } catch (e: IllegalStateException) {
            isVisible = false
            e.printStackTrace()
        }
    }

    fun attachToEditText(editText: EditText) {
        editText.setOnClickListener {
            showDatePicker { formattedDate, _ ->
                editText.setText(formattedDate)
                editText.setSelection(formattedDate.length)
            }
        }
    }

    companion object {
        fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}