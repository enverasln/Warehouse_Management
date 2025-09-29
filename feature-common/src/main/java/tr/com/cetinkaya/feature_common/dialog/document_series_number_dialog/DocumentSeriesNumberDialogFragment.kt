package tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.feature_common.databinding.DialogFragmentDocumentSeriesNumberBinding
import java.util.Calendar

class DocumentSeriesNumberDialogFragment(private val dialogListener: DocumentSeriesNumberDialogListener) : DialogFragment() {
    interface DocumentSeriesNumberDialogListener {
        fun onPositiveClick(documentDate: String, documentSeries: String, documentNumber: Int, paperNumber: String)
        fun onNegativeClick()
        fun onDocumentNumberEditTextChanged(documentSeries: String, documentNumber: Int)
    }

    private var _binding: DialogFragmentDocumentSeriesNumberBinding? = null
    private val binding get() = _binding!!
    private var initialDocumentSeries: String? = null
    private var initialDocumentNumber: String? = null
    private var _datePicker: MaterialDatePicker<Long>? = null
    private val datePicker get() = _datePicker!!
    private var warehouseLockDateMillis: Long? = null
    private var positiveButton: Button? = null
    private var blockingErrorMsg: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
        _binding = DialogFragmentDocumentSeriesNumberBinding.inflate(layoutInflater)

        initialDocumentSeries?.let { _binding?.documentSeriesEditText?.setText(it) }
        initialDocumentNumber?.let { _binding?.documentSeriesNumberEditText?.setText(it) }

        val alertDialog = AlertDialog.Builder(requireContext()).setTitle("Belge No Girişi").setView(binding.root).setPositiveButton("Tamam", null)
            .setNegativeButton("Vazgeç", null).setCancelable(false).create()

        alertDialog.setOnShowListener {
            positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                text = "Tamam"
                setOnClickListener {
                    if (!validateAll(showDialogIfInvalid = true)) return@setOnClickListener
                    val documentDate = binding.documentDateEditText.text.toString()
                    val documentSeries = binding.documentSeriesEditText.text.toString()
                    val documentNumber = binding.documentSeriesNumberEditText.text.toString().toIntOrNull() ?: 0
                    val paperNumber = binding.paperNumberEditText.text.toString()

                    dialogListener.onPositiveClick(
                        documentDate = documentDate, documentSeries = documentSeries, documentNumber = documentNumber, paperNumber = paperNumber
                    )
                }
            }

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                text = "İptal"
                setOnClickListener { dialogListener.onNegativeClick(); dismiss() }
            }

            updatePositiveEnabled()
        }

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.apply {

            documentDateEditText.setText(DateConverter.timestampToUi(System.currentTimeMillis()))

            datePickerButton.setOnClickListener {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Belge Tarihini Seçiniz")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                datePickerButton.isEnabled = false

                picker.addOnPositiveButtonClickListener { selectedDate ->
                    val uiDate = DateConverter.timestampToUi(selectedDate)
                    documentDateEditText.setText(uiDate)
                    documentDateEditText.setSelection(uiDate.length)
                    validateDate(showDialogIfInvalid = true)
                    updatePositiveEnabled()
                    datePickerButton.isEnabled = true
                }

                picker.addOnDismissListener { datePickerButton.isEnabled = true }
                picker.show(parentFragmentManager, "SELECT_DATE")
            }

            documentDateEditText.addTextChangedListener(object : TextWatcher {
                private var selfChange = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (selfChange || s == null) return
                    val formattedDate = formatInputAsDate(s.toString())
                    if (formattedDate != s.toString()) {
                        selfChange = true
                        documentDateEditText.setText(formattedDate)
                        documentDateEditText.setSelection(formattedDate.length)
                        selfChange = false
                    }
                    validateDate(showDialogIfInvalid = true)
                    updatePositiveEnabled()
                }
            })


            binding.documentDateEditText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    validateDate(showDialogIfInvalid = true) // uyarı dialogu burada açılır
                    updatePositiveEnabled()
                }
            }

            binding.documentDateEditText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    v.clearFocus()
                    paperNumberEditText.requestFocus()
                    true
                } else false
            }

            documentSeriesNumberEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val documentNumber = documentSeriesNumberEditText.text?.toString()?.toIntOrNull()
                    val documentSeries = documentSeriesEditText.text?.toString() ?: return@OnFocusChangeListener
                    if (documentNumber != null) {
                        dialogListener.onDocumentNumberEditTextChanged(documentSeries, documentNumber)
                        paperNumberEditText.requestFocus()
                    }
                }
            }

            documentSeriesNumberEditText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    v.clearFocus() // onFocusChange tetikler
                    true
                } else false
            }
        }
        return binding.root
    }

    private fun validateAll(showDialogIfInvalid: Boolean = false): Boolean = validateDate(showDialogIfInvalid) && blockingErrorMsg.isNullOrBlank()

    private fun validateDate(showDialogIfInvalid: Boolean = false): Boolean {
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
            binding.documentDateInputLayout.error = "Belge tarihi, depo kilit tarihinden (" + DateConverter.timestampToUi(lockDay) + ") küçük olamaz."
            if (showDialogIfInvalid) showDateBeforeLockWarning(lockDay)
            false
        } else {
            binding.documentDateInputLayout.error = null
            true
        }
    }

    private fun showDateBeforeLockWarning(lockDay: Long) {
        AlertDialog.Builder(requireContext()).setTitle("Tarih Uyarısı")
            .setMessage("Belge tarihi depo kilit tarihinden (" + DateConverter.timestampToUi(lockDay) + ") küçük olamaz.")
            .setPositiveButton("Tamam", null).setOnDismissListener {
                refocusDateField()
                updatePositiveEnabled()
            }.show()
    }

    private fun updatePositiveEnabled() {
        positiveButton?.isEnabled = validateAll()
    }

    fun setPaperNumber(paperNumber: String?) {
        binding.paperNumberEditText.setText(paperNumber)
        binding.paperNumberEditText.setSelection(paperNumber?.length ?: 0)
    }

    fun setBlockingError(message: String?) {


        if (message.isNullOrBlank()) {
            blockingErrorMsg = null
            updatePositiveEnabled()
            return
        }
        blockingErrorMsg = message
        AlertDialog.Builder(requireContext()).setTitle("Dikkat").setMessage(message).setPositiveButton("Tamam", null).setOnDismissListener {
            binding.documentSeriesNumberEditText.requestFocus()
            updatePositiveEnabled()
        }.show()
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

    private fun refocusDateField() {
        binding.documentDateEditText.requestFocus()
        binding.documentDateEditText.post {
            binding.documentDateEditText.setSelection(binding.documentDateEditText.text?.length ?: 0)
            showKeyboard(binding.documentDateEditText)
        }
    }

    private fun showKeyboard(view: View) {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}