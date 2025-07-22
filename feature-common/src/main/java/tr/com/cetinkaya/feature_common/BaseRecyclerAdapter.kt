package tr.com.cetinkaya.feature_common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerAdapter<M : Any, VB : ViewBinding, VH : BaseViewHolder<M, VB>>(callback: DiffUtil.ItemCallback<M>) :
    ListAdapter<M, VH>(callback) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.doBindings((getItem(position)))
        holder.bind()
    }

    override fun submitList(items: List<M>?) {
        super.submitList(items ?: emptyList())
    }

}


