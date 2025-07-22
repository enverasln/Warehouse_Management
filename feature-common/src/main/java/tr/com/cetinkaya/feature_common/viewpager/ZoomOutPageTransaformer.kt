package tr.com.cetinkaya.feature_common.viewpager

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.05f
        private const val MIN_ALPHA = 0.5f
    }
    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> {
                page.alpha = 0f
            }

            position <= 1 -> {
                val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                val vertMargin = pageHeight * (1 - scaleFactor) / 2
                val horzMargin = pageWidth * (1 - scaleFactor) / 2
                if (position < 0) {
                    page.translationX = horzMargin - vertMargin / 2
                } else {
                    page.translationX = -horzMargin + vertMargin / 2
                }
                // Sayfayı küçültün (MIN_SCALE ile 1 arasında)
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
                // Sayfanın şeffaflığını boyutuna göre ayarlayın.
                page.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
            }

            else -> {
                // Bu sayfa ekranın çok dışında, sağda.
                page.alpha = 0f
            }
        }
    }

}