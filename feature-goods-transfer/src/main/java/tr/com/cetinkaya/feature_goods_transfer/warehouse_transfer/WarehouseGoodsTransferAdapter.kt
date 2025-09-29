package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_transfer.databinding.RowLayoutWarehouseGoodsTransferBinding
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionUiModel

class WarehouseGoodsTransferAdapter(
    private val onItemClick: ((StockTransactionUiModel?) -> Unit)? = null,
    private val onItemLongClick: ((StockTransactionUiModel) -> Unit)? = null
) :
    BaseRecyclerAdapter<StockTransactionUiModel, RowLayoutWarehouseGoodsTransferBinding, WarehouseGoodsTransferViewHolder>(
        WarehouseGoodsTransferDiffUtil()
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarehouseGoodsTransferViewHolder {
        val binding = RowLayoutWarehouseGoodsTransferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarehouseGoodsTransferViewHolder(binding, onItemClick, onItemLongClick)
    }
}

class WarehouseGoodsTransferViewHolder(
    private val binding: RowLayoutWarehouseGoodsTransferBinding,
    private val click: ((StockTransactionUiModel?) -> Unit)? = null,
    private val longClick: ((StockTransactionUiModel) -> Unit)? = null
) :
    BaseViewHolder<StockTransactionUiModel, RowLayoutWarehouseGoodsTransferBinding>(binding) {

    init {
        binding.root.setOnClickListener {
            click?.invoke(getRowItem())
        }

        binding.root.setOnLongClickListener {
            getRowItem()?.let {
                longClick?.invoke(it)
            }
            true
        }
    }

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

class WarehouseGoodsTransferDiffUtil : DiffUtil.ItemCallback<StockTransactionUiModel>() {
    override fun areItemsTheSame(
        oldItem: StockTransactionUiModel,
        newItem: StockTransactionUiModel
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.barcode == newItem.barcode
    }

    override fun areContentsTheSame(
        oldItem: StockTransactionUiModel,
        newItem: StockTransactionUiModel
    ): Boolean = oldItem == newItem

}