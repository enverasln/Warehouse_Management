package tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.feature_common.databinding.DialogFragmentDocumentSeriesNumberBinding
import java.util.Calendar

class DocumentSeriesNumberDialogFragment(private val dialogListener: DocumentSeriesNumberDialogListener) : DialogFragment() {
    private var _binding: DialogFragmentDocumentSeriesNumberBinding? = null
    private val binding get() = _binding!!

    private var initialDocumentSeries: String? = null
    private var initialDocumentNumber: String? = null

    private var _datePicker: MaterialDatePicker<Long>? = null
    private val datePicker get() = _datePicker!!

    private var warehouseLockDateMillis: Long? = null
    private var positiveButton: Button? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
        _binding = DialogFragmentDocumentSeriesNumberBinding.inflate(layoutInflater)

        initialDocumentSeries?.let { _binding?.documentSeriesEditText?.setText(it) }
        initialDocumentNumber?.let { _binding?.documentSeriesNumberEditText?.setText(it) }

        val alertDialog = AlertDialog.Builder(requireContext()).setTitle("Belge No Girişi")
            .setView(binding.root)
            .setPositiveButton("Tamam", null)
            .setNegativeButton("Vazgeç", null)
            .setCancelable(false)
            .create()

        alertDialog.setOnShowListener {
            positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                text = "Tamam"
                setOnClickListener {
                    if (!validateDate()) return@setOnClickListener
                    val documentDate = binding.documentDateEditText.text.toString()
                    val documentSeries = binding.documentSeriesEditText.text.toString()
                    val documentNumber = binding.documentSeriesNumberEditText.text.toString().toIntOrNull() ?: 0
                    val paperNumber = binding.paperNumberEditText.text.toString()

                    dialogListener.onPositiveClick(
                        documentDate = documentDate,
                        documentSeries = documentSeries,
                        documentNumber = documentNumber,
                        paperNumber = paperNumber
                    )
                }
            }

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                text = "İptal"
                setOnClickListener { dialogListener.onNegativeClick(); dismiss() }
            }
        }

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.apply {

            documentDateEditText.setText(DateConverter.timestampToUi(System.currentTimeMillis()))

            datePickerButton.setOnClickListener {
                if (_datePicker == null) {
                    _datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Belge Tarihini Seçiniz")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build()
                }

                if (datePicker.isAdded) return@setOnClickListener
                datePickerButton.isEnabled = false

                datePicker.addOnPositiveButtonClickListener { selectedDate ->
                    val uiDate = DateConverter.timestampToUi(selectedDate)
                    documentDateEditText.setText(uiDate)
                    documentDateEditText.setSelection(uiDate.length)
                    positiveButton?.isEnabled = validateDate()
                    datePickerButton.isEnabled = true
                }

                datePicker.addOnDismissListener { datePickerButton.isEnabled = true }
                datePicker.show(parentFragmentManager, "SELECT_DATE")
            }

            documentDateEditText.addTextChangedListener(object : TextWatcher {
                private var selfChange = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (selfChange || s.isNullOrEmpty()) return
                    val formattedDate = formatInputAsDate(s.toString())
                    if (formattedDate != s.toString()) {
                        selfChange = true
                        documentDateEditText.setText(formattedDate)
                        documentDateEditText.setSelection(formattedDate.length)
                        selfChange = false
                    }
                    positiveButton?.isEnabled = validateDate()
                }

            })
        }
        return binding.root
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

    private fun validateDate(): Boolean {
        val dateStr = binding.documentDateEditText.text?.toString()?.trim().orEmpty()
        if (dateStr.isEmpty()) {
            binding.documentDateInputLayout.error = "Tarih zorunludur"
            return false
        }

        val selected = DateConverter.uiToTimestamp(dateStr).takeIf { it > 0 }
        if (selected == null) {
            binding.documentDateInputLayout.error = "Geçersiz tarih"
            return false
        }

        val lock = warehouseLockDateMillis?.takeIf { it > 0 } ?: run {
            binding.documentDateInputLayout.error = null
            return true
        }

        val selectedDay = startOfDay(selected)
        val lockDay = startOfDay(lock)

        return if (selectedDay < lockDay) {
            binding.documentDateEditText.error =
                "Belge tarihi, depo kilit tarihinden (" + DateConverter.timestampToUi(lockDay) + ") sonra olmalı."
            false
        } else {
            binding.documentDateEditText.error = null
            true
        }
    }

    fun setDocumentSeries(documentSeries: String) {
        initialDocumentSeries = documentSeries
    }

    fun setDocumentNumber(documentNumber: String) {
        initialDocumentNumber = documentNumber
    }

    fun setWarehouseLockDateMillis(millis: Long?) {
        warehouseLockDateMillis = millis?.takeIf { it > 0 }
    }

    private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun setDocumentOrderError(message: String) {
        binding.documentSeriesNumberEditText.error = message
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    interface DocumentSeriesNumberDialogListener {
        fun onPositiveClick(documentDate: String, documentSeries: String, documentNumber: Int, paperNumber: String)
        fun onNegativeClick()
    }
}