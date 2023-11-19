package com.R.R.model

import kotlinx.serialization.Serializable

@Serializable
data class WhoHowmuch(var whoWillPay : String? = "",var groupName : String? = "", var whohasPaid: Int = 0, var whoBought : String? = "",var howmuchWillpay : Double = 0.0,var totalPrice : Double = 0.0,
var billname : String? = ""){
}