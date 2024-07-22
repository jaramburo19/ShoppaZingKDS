package com.byteswiz.parentmodel
import java.io.Serializable

data class CompleteOrderRequest(
    val order: ParentModel,
    val orderStatusId: Int,
    val textDuration:String
): Serializable {
}
data class ParentModel (
    val OrderTypeId : Int,
    val OrderRefNo : String = "",
    val children : List<KDSCartItem>,
    val KDSOrderDate: String ="",
    val diningOptionName: String,
    val ticketName: String?,
    val receiptNo: String,
    val qrcode: String,
    var orderStatusId: Int,
    var IsSynced: Boolean,
    var qNo: String?,
    var TodaysOrderNo: String,
    var IsCompleted:Boolean?,
    var ticketGuidId: String?,
    var localUniqueId: String?
):Serializable

data class KDSCartItem(
    var itemId: Long?,
    var CombiName: String,
    var quantity: Double = 0.0,
    var modifiers: List<KDSModifiersModel>?,
    var SpecialInstructions: String?,
    var IfNotAvailable: Int?,
    var IsDone: Boolean?): Serializable


class KDSModifiersModel(
    var Id: Int,

    var isChecked: Boolean,

    var name: String,

    var SKUId: Int,

    var Price: Double,

    var ModifierId: Long,

    var ModifierName: String,

    var isHeader: Boolean,

    var SelectedTypeId: Int,

    var MaxQty: Int,

    var IsRequired: Boolean,

    var DisplayName: String,

    var MinQty: Int,

    var IsDisabled: Boolean,

    var ModifierOptionId: Int,

    var SelectedQty:Int

) : Serializable {
    /*var Id: Int = 0

    var isChecked = false

    var name: String? = null*/

}

