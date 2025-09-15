package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories

import tr.com.cetinkaya.common.enums.DataOrigin
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.domain.model.order_transaction.AddOrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models.AddOrderTransactionParams
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel

interface OrderTransactionFactory {
    fun from(orderTx: OrderTransactionUiModel): AddOrderTransactionDomainModel
}

class DefaultOrderTransactionFactory: OrderTransactionFactory {
    override fun from(orderTx: OrderTransactionUiModel): AddOrderTransactionDomainModel {
        return AddOrderTransactionDomainModel(
            orderDate = orderTx.orderDate,
            documentSeries = orderTx.documentSeries,
            documentNumber = orderTx.documentNumber,
            rowNumber = orderTx.rowNumber,
            stockId = orderTx.stockId,
            stockCode = orderTx.stockCode,
            stockName = orderTx.stockName,
            barcode = orderTx.barcode,
            currentId = orderTx.currentId,
            currentCode = orderTx.currentCode,
            currentName = orderTx.currentName,
            paymentPlanNumber = orderTx.paymentPlanNumber,
            warehouseId = orderTx.warehouseId,
            warehouseNumber = orderTx.warehouseNumber,
            warehouseName = orderTx.warehouseName,
            quantity = orderTx.quantity,
            unitPrice = orderTx.unitPrice,
            currencyType = orderTx.currencyType,
            discount1 = orderTx.discount1,
            discount2 = orderTx.discount2,
            discount3 = orderTx.discount3,
            discount4 = orderTx.discount4,
            discount5 = orderTx.discount5,
            totalPrice = orderTx.totalPrice,
            taxPointer = orderTx.taxPointer,
            currentResponsibilityCenter = orderTx.currentResponsibilityCenter,
            stockResponsibilityCenter = orderTx.stockResponsibilityCenter,
            remainingQuantity = orderTx.remainingQuantity,
            deliveredQuantity = orderTx.deliveredQuantity,
            isColoredAndSized = orderTx.isColoredAndSized,
            syncStatus = SyncStatus.New,
            userCode = orderTx.userCode,
            dataOrigin = DataOrigin.Local
        )
    }
}


class ColoredSizeAwareSizeTransactionFactory : SizeTransactionFactory {
    override fun forStock(params: AddOrderTransactionParams, deliveredQty: Double, isColoredAndSized: Boolean): List<AddSizeTransactionDomainModel> =
        build(params, deliveredQty, isColoredAndSized, SizeTransactionType.StockTransaction)


    override fun forOrder(params: AddOrderTransactionParams, deliveredQty: Double, isColoredAndSized: Boolean): List<AddSizeTransactionDomainModel> =
        build(params, deliveredQty, isColoredAndSized, SizeTransactionType.Order)

    private fun build(
        params: AddOrderTransactionParams,
        deliveredQty: Double,
        isColoredAndSized: Boolean,
        type: SizeTransactionType
    ): List<AddSizeTransactionDomainModel> {
        if (!isColoredAndSized) return emptyList()
        return listOf(
            AddSizeTransactionDomainModel(
                barcode = params.barcode,
                refRecordId = "", // NOTE: to be filled by repository after insert, if needed
                sizeTransactionType = type,
                quantity = deliveredQty
            )
        )
    }
}