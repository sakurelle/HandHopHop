package ru.handhophop.core.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.datetime.Clock
import ru.handhophop.core.network.voucher.VoucherData
import ru.handhophop.core.network.voucher.VoucherNetworkService

object PremiumProvider {
    private var prefs: SharedPreferences? = null
    private const val IS_PREMIUM_KEY = "is_premium_user"
    private const val PREFS_NAME = "premium_prefs"
    private const val VOUCHER_CODE_KEY = "voucher_code"

    private const val SAVED_HASH_KEY = "saved_user_hash"
    private const val SAVED_INPUT_HASH_KEY = "saved_input_hash"
    private const val PREMIUM_EXPIRED_KEY = "premium_expired_at"

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ensureUserHashExists()
        }
    }

    private fun getPrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException(
            "PremiumProvider must be initialized with init(context) before use"
        )
    }


    fun isPremium(): Boolean {
        val hasStatus = getPrefs().getBoolean(IS_PREMIUM_KEY, false)
        val remaining = getRemainingTime()


        if (hasStatus && remaining <= 0) {
            handlePremiumExpiration()
            return false
        }

        return hasStatus && remaining > 0 && validateHashes()
    }


    fun ensureUserHashExists() {
        if (getUserHash().isEmpty()) {
            generateAndSaveNewUserHash()
        }
    }


    fun generateAndSaveNewUserHash() {//времено паблик
        val voucherService = VoucherNetworkService()
        val newHash = voucherService.getVoucher()
        getPrefs().edit().putString(SAVED_HASH_KEY, newHash).apply()
    }

    private fun handlePremiumExpiration() {
        getPrefs().edit()
            .putBoolean(IS_PREMIUM_KEY, false)
            .putString(SAVED_INPUT_HASH_KEY, "")
            .putString(VOUCHER_CODE_KEY, null)
            .apply()

        generateAndSaveNewUserHash()
    }

    fun getUserHash(): String = getPrefs().getString(SAVED_HASH_KEY, "") ?: ""

    fun getInputHash(): String = getPrefs().getString(SAVED_INPUT_HASH_KEY, "") ?: ""
    fun putInputHash(hash: String) {
        getPrefs().edit().putString(SAVED_INPUT_HASH_KEY, hash).apply()
    }

    fun validateHashes(): Boolean {
        val userHash = getUserHash()
        val inputHash = getInputHash()
        return userHash.isNotEmpty() && userHash == inputHash
    }

    fun setPremiumStatus(isPremium: Boolean, code: String? = null, durationMs: Long = 2592000000L) {
        val expirationTime = if (isPremium) System.currentTimeMillis() + durationMs else 0L

        getPrefs().edit().apply {
            putBoolean(IS_PREMIUM_KEY, isPremium)
            putString(VOUCHER_CODE_KEY, code)
            putLong(PREMIUM_EXPIRED_KEY, expirationTime)
            if (code != null) {
                putString(SAVED_INPUT_HASH_KEY, code)
            }
        }.apply()
    }
    fun getRemainingTime(): Long {
        val expiredAt = getPrefs().getLong(PREMIUM_EXPIRED_KEY, 0L)
        val currentTime = System.currentTimeMillis()
        val remaining = expiredAt - currentTime
        return if (remaining > 0) remaining else 0L
    }
}