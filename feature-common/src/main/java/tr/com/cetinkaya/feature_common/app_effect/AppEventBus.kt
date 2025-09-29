package tr.com.cetinkaya.feature_common.app_effect

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEventBus @Inject constructor() {
    private val _effects = MutableSharedFlow<AppEffect>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<AppEffect> = _effects.asSharedFlow()

    suspend fun send(effect: AppEffect) {
        _effects.emit(effect)
    }

    fun trySend(effect: AppEffect) {
        _effects.tryEmit(effect)
    }
}