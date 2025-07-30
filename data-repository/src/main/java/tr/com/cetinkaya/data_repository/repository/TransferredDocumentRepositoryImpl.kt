package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import tr.com.cetinkaya.data_repository.models.transferred_document.toDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import javax.inject.Inject

class TransferredDocumentRepositoryImpl @Inject constructor(
    private val localTransferredDocumentDataSource: LocalTransferredDocumentDataSource
) : TransferredDocumentRepository {

    override suspend fun add(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String
    ): Long {
        val newTransferredDocument = TransferredDocumentDataModel(
            id = 0,
            transferredDocumentType = transferredDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            synchronizationStatus = synchronizationStatus,
            description = description
        )

        val result = localTransferredDocumentDataSource.add(newTransferredDocument)

        return result

    }

    override suspend fun delete(
        transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, documentNumber: Int
    ): Int {
        val result = localTransferredDocumentDataSource.delete(transferredDocumentTypes, documentSeries, documentNumber)
        return result
    }

    override fun getUntransferredDocuments(): Flow<List<TransferredDocumentDomainModel>> = localTransferredDocumentDataSource.getUntransferredDocuments().map { untransferredDocuments ->
        untransferredDocuments.map { document -> document.toDomainModel() }
    }

}