package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository


class WarehouseShipmentDocumentSyncHandler(
    stockTransactionRepository: StockTransactionRepository,
    transferredDocumentRepository: TransferredDocumentRepository
) : StockTransactionSyncHandlerBase(
    stockRepo = stockTransactionRepository,
    transferredDocumentRepo = transferredDocumentRepository,
    type = StockTransactionTypes.WarehouseTransfer,
    kind = StockTransactionKinds.InternalTransfer,
    isNormalOrReturn = 0,
    docType = StockTransactionDocumentTypes.InterWarehouseShippingNote
)
