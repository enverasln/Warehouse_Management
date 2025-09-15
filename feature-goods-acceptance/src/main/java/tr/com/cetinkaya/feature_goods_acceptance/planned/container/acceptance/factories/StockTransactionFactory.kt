package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories

import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

interface StockTransactionFactory {
    fun from(orderTx: OrderTransactionUiModel, doc: StockTransactionDocumentUiModel, user: UserUiModel, deliveredQty: Double) : AddStockTransactionDomainModel
}

class DefaultStockTransactionFactory : StockTransactionFactory {
    override fun from(
        orderTx: OrderTransactionUiModel,
        doc: StockTransactionDocumentUiModel,
        user: UserUiModel,
        deliveredQty: Double
    ): AddStockTransactionDomainModel {
        return AddStockTransactionDomainModel(
            transactionType = doc.transactionType,
            transactionKind = doc.transactionKind,
            isNormalOrReturn = doc.isNormalOrReturn,
            transactionDocumentType = doc.transactionDocumentType,
            documentDate = DateConverter.uiToTimestamp(doc.documentDate),
            documentSeries = doc.documentSeries,
            documentNumber = doc.documentNumber,
            lineNumber = 0,
            stockCode = orderTx.stockCode,
            stockName = orderTx.stockName,
            currentCode = orderTx.currentCode,
            quantity = deliveredQty,
            inputWarehouseNumber = user.warehouseNumber,
            outputWarehouseNumber = user.warehouseNumber,
            paymentPlanNumber = orderTx.paymentPlanNumber,
            salesman = user.username,
            responsibilityCenter = user.warehouseNumber.toString(),
            userCode = user.mikroFlyUserId,
            totalPrice = deliveredQty * orderTx.unitPrice,
            discount1 = orderTx.discount1,
            discount2 = orderTx.discount2,
            discount3 = orderTx.discount3,
            discount4 = orderTx.discount4,
            discount5 = orderTx.discount5,
            taxPointer = orderTx.taxPointer,
            orderId = orderTx.id,
            price = orderTx.unitPrice,
            paperNumber = doc.paperNumber,
            companyNumber = 0,
            storeNumber = 0,
            barcode = orderTx.barcode,
            isColoredAndSized = orderTx.isColoredAndSized,
            transportationStatus = 0
        )
    }

}