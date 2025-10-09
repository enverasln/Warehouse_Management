package tr.com.cetinkaya.feature_goods_acceptance.unplanned.acceptance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutUnplannedAcceptanceBinding
import tr.com.cetinkaya.feature_goods_acceptance.models.stock_transaction.StockTransactionUiModel


class UnplannedAcceptanceAdapter(
    private val onItemLongClick: ((StockTransactionUiModel) -> Unit)? = null
) : BaseRecyclerAdapter<StockTransactionUiModel, RowLayoutUnplannedAcceptanceBinding, UnplannedAcceptanceViewHolder>(
    UnplannedAcceptanceDiffUtil()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UnplannedAcceptanceViewHolder {
        val binding = RowLayoutUnplannedAcceptanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnplannedAcceptanceViewHolder(binding, onItemLongClick)
    }


}

class UnplannedAcceptanceViewHolder(
    private val binding: RowLayoutUnplannedAcceptanceBinding, private val longClick: ((StockTransactionUiModel) -> Unit)? = null
) : BaseViewHolder<StockTransactionUiModel, RowLayoutUnplannedAcceptanceBinding>(binding) {


    init {
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

class UnplannedAcceptanceDiffUtil : DiffUtil.ItemCallback<StockTransactionUiModel>() {
    override fun areItemsTheSame(
        oldItem: StockTransactionUiModel, newItem: StockTransactionUiModel
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.barcode == newItem.barcode
    }

    override fun areContentsTheSame(
        oldItem: StockTransactionUiModel, newItem: StockTransactionUiModel
    ): Boolean = oldItem == newItem

}