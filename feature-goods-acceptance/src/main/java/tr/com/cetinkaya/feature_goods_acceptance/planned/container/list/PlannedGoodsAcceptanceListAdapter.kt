package tr.com.cetinkaya.feature_goods_acceptance.planned.container.list

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutPlannedGoodsAcceptanceProductBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.ProductUiModel

class PlannedGoodsAcceptanceListAdapter(
    private val onDoubleTab: ((ProductUiModel?) -> Unit)? = null
) : BaseRecyclerAdapter<ProductUiModel, RowLayoutPlannedGoodsAcceptanceProductBinding, PlannedGoodsAcceptanceListViewHolder>(
    PlannedGoodsAcceptanceListDiffUtil()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PlannedGoodsAcceptanceListViewHolder {
        val binding = RowLayoutPlannedGoodsAcceptanceProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PlannedGoodsAcceptanceListViewHolder(binding, onDoubleTab)
    }
}


class PlannedGoodsAcceptanceListDiffUtil : DiffUtil.ItemCallback<ProductUiModel>() {

    override fun areItemsTheSame(
        oldItem: ProductUiModel, newItem: ProductUiModel
    ): Boolean {
        return oldItem.barcode == newItem.barcode && oldItem.quantity == newItem.quantity && oldItem.remainingQuantity == newItem.remainingQuantity
    }

    override fun areContentsTheSame(
        oldItem: ProductUiModel, newItem: ProductUiModel
    ): Boolean {
        return oldItem == newItem
    }

}

@SuppressLint("ClickableViewAccessibility")
class PlannedGoodsAcceptanceListViewHolder constructor(
    private val binding: RowLayoutPlannedGoodsAcceptanceProductBinding, private val doubleTap: ((ProductUiModel?) -> Unit)? = null
) : BaseViewHolder<ProductUiModel, RowLayoutPlannedGoodsAcceptanceProductBinding>(binding) {

    private lateinit var gestureDetector: GestureDetector

    override fun bind() {

        val context = binding.root.context
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val item = getRowItem()
                doubleTap?.invoke(item)
                return true
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        getRowItem()?.let {
            binding.apply {
                tvBarcode.text = it.barcode
                tvQuantity.text = it.quantity.toString()
                tvRemainingQuantity.text = (it.remainingQuantity - it.deliveredQuantity).toString()
                tvStockName.text = it.stockName
            }
        }
    }
}