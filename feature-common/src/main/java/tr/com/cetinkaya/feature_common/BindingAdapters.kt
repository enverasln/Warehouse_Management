package tr.com.cetinkaya.feature_common

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("items")
@Suppress("UNCHECKED_CAST")
fun<T> RecyclerView.setItem(items: List<T>?) {
    if(adapter is CGSBindingAdapter<*>) {
        (adapter as? CGSBindingAdapter<T>)?.submitList(items ?: emptyList())
    }
}

interface CGSBindingAdapter<T> {
    fun submitList(items: List<T>)
}

