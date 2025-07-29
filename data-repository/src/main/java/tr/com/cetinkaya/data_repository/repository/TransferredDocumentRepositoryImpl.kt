package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import javax.inject.Inject

class TransferredDocumentRepositoryImpl @Inject constructor(
    private val localTransferredDocumentDataSource: LocalTransferredDocumentDataSource
) : TransferredDocumentRepository {

    override fun add(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String
    ): Flow<Long> = flow{
        val newTransferredDocument = TransferredDocumentDataModel(
            id = 0,
            transferredDocumentType = transferredDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            synchronizationStatus = synchronizationStatus,
            description = description
        )

        val result = localTransferredDocumentDataSource.add(newTransferredDocument)

        emit(result)

    }

}