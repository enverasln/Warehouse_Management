package tr.com.cetinkaya.data_repository.models.order_transaction

import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel

data class OrderTransactionDocumentDataModel(
    val docSeries: String,
    val docNumber: Int
)

fun OrderTransactionDocumentDomainModel.toDataModel(): OrderTransactionDocumentDataModel = OrderTransactionDocumentDataModel(
    docSeries = this.docSeries,
    docNumber = this.docNumber
)

fun OrderTransactionDocumentDataModel.toDomainModel(): OrderTransactionDocumentDomainModel = OrderTransactionDocumentDomainModel(
    docSeries = this.docSeries,
    docNumber = this.docNumber
)