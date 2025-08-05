package tr.com.cetinkaya.feature_sync

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.feature_common.BaseRecyclerAdapter
import tr.com.cetinkaya.feature_common.BaseViewHolder
import tr.com.cetinkaya.feature_sync.databinding.RowLayoutTransferredDocumentBinding
import tr.com.cetinkaya.feature_sync.models.TransferredDocumentUiModel

class TransferredDocumentAdapter :
BaseRecyclerAdapter<TransferredDocumentUiModel, RowLayoutTransferredDocumentBinding, TransferredDocumentViewHolder>(
    TransferredDocumentDiffUtil()
){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransferredDocumentViewHolder {
        val binding = RowLayoutTransferredDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransferredDocumentViewHolder(binding)
    }
}

class TransferredDocumentViewHolder(private val binding: RowLayoutTransferredDocumentBinding) :
    BaseViewHolder<TransferredDocumentUiModel, RowLayoutTransferredDocumentBinding>(binding) {

    @SuppressLint("SetTextI18n")
    override fun bind() {
        getRowItem()?.let {
            binding.apply {
                tvDocumentType.text =
                    when (it.transferredDocumentType) {
                        TransferredDocumentTypes.WarehouseShipmentDocument -> "Depolara Arası Transfer"
                        TransferredDocumentTypes.NormalGivenOrder -> "Sipariş"
                        TransferredDocumentTypes.NormalPurchaseDispatch -> "Toptan Alış İrsaliyesi"
                        else -> ""
                    }

                tvDocumentSeriesAndNumber.text = "${it.documentSeries} - ${it.documentNumber}"
                tvTransferStatus.text = it.description
            }
        }
    }
}

class TransferredDocumentDiffUtil : DiffUtil.ItemCallback<TransferredDocumentUiModel>() {

    override fun areItemsTheSame(
        oldItem: TransferredDocumentUiModel,
        newItem: TransferredDocumentUiModel
    ): Boolean {
       return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: TransferredDocumentUiModel,
        newItem: TransferredDocumentUiModel
    ): Boolean {
        return oldItem == newItem
    }

}