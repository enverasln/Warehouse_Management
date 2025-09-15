package tr.com.cetinkaya.domain.di

import dagger.MapKey
import tr.com.cetinkaya.common.enums.TransferredDocumentType

@MapKey
@Retention(AnnotationRetention.BINARY)
annotation class DocumentSyncHandlerKey(val value: TransferredDocumentType)