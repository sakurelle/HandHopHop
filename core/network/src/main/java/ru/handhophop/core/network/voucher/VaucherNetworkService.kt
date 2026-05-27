@file:Suppress("DEPRECATION")

package ru.handhophop.core.network.voucher

import kotlinx.datetime.Clock


class VoucherNetworkService {
    private val currentPremium = VoucherData(
        id = "00983891",//1 в конце для диактив према
        expired = Clock.System.now().toEpochMilliseconds() + 2592000000L,
        createAt = 	Clock.System.now().toEpochMilliseconds()
    )

    fun verifyVoucher(inputVoucher: String): Result<Boolean> {

        val isValid = currentPremium.generateHash() == inputVoucher

        return if (isValid) {
            Result.success(true)
        } else {
            Result.success(false)
        }
    }
    fun getVoucher(): String {
        return currentPremium.generateHash()
    }
}