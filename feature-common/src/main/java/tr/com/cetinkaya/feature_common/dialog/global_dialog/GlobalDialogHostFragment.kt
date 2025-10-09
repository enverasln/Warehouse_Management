package tr.com.cetinkaya.feature_common.dialog.global_dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.R
import tr.com.cetinkaya.feature_common.app_effect.AppEffect
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import javax.inject.Inject

@AndroidEntryPoint
class GlobalDialogHostFragment : Fragment() {
    @Inject
    lateinit var appEventBus: AppEventBus

    @Inject
    lateinit var registry: DialogRequestRegistry

    private var activeDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                appEventBus.effect.collect { effect ->
                    when (effect) {
                        is AppEffect.ShowConfirmDialog -> {
                            if (!registry.has(effect.id)) return@collect
                            showConfirm(effect)
                        }

                        is AppEffect.ShowConfirmDialogWithCheckBox -> {
                            val ctx = requireContext()
                            val dp = resources.displayMetrics.density
                            val padH = (24 * dp).toInt()
                            val padV = (18 * dp).toInt()

                            val messageView = TextView(ctx).apply {
                                text = effect.message
                                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                            }
                            val checkBox = CheckBox(ctx).apply { text = effect.checkLabel }

                            val container = LinearLayout(ctx).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(padH, padV, padH, padV)
                                addView(
                                    messageView, ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                )
                                addView(
                                    checkBox, ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                )
                            }

                            val dialog = MaterialAlertDialogBuilder(ctx).setTitle(effect.title).setIcon(R.drawable.ic_warning) // varsa
                                .setView(container).setNegativeButton(effect.negativeButtonText) { d, _ ->
                                    registry.complete(effect.id, false); d.dismiss()
                                }.setPositiveButton(effect.positiveButtonText, null).setCancelable(effect.cancelable).create()

                            dialog.setOnShowListener {
                                val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                positive.isEnabled = false
                                checkBox.setOnCheckedChangeListener { _, ok -> positive.isEnabled = ok }
                                positive.setOnClickListener {
                                    registry.complete(effect.id, true); dialog.dismiss()
                                }
                            }
                            dialog.setOnCancelListener {
                                registry.complete(effect.id, false)
                            }
                            dialog.show()

                        }

                        is AppEffect.ShowWarningDialog -> {
                            if (!registry.has(effect.id)) return@collect
                            showWarning(effect)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activeDialog?.dismiss()
        activeDialog = null
    }

    private fun showConfirm(e: AppEffect.ShowConfirmDialog) {
        activeDialog?.dismiss()
        activeDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            e.title?.let { setTitle(it) }
            setMessage(e.message)
            setCancelable(e.cancelable)
            setPositiveButton(e.positiveText) { d, _ ->
                registry.complete(e.id, true); d.dismiss()
            }
            setNegativeButton(e.negativeText) { d, _ ->
                registry.complete(e.id, false); d.dismiss()
            }
            setOnCancelListener { registry.complete(e.id, false) }
        }.create().also { it.show() }
    }

    @SuppressLint("PrivateResource")
    private fun showWarning(e: AppEffect.ShowWarningDialog) {
        activeDialog?.dismiss()
        activeDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            e.title?.let { setTitle(it) }
            setIcon(com.google.android.material.R.drawable.mtrl_ic_error)
            setMessage(e.message)
            setCancelable(e.cancelable)
            setPositiveButton(e.buttonText) { d, _ ->
                registry.complete(e.id, true); d.dismiss()
            }
            setOnCancelListener { registry.complete(e.id, false) }
        }.create().also { it.show() }
    }

    companion object {
        const val TAG = "GlobalDialogHost"
        fun newInstance() = GlobalDialogHostFragment()
    }
}