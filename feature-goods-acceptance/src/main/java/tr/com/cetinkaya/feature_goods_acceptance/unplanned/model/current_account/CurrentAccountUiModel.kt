package tr.com.cetinkaya.feature_goods_acceptance.unplanned.model.current_account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tr.com.cetinkaya.domain.model.current_account.GetCurrentAccountByTitleDomainModel

@Parcelize
data class CurrentAccountUiModel(
    val currentCode: String, val currentTitle1: String, val currentTitle2: String, val isSelected: Boolean = false
) : Parcelable

fun GetCurrentAccountByTitleDomainModel.toUiModel(): CurrentAccountUiModel = CurrentAccountUiModel(
    currentCode = this.currentCode, currentTitle1 = this.currentTitle1, currentTitle2 = this.currentTitle2
)

fun List<GetCurrentAccountByTitleDomainModel>.toUiModel(): List<CurrentAccountUiModel> = this.map { it.toUiModel() }