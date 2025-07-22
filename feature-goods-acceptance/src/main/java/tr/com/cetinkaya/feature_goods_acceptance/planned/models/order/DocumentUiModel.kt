package tr.com.cetinkaya.feature_goods_acceptance.planned.models.order
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import java.util.Date


@Parcelize
data class DocumentUiModel(
    val companyName: String,
    val companyCode: String,
    val documentSeries: String,
    val documentNumber: Int,
    val documentDate: String,
    val isSelected: Boolean = false
) : Parcelable


fun DocumentUiModel.toDomainModel() = DocumentDomainModel(
    companyName = companyName,
    companyCode = companyCode,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    documentDate = Date(DateConverter.uiToTimestamp(documentDate) ?: 0),
    isSelected = isSelected,
    warehouseNumber = 0
)

fun DocumentDomainModel.toUiModel() = DocumentUiModel(
    companyName = companyName,
    companyCode = companyCode,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    documentDate = DateConverter.timestampToUi(documentDate.time),
    isSelected = isSelected
)