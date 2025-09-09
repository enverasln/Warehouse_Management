package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

class NormalPurchaseStockTransactionSyncHandler(
    stockTransactionRepository: StockTransactionRepository,
    sizeTransactionRepository: SizeTransactionRepository,
    transferredDocumentRepository: TransferredDocumentRepository
) : StockTransactionSyncHandlerBase(
    stockTransactionRepo = stockTransactionRepository,
    sizeTransactionRepo =sizeTransactionRepository,
    transferredDocumentRepo = transferredDocumentRepository,
    transactionType = StockTransactionType.Input,
    transactionKind = StockTransactionKind.Wholesale,
    isNormalOrReturn = 0,
    transactionDocumentType = StockTransactionDocumentType.EntryDispatchNote
)