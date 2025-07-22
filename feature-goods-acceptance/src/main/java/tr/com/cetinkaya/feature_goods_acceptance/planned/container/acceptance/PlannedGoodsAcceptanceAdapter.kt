package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutStockTransactionBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.GetStockTransactionsByDocumentUiModel

class PlannedGoodsAcceptanceAdapter() :
    BaseRecyclerAdapter<GetStockTransactionsByDocumentUiModel, RowLayoutStockTransactionBinding, PlannedGoodsAcceptanceViewHolder>(
        PlannedGoodsAcceptanceDiffUtil()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PlannedGoodsAcceptanceViewHolder {
        val binding = RowLayoutStockTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlannedGoodsAcceptanceViewHolder(binding)
    }
}


class PlannedGoodsAcceptanceViewHolder(private val binding: RowLayoutStockTransactionBinding) :
    BaseViewHolder<GetStockTransactionsByDocumentUiModel, RowLayoutStockTransactionBinding>(binding) {

    override fun bind() {
        getRowItem()?.let {
            binding.apply {
                tvBarcode.text = it.barcode
                tvQuantity.text = it.quantity.toString()
                tvDeliveredQuantity.text = it.deliveredQuantity.toString()
                tvStockName.text = it.stockName
            }
        }
    }

}


class PlannedGoodsAcceptanceDiffUtil : DiffUtil.ItemCallback<GetStockTransactionsByDocumentUiModel>() {

    override fun areItemsTheSame(
        oldItem: GetStockTransactionsByDocumentUiModel, newItem: GetStockTransactionsByDocumentUiModel
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.barcode == newItem.barcode && oldItem.quantity == newItem.quantity
    }

    override fun areContentsTheSame(
        oldItem: GetStockTransactionsByDocumentUiModel, newItem: GetStockTransactionsByDocumentUiModel
    ): Boolean {
        return oldItem == newItem
    }

}