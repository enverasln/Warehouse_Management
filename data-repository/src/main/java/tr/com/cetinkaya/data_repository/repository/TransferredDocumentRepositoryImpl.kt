package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import tr.com.cetinkaya.data_repository.models.transferred_document.toDataModel
import tr.com.cetinkaya.data_repository.models.transferred_document.toDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import javax.inject.Inject

class TransferredDocumentRepositoryImpl @Inject constructor(
    private val localTransferredDocumentDataSource: LocalTransferredDocumentDataSource
) : TransferredDocumentRepository {

    override suspend fun add(
        transferredDocumentType: TransferredDocumentType,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String,
        currentCode: String?,
        paperNumber: String?
    ): Long {
        val newTransferredDocument = TransferredDocumentDataModel(
            id = 0,
            transferredDocumentType = transferredDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            synchronizationStatus = synchronizationStatus,
            description = description,
            currentCode = currentCode,
            paperNumber = paperNumber
        )

        val result = localTransferredDocumentDataSource.add(newTransferredDocument)

        return result

    }

    override suspend fun delete(
        transferredDocumentTypes: TransferredDocumentType, documentSeries: String, documentNumber: Int
    ): Int {
        val result = localTransferredDocumentDataSource.delete(transferredDocumentTypes, documentSeries, documentNumber)
        return result
    }

    override fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentDomainModel>> =
        localTransferredDocumentDataSource.getUntransferredDocumentsFlow().map { untransferredDocuments ->
            untransferredDocuments.map { document -> document.toDomainModel() }
        }

    override suspend fun getUntransferredDocuments(): List<TransferredDocumentDomainModel> {
        val documents = localTransferredDocumentDataSource.getUntransferredDocuments()
        return documents.map { it.toDomainModel() }
    }

    override suspend fun markedTransferredDocumentSynced(
        documentType: TransferredDocumentType, documentSeries: String, documentNumber: Int
    ) {
        localTransferredDocumentDataSource.markedTransferredDocumentSynced(documentType, documentSeries, documentNumber)
    }

    override suspend fun updateTransferredDocument(
        transferredDocumentType: TransferredDocumentType, documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int
    ) {
        localTransferredDocumentDataSource.updateTransferredDocument(
            transferredDocumentType, documentSeries, oldDocumentNumber, newDocumentNumber
        )
    }

    override suspend fun removeTransferredDocument(
        documentSeries: String, documentNumber: Int, transferredDocumentType: TransferredDocumentType
    ) {
        localTransferredDocumentDataSource.removeTransferredDocument(
            documentSeries = documentSeries, documentNumber = documentNumber, transferredDocumentType = transferredDocumentType
        )
    }

    override suspend fun upsertPending(transferredDocs: List<TransferredDocumentDomainModel>): List<Long> =
        localTransferredDocumentDataSource.upsertPending(transferredDocs.toDataModel())

}