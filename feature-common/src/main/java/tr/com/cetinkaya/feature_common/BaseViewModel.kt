package tr.com.cetinkaya.feature_common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tr.com.cetinkaya.feature_common.app_effect.AppEffect
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import java.util.UUID

abstract class BaseViewModel<Event : UiEvent, State : UiState, Effect : UiEffect> constructor(
    private val appEventBus: AppEventBus, private val dialogRegistry: DialogRequestRegistry
) : ViewModel() {

    private val initialState: State by lazy { createInitialState() }
    abstract fun createInitialState(): State

    private var _previousState: State? = null
    val previousState: State?
        get() = _previousState

    val currentState: State
        get() = uiState.value

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow(
        replay = 0,                       // one-shot
        extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeEvents()
    }

    /**
     * Start listening to Event
     */
    private fun subscribeEvents() {
        viewModelScope.launch {
            event.collect {
                handleEvent(it)
            }
        }
    }

    /**
     * Handle each event
     */
    abstract fun handleEvent(event: Event)


    /**
     * Set new Event
     */
    fun setEvent(event: Event) {
        val newEvent = event
        viewModelScope.launch { _event.emit(newEvent) }
    }


    /**
     * Set new Ui State
     */
    fun setState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _previousState = currentState
        _uiState.value = newState
    }

    /**
     * Set new Effect
     */
    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    protected suspend fun askForConfirmation(
        message: String, title: String? = null, positiveButtonText: String = "Evet", negativeButtonText: String = "Hayır", cancelable: Boolean = true
    ): Boolean {
        val id = UUID.randomUUID().toString()
        val deferred = dialogRegistry.register(id)

        withContext(Dispatchers.Main.immediate) {
            appEventBus.send(
                AppEffect.ShowConfirmDialog(
                    id = id, title = title, message = message, positiveText = positiveButtonText, negativeText = negativeButtonText, cancelable = cancelable
                )
            )
        }
        return deferred.await()
    }

    protected suspend fun askForDangerousConfirmation(
        message: String,
        title: String?,
        checkLabel: String,
        positiveButtonText: String = "Evet",
        negativeButtonText: String = "Hayır",
        cancelable: Boolean = false
    ): Boolean  {
        val id = UUID.randomUUID().toString()
        val deferred = dialogRegistry.register(id)

        withContext(Dispatchers.Main.immediate) {
            appEventBus.send(
                AppEffect.ShowConfirmDialogWithCheckBox(
                    id = id,
                    title = title,
                    message = message,
                    checkLabel = checkLabel,
                    positiveButtonText = positiveButtonText,
                    negativeButtonText = negativeButtonText,
                    cancelable = cancelable
                )
            )
        }

        return deferred.await()
    }

    protected suspend fun postGlobalErrorSuspending(message: String) {
        appEventBus.send(AppEffect.ShowError(message))
    }

    protected fun postGlobalError(message: String) {
        appEventBus.trySend(AppEffect.ShowError(message))
    }

    protected suspend fun postGlobalSuccessSuspending(message: String) {
        appEventBus.send(AppEffect.ShowSuccess(message))
    }

    protected suspend fun postGlobalSuccess(message: String) {
        appEventBus.trySend(AppEffect.ShowSuccess(message))
    }
}

interface UiEvent

interface UiState

interface UiEffect