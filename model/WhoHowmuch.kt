package com.R.R.model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class WhoHowmuch(var whoWillPay : String? = "",var groupName : String? = "", var whohasPaid: Int = 0, var whoBought : String? = "",var howmuchWillpay : Double = 0.0,var totalPrice : Double = 0.0,
var billname : String? = "", var photoLocation : String? = "",var snapKeyOfGroup : String? = "", val createTime : Date? = null
){
}