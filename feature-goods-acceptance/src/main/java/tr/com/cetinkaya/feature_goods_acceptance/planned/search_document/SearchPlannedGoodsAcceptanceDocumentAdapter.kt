package tr.com.cetinkaya.feature_goods_acceptance.planned.search_document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutPlannedGoodsAcceptanceDocumentBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel

class SearchPlannedGoodsAcceptanceDocumentAdapter(
    private val onItemClick: ((DocumentUiModel?) -> Unit)? = null
) : BaseRecyclerAdapter<DocumentUiModel, RowLayoutPlannedGoodsAcceptanceDocumentBinding, SearchPlannedGoodsAcceptanceDocumentViewHolder>(
    SearchPlannedGoodsAcceptanceDocumentDiffUtil()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SearchPlannedGoodsAcceptanceDocumentViewHolder {
        val binding = RowLayoutPlannedGoodsAcceptanceDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SearchPlannedGoodsAcceptanceDocumentViewHolder(binding = binding, click = onItemClick)
    }


}

class SearchPlannedGoodsAcceptanceDocumentDiffUtil : DiffUtil.ItemCallback<DocumentUiModel>() {
    override fun areItemsTheSame(
        oldItem: DocumentUiModel, newItem: DocumentUiModel
    ): Boolean {
        return oldItem.documentSeries == newItem.documentSeries && oldItem.documentNumber == newItem.documentNumber && oldItem.companyCode == newItem.companyCode
    }

    override fun areContentsTheSame(
        oldItem: DocumentUiModel, newItem: DocumentUiModel
    ): Boolean {
        return oldItem == newItem
    }
}