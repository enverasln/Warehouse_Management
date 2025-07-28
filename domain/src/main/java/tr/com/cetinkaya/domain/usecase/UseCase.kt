package tr.com.cetinkaya.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.Result


abstract class UseCase<I : UseCase.Request, O : UseCase.Response>(private val configuration: Configuration) {

    operator fun invoke(request: I) = process(request)
        .map<O, Result<O>> {
            Result.Success(it)
        }.flowOn(configuration.dispatcher)
        .catch { e ->
            emit(
                Result.Error(message = e.message ?: "Bilinmeyen hata ile karşılaşıldı.", throwable = e))
        }

    internal abstract fun process(request: I): Flow<O>

    interface Request

    interface Response

    class Configuration(val dispatcher: CoroutineDispatcher = Dispatchers.IO)
}


