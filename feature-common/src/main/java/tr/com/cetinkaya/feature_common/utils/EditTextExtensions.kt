package tr.com.cetinkaya.feature_common.utils

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(FlowPreview::class)
fun EditText.textChanges(): Flow<String> = callbackFlow {
    val watcher = doAfterTextChanged { text ->
        trySend(text?.toString().orEmpty())
    }
    awaitClose { removeTextChangedListener(watcher) }
}