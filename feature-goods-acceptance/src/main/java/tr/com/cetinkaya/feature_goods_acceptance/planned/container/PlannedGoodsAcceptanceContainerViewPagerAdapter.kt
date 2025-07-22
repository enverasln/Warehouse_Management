package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlannedGoodsAcceptanceContainerViewPagerAdapter(
    fragment: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments: List<() -> Fragment>
) : FragmentStateAdapter(fragment, lifecycle) {


    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position].invoke()

    }


}