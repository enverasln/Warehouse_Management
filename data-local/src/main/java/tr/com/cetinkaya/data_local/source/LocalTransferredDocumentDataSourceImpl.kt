package tr.com.cetinkaya.data_local.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import javax.inject.Inject

class LocalTransferredDocumentDataSourceImpl @Inject constructor(
    private val transferredDocumentDao: TransferredDocumentDao,
) : LocalTransferredDocumentDataSource {

    override suspend fun add(transferredDocument: TransferredDocumentDataModel): Long {
        val existedTransferredDocument = transferredDocumentDao.getTransferredDocumentByDocumentSeriesAndNumber(
            documentSeries = transferredDocument.documentSeries,
            documentNumber = transferredDocument.documentNumber,
            transferredDocumentType = transferredDocument.transferredDocumentType
        )
        if(existedTransferredDocument != null) return 0
        return transferredDocumentDao.add(transferredDocument.toEntity())
    }

    override suspend fun delete(
        transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, documentNumber: Int
    ): Int {
        return transferredDocumentDao.delete(documentSeries, documentNumber, transferredDocumentTypes)

    }

    override fun getUntransferredDocuments(): Flow<List<TransferredDocumentDataModel>> =
        transferredDocumentDao.getUntransferredDocuments().map { transferredDocuments -> transferredDocuments.map { it.toDataModel() } }


}