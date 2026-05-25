package ru.handhophop.core.network.voucher


data class VoucherData(
    val id: String,
    val expired:Long,
    val createAt:Long,
){
    fun generateHash(): String {
        return "${id}_${expired}_${createAt}".hashCode().toString()
    }
}
