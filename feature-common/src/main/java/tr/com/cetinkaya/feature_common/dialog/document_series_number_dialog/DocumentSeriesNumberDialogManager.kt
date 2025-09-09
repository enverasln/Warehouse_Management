package tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog

import androidx.fragment.app.Fragment

class DocumentSeriesNumberDialogManager(
    private val fragment: Fragment,
    private val onPositive: (String, String, Int, String) -> Unit,
    private val onNegative: () -> Unit,
    private val onDocumentNumberChanged: ((documentSeries: String, documentNumber: Int) -> Unit)? = null
) {
    private var dialog: DocumentSeriesNumberDialogFragment? = null

    fun showDialog(
        documentSeries: String,
        documentNumber: Int? = null,
        warehouseLockDateMillis: Long? = null
    ) {
        dialog = DocumentSeriesNumberDialogFragment(object : DocumentSeriesNumberDialogFragment.DocumentSeriesNumberDialogListener {

            override fun onPositiveClick(
                documentDate: String, documentSeries: String, documentNumber: Int, paperNumber: String
            ) {
                onPositive(documentDate, documentSeries, documentNumber, paperNumber)
            }

            override fun onNegativeClick() {
                onNegative()
            }

            override fun onDocumentNumberEditTextChanged(documentSeries: String, documentNumber: Int) {
                onDocumentNumberChanged?.invoke(documentSeries, documentNumber)
            }

        }).apply {
            setDocumentSeries(documentSeries)
            setDocumentNumber(documentNumber?.toString() ?: "")
            setWarehouseLockDateMillis(warehouseLockDateMillis)
            show(fragment.childFragmentManager, "DocumentSeriesNumberDialog")
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun setPaperNumberOnDialog(paperNumber: String?) {
        dialog?.setPaperNumber(paperNumber)
    }

    fun setBlockingErrorOnDialog(message: String?) {
        dialog?.setBlockingError(message)
    }
}

