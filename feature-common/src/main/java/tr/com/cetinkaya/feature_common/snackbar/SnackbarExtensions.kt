package tr.com.cetinkaya.feature_common.snackbar

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import tr.com.cetinkaya.feature_common.R

fun View.showSuccessSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    this.showSnackbar(message, duration, false)
}

fun View.showErrorSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    this.showSnackbar(message, duration, true)
}


fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT, isError: Boolean = false) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    val backgroundColor = if (isError) R.color.snackbar_error_bg else R.color.snackbar_success_bg
    val textColor = R.color.snackbar_text_color

    // Arka planı tint olarak uygula
    ViewCompat.setBackgroundTintList(
        snackbar.view,
        ColorStateList.valueOf(ContextCompat.getColor(context, backgroundColor))
    )

    // Yazı rengi
    val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, textColor))

    snackbar.show()
}