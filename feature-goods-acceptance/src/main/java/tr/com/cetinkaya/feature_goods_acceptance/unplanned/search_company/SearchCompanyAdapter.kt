package tr.com.cetinkaya.feature_goods_acceptance.unplanned.search_company

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_goods_acceptance.databinding.RowLayoutSearchCompanyBinding
import tr.com.cetinkaya.feature_goods_acceptance.unplanned.model.current_account.CurrentAccountUiModel

class SearchCompanyAdapter(
    private val onItemClick: ((CurrentAccountUiModel?) -> Unit)? = null
) : BaseRecyclerAdapter<CurrentAccountUiModel, RowLayoutSearchCompanyBinding, SearchCompanyAdapter.SearchCompanyViewHolder>(
    SearchCompanyDocumentDiffUtil()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SearchCompanyViewHolder {
        val binding = RowLayoutSearchCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchCompanyViewHolder(binding = binding, onItemClick = onItemClick)
    }

    override fun onBindViewHolder(holder: SearchCompanyViewHolder, position: Int, payloads: List<Any?>) {
        if(payloads.isNotEmpty() && payloads.contains("payload_isSelected_changed")) {
            val item = getItem(position)
            holder.updateSelectionBackground(item.isSelected) //
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    class SearchCompanyViewHolder(
        private val binding: RowLayoutSearchCompanyBinding, private val onItemClick: ((CurrentAccountUiModel?) -> Unit)? = null
    ) : BaseViewHolder<CurrentAccountUiModel, RowLayoutSearchCompanyBinding>(binding) {

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(getRowItem())
            }
        }

        fun updateSelectionBackground(isSelected: Boolean) {
            binding.root.setBackgroundColor(
                if (isSelected) "#D1E9FF".toColorInt() else Color.WHITE
            )
        }

        override fun bind() {
            getRowItem()?.let {
                binding.apply {
                    tvCurrentCode.text = it.currentCode
                    tvCurrentTile1.text = it.currentTitle1
                    tvCurrentTile2.text = it.currentTitle2
                }
                updateSelectionBackground(it.isSelected)
            }
        }
    }
}

class SearchCompanyDocumentDiffUtil : DiffUtil.ItemCallback<CurrentAccountUiModel>() {
    override fun areItemsTheSame(oldItem: CurrentAccountUiModel, newItem: CurrentAccountUiModel): Boolean {
        return oldItem.currentCode == newItem.currentCode
    }

    override fun areContentsTheSame(oldItem: CurrentAccountUiModel, newItem: CurrentAccountUiModel): Boolean {
        return oldItem.currentTitle1 == newItem.currentTitle1 && oldItem.currentTitle2 == newItem.currentTitle2 && oldItem.isSelected == newItem.isSelected
    }

    override fun getChangePayload(oldItem: CurrentAccountUiModel, newItem: CurrentAccountUiModel): Any? {
        return if (oldItem.isSelected != newItem.isSelected) {
            "payload_isSelected_changed"
        } else null
    }
}