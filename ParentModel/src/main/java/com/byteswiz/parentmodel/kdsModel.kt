package com.byteswiz.parentmodel
import java.io.Serializable
import androidx.room.*
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

data class CompleteOrderRequest(
    val order: ParentModel,
    val orderStatusId: Int,
    val textDuration:String
): Serializable {
}


@Entity(tableName = "Orders",
    indices = [Index(value = ["Id"])])
data class ParentModel (
    @PrimaryKey(autoGenerate = true)
    var Id: Int = 0,

    @SerializedName("OrderTypeId")
    val OrderTypeId : Int,

    @SerializedName("OrderRefNo")
    val OrderRefNo : String = "",

    @SerializedName("children")
    @TypeConverters(KDSCartItem::class)
    var children : List<KDSCartItem>?,

    @SerializedName("KDSOrderDate")
    val KDSOrderDate: String ="",

    @SerializedName("diningOptionName")
    val diningOptionName: String,

    @SerializedName("ticketName")
    val ticketName: String?,

    @SerializedName("receiptNo")
    val receiptNo: String,

    @SerializedName("qrcode")
    val qrcode: String,

    @SerializedName("orderStatusId")
    var orderStatusId: Int,

    @SerializedName("IsSynced")
    var IsSynced: Boolean,

    @SerializedName("qNo")
    var qNo: String?,

    @SerializedName("TodaysOrderNo")
    var TodaysOrderNo: String,

    @SerializedName("IsCompleted")
    var IsCompleted:Boolean?,

    @SerializedName("ticketGuidId")
    var ticketGuidId: String?,

    @SerializedName("localUniqueId")
    var localUniqueId: String
):Serializable

@Entity(tableName = "CartItems",
    foreignKeys = [
        ForeignKey(entity = ParentModel::class,
            parentColumns = ["Id"],
            childColumns = ["ParentModelId"]
            //,  onDelete = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["Id","ParentModelId"])])
data class KDSCartItem(

    @SerializedName("Id")
    @PrimaryKey(autoGenerate = true)
    var Id: Int = 0,

    @SerializedName("itemId")
    var itemId: Long?,

    @SerializedName("CombiName")
    var CombiName: String,

    @SerializedName("quantity")
    var quantity: Double = 0.0,

    @SerializedName("modifiers")
    @TypeConverters(KDSModifiersModel::class)
    var modifiers: List<KDSModifiersModel>?,

    @SerializedName("SpecialInstructions")
    var SpecialInstructions: String?,

    @SerializedName("IfNotAvailable")
    var IfNotAvailable: Int?,

    @SerializedName("IsDone")
    var IsDone: Boolean?,

    @SerializedName("ParentModelId")
    var ParentModelId: Long?

): Serializable


class KDSModifiersModel(

    @SerializedName("Id")
    @PrimaryKey(autoGenerate = true)
    val Id: Int?,

    @SerializedName("isChecked")
    var isChecked: Boolean,

    @SerializedName("name")
    var name: String,

    @SerializedName("SKUId")
    var SKUId: Int,

    @SerializedName("Price")
    var Price: Double,

    @SerializedName("ModifierId")
    var ModifierId: Long,

    @SerializedName("ModifierName")
    var ModifierName: String,

    @SerializedName("isHeader")
    var isHeader: Boolean,

    @SerializedName("SelectedTypeId")
    var SelectedTypeId: Int,

    @SerializedName("MaxQty")
    var MaxQty: Int,

    @SerializedName("IsRequired")
    var IsRequired: Boolean,

    @SerializedName("DisplayName")
    var DisplayName: String,

    @SerializedName("MinQty")
    var MinQty: Int,

    @SerializedName("IsDisabled")
    var IsDisabled: Boolean,

    @SerializedName("ModifierOptionId")
    var ModifierOptionId: Int,

    @SerializedName("SelectedQty")
    var SelectedQty:Int

) : Serializable {
    /*var Id: Int = 0

    var isChecked = false

    var name: String? = null*/

}

