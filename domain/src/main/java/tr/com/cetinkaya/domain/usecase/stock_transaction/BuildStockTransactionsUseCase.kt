package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.DataOrigin
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.common.enums.TransferUnit
import tr.com.cetinkaya.common.utils.DoubleExtensions.isNullOrZero
import tr.com.cetinkaya.domain.model.barcode.BarcodeDefinitionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel
import tr.com.cetinkaya.domain.usecase.UseCase
import java.util.UUID

class BuildStockTransactionsUseCase(
    configuration: Configuration,
) : UseCase<BuildStockTransactionsUseCase.Request, BuildStockTransactionsUseCase.Response>(configuration) {


    override fun process(request: Request): Flow<Response> = flow {
        val now = System.currentTimeMillis()
        val unitPrice = request.barcodeDefinition.price1

        val coef = when (request.unit) {
            TransferUnit.Adet -> 1.0
            TransferUnit.Paket -> request.barcodeDefinition.unit2Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
            TransferUnit.Koli -> request.barcodeDefinition.unit3Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
        }

        val adjustedBaseQty = request.baseQuantity * coef
        val id = request.stockTransactionId ?: UUID.randomUUID().toString()

        fun makeLine(qty: Double, barcode: String): StockTransactionDomainModel {
            val totalPrice = unitPrice * qty
            return StockTransactionDomainModel(
                id = id,
                transactionType = request.stockTransactionDocument.transactionType,
                transactionKind = request.stockTransactionDocument.transactionKind,
                isNormalOrReturn = request.stockTransactionDocument.isNormalOrReturn,
                transactionDocumentType = request.stockTransactionDocument.transactionDocumentType,
                documentDate = request.stockTransactionDocument.documentDate,
                documentSeries = request.stockTransactionDocument.documentSeries,
                documentNumber = request.stockTransactionDocument.documentNumber,
                lineNumber = request.lineNumber,
                stockCode = request.barcodeDefinition.stockCode,
                stockName = request.barcodeDefinition.stockName,
                currentCode = "",
                quantity = qty,
                inputWarehouseNumber = request.selectedWarehouse.warehouseNumber,
                outputWarehouseNumber = request.loggedUser.warehouseNumber,
                paymentPlanNumber = 0,
                salesman = request.loggedUser.username,
                responsibilityCenter = request.loggedUser.warehouseNumber.toString(),
                userCode = request.loggedUser.mikroFlyUserId,
                totalPrice = totalPrice,
                discount1 = 0.0,
                discount2 = 0.0,
                discount3 = 0.0,
                discount4 = 0.0,
                discount5 = 0.0,
                taxPointer = 0,
                orderId = null,
                price = unitPrice,
                paperNumber = request.stockTransactionDocument.paperNumber,
                companyNumber = 0,
                storeNumber = 0,
                barcode = barcode,
                isColoredAndSized = request.barcodeDefinition.isColoredAndSized,
                transportationStatus = 0,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.New,
                dataOrigin = DataOrigin.Local
            )
        }

        val lines = if (request.barcodeDefinition.connectionType == 2.toByte()) {
            request.barcodeDefinition.sizeBarcodes?.map { sb ->
                val lineQty = adjustedBaseQty * sb.quantity
                makeLine(lineQty, sb.barcode)
            }
        } else {
            listOf(makeLine(adjustedBaseQty, request.barcodeDefinition.barcode))
        } ?: emptyList()
        emit(Response(lines))
    }


    data class Request(
        val barcodeDefinition: BarcodeDefinitionDomainModel,
        val stockTransactionDocument: StockTransactionDocumentDomainModel,
        val loggedUser: UserDomainModel,
        val selectedWarehouse: WarehouseDomainModel,
        val baseQuantity: Double,
        val lineNumber: Long = 0L,
        val unit: TransferUnit,
        val stockTransactionId: String? = null
    ) : UseCase.Request

    data class Response(val transactions: List<StockTransactionDomainModel>) : UseCase.Response

}