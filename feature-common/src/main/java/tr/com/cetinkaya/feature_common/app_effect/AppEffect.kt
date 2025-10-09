package tr.com.cetinkaya.feature_common.app_effect

sealed interface AppEffect {
    data class ShowError(val message: String, val durationMs: Long = 3000L) : AppEffect
    data class ShowSuccess(val message: String, val durationMs: Long = 3000L) : AppEffect
    data class ShowConfirmDialog(
        val id: String,
        val title: String? = null,
        val message: String,
        val positiveText: String = "Evet",
        val negativeText: String = "Hayır",
        val cancelable: Boolean = false
    ) : AppEffect

    data class ShowWarningDialog(
        val id: String,
        val title: String? = null,
        val message: String,
        val buttonText: String = "Tamam",
        val cancelable: Boolean = false
    ) : AppEffect

    data class ShowConfirmDialogWithCheckBox(
        val id: String,
        val title: String?,
        val message: String,
        val checkLabel: String,
        val positiveButtonText: String = "Evet",
        val negativeButtonText: String = "Hayır",
        val cancelable: Boolean = false
    ) : AppEffect
}