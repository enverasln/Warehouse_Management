package tr.com.cetinkaya.data_repository.models.current_account

import tr.com.cetinkaya.domain.model.current_account.GetCurrentAccountByTitleDomainModel

data class GetCurrentAccountByTitleDataModel(
    val currentCode: String,
    val currentTitle1: String,
    val currentTitle2: String,
)

fun GetCurrentAccountByTitleDataModel.toDomainModel(): GetCurrentAccountByTitleDomainModel = GetCurrentAccountByTitleDomainModel(
    currentCode = this.currentCode, currentTitle1 = this.currentTitle1, currentTitle2 = this.currentTitle2
)

fun List<GetCurrentAccountByTitleDataModel>.toDomainModel(): List<GetCurrentAccountByTitleDomainModel> = this.map { it.toDomainModel() }