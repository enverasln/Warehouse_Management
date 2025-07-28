package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_transfer.databinding.RowLayoutWarehouseGoodsTransferBinding
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionUiModel

class WarehouseGoodsTransferAdapter :
    BaseRecyclerAdapter<StockTransactionUiModel, RowLayoutWarehouseGoodsTransferBinding, WarehouseGoodsTransferViewHolder>(
        WarehouseGoodsTransferDiffUtil()
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarehouseGoodsTransferViewHolder {
        val binding = RowLayoutWarehouseGoodsTransferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarehouseGoodsTransferViewHolder(binding)
    }
}

class WarehouseGoodsTransferViewHolder(private val binding: RowLayoutWarehouseGoodsTransferBinding) :
    BaseViewHolder<StockTransactionUiModel, RowLayoutWarehouseGoodsTransferBinding>(binding) {

    override fun bind() {
        getRowItem()?.let {
            binding.apply {
                tvBarcode.text = it.barcode
                tvStockName.text = it.stockName
                tvQuantity.text = it.quantity.toString()
            }
        }
    }

}

class WarehouseGoodsTransferDiffUtil: DiffUtil.ItemCallback<StockTransactionUiModel>() {
    override fun areItemsTheSame(
        oldItem: StockTransactionUiModel,
        newItem: StockTransactionUiModel
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.barcode == newItem.barcode
    }

    override fun areContentsTheSame(
        oldItem: StockTransactionUiModel,
        newItem: StockTransactionUiModel
    ): Boolean {
        return oldItem == newItem
    }

}