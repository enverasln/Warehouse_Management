package tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
        _binding = DialogFragmentDocumentSeriesNumberBinding.inflate(layoutInflater)
        initialDocumentSeries?.let {
            _binding?.documentSeriesEditText?.setText(it)
        }

        initialDocumentNumber?.let {
            _binding?.documentSeriesNumberEditText?.setText(it)
        }


        val alertDialog = AlertDialog.Builder(requireContext()).setTitle("Belge No Girişi").setView(binding.root).setPositiveButton("Tamam") { _, _ ->

        }.setNegativeButton("Vazgeç") { _, _ ->
            dialogListener.onNegativeClick()
            dismiss()
        }.setCancelable(false).create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.text = "Tamam"
            positiveButton.setOnClickListener {
                val documentDate = binding.documentDateEditText.text.toString()
                val documentSeries = binding.documentSeriesEditText.text.toString()
                val documentNumber = binding.documentSeriesNumberEditText.text.toString().toIntOrNull()
                val paperNumber = binding.paperNumberEditText.text.toString()
                dialogListener.onPositiveClick(
                    documentDate = documentDate, documentSeries = documentSeries, documentNumber = documentNumber ?: 0, paperNumber = paperNumber
                )
            }

            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.text = "İptal"
            negativeButton.setOnClickListener {
                dialogListener.onNegativeClick()
                dismiss()
            }
        }

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding.apply {

            documentDateEditText.setText(DateConverter.timestampToUi(Calendar.getInstance().timeInMillis))

            datePickerButton.setOnClickListener {
                if (_datePicker == null) _datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Belge Tarihini Seçiniz")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
                if (datePicker.isAdded) return@setOnClickListener

                datePickerButton.isEnabled = false

                datePicker.addOnPositiveButtonClickListener { selectedDate ->
                    val selectedDateStr = DateConverter.timeStampToApi(selectedDate)
                    documentDateEditText.setText(selectedDateStr)
                    datePickerButton.isEnabled = true
                }

                datePicker.addOnDismissListener {
                    datePickerButton.isEnabled = true
                }

                datePicker.show(parentFragmentManager, "SELECT_DATE")
            }

            documentDateEditText.addTextChangedListener { s ->
                if (s.isNullOrEmpty()) return@addTextChangedListener
                val formattedDate = formatInputAsDate(s.toString())
                documentDateEditText.setText(formattedDate)
                documentDateEditText.setSelection(formattedDate.length)
            }
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

    fun setDocumentSeries(documentSeries: String) {
        initialDocumentSeries = documentSeries
    }

    fun setDocumentOrderError(message: String) {
        binding.documentSeriesNumberEditText.error = message
    }

    fun setDocumentNumber(documentNumber: String) {
        initialDocumentNumber = documentNumber
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    interface DocumentSeriesNumberDialogListener {
        fun onPositiveClick(
            documentDate: String, documentSeries: String, documentNumber: Int, paperNumber: String
        )

        fun onNegativeClick()
    }
}