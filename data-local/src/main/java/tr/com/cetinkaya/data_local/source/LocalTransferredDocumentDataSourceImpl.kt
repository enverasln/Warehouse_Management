package tr.com.cetinkaya.data_local.source

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import javax.inject.Inject

class LocalTransferredDocumentDataSourceImpl @Inject constructor(
    private val transferredDocumentDao: TransferredDocumentDao,
) : LocalTransferredDocumentDataSource{

    override suspend fun add(transferredDocument: TransferredDocumentDataModel): Long {
        return transferredDocumentDao.add(transferredDocument.toEntity())
    }
}