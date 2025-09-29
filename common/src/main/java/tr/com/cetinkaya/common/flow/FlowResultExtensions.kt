package tr.com.cetinkaya.common.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import tr.com.cetinkaya.common.Result

suspend fun <T : Any> Flow<Result<T>>.awaitResult(): Result<T> = first { it !is Result.Loading }

suspend fun <T : Any> Flow<Result<T>>.awaitTerminal(): Result<T> = first { it is Result.Success || it is Result.Error }

suspend fun <T : Any> Flow<Result<T>>.await(): Result<T> = first { it is Result.Loading || it is Result.Success || it is Result.Error }

fun <T : Any> Flow<Result<T>>.withLoading(): Flow<Result<T>> = onStart { emit(Result.Loading) }
