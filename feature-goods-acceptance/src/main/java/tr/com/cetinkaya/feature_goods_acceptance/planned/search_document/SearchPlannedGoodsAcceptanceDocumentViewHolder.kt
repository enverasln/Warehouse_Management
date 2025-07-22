package tr.com.cetinkaya.feature_goods_acceptance.planned.search_document

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.core.graphics.toColorInt
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutPlannedGoodsAcceptanceDocumentBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel

class SearchPlannedGoodsAcceptanceDocumentViewHolder constructor(
    private val binding: RowLayoutPlannedGoodsAcceptanceDocumentBinding, private val click: ((DocumentUiModel?) -> Unit)? = null
) : BaseViewHolder<DocumentUiModel, RowLayoutPlannedGoodsAcceptanceDocumentBinding>(binding) {

    init {
        binding.root.setOnClickListener {
            click?.invoke(getRowItem())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bind() {
        getRowItem()?.let {
            binding.apply {
                tvOrderDate.text = it.documentDate
                tvDocumentSeriesAndNumber.text = "${it.documentSeries} - ${it.documentNumber}"
                tvCompanyCodeAndName.text = "${it.companyCode} - ${it.companyName}"

                root.setBackgroundColor(
                    if (it.isSelected) "#D1E9FF".toColorInt() else Color.TRANSPARENT
                )
            }
        }
    }
}