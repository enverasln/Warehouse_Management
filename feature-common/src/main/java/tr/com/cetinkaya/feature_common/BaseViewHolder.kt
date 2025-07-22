package tr.com.cetinkaya.feature_common

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<M, VB : ViewBinding> constructor(viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: M? = null

    fun doBindings(data: M?) {
        this.item = data
    }

    abstract fun bind()

    fun getRowItem(): M? {
        return item
    }
}