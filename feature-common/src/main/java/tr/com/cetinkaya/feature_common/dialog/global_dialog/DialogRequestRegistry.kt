package tr.com.cetinkaya.feature_common.dialog.global_dialog

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogRequestRegistry @Inject constructor() {
    private val map = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    fun register(id: String): CompletableDeferred<Boolean> =
        CompletableDeferred<Boolean>().also { map[id] = it }

    fun complete(id: String, value: Boolean) { map.remove(id)?.complete(value) }
    fun cancel(id: String) { map.remove(id)?.cancel() }

    fun has(id: String): Boolean = map.containsKey(id)
}