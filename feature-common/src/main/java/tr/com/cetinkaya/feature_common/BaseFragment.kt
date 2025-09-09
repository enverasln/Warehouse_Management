package tr.com.cetinkaya.feature_common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnAttach
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    abstract val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> VB

    protected val binding: VB
        get() = requireNotNull(_binding)

    protected open fun useNestedScroll(): Boolean = true

    protected open fun configureNestedScroll(scroll: NestedScrollView) {}

    abstract fun prepareView(savedInstanceState: Bundle?)
    open fun observeState() {}
    open fun observeEffect() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindLayout.invoke(inflater, container, false)
        val content = requireNotNull(_binding).root

        if(!useNestedScroll()) return content

        val scroll = NestedScrollView(requireContext()).apply {
            id = View.generateViewId()
            isFillViewport = true
            clipToPadding = false
            addView(content, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        return scroll
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(useNestedScroll() && view is NestedScrollView) {
            view.doOnAttach {
                (view.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = AppBarLayout.ScrollingViewBehavior()
            }
            configureNestedScroll(view)
        }

        prepareView(savedInstanceState)
        observeState()
        observeEffect()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}