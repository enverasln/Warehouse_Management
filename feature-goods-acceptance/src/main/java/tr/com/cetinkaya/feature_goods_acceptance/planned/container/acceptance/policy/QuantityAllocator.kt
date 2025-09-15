package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy

import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import kotlin.math.min

interface QuantityAllocator {
    fun allocate(totalDeliveredQuantity: Double, orderTxs: List<OrderTransactionUiModel>): List<AllocatedDelivery>
}

data class AllocatedDelivery(val orderTx: OrderTransactionUiModel, val deliveredQty: Double)

class RemainingFirstQuantityAllocator : QuantityAllocator {
    override fun allocate(totalDeliveredQuantity: Double, orderTxs: List<OrderTransactionUiModel>): List<AllocatedDelivery> {
        var remaining = totalDeliveredQuantity
        if(remaining <= 0.0) return emptyList()
        val result = mutableListOf<AllocatedDelivery>()
        for(tx in orderTxs) {
            if(remaining <= 0.0) break
            val rem = (tx.remainingQuantity - tx.deliveredQuantity).coerceAtLeast(0.0)
            if(rem <= 0.0) continue
            val alloc = min(remaining, rem)
            if(alloc > 0.0) {
                result += AllocatedDelivery(tx, alloc)
                remaining -= alloc
            }
        }
        return result
    }
}