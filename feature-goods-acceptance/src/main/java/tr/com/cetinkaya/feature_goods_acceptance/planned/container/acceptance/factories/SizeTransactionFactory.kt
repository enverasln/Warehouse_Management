package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories

import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models.AddOrderTransactionParams

interface SizeTransactionFactory {
    fun forStock(params: AddOrderTransactionParams, deliveredQty: Double, isColoredAndSized: Boolean): List<AddSizeTransactionDomainModel>
    fun forOrder(params: AddOrderTransactionParams, deliveredQty: Double, isColoredAndSized: Boolean): List<AddSizeTransactionDomainModel>
}