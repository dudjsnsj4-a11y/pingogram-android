package dev.kyralabs.pingogram.credentials

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kyralabs.pingogram.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Telegram API credentials TDLib signs in with: the ones the build baked in from `secrets.properties`, unless the
 * user entered their own on the first screen, which are kept in this device's SharedPreferences and win from then on.
 *
 * Read synchronously: [TdlibConfigModule] builds TDLib's settings while the application starts, before any screen can
 * wait for a suspend read.
 */
@Singleton
class ApiCredentialsStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

        fun apiId(): Int = preferences.getInt(KEY_API_ID, BuildConfig.TELEGRAM_API_ID)

        fun apiHash(): String = preferences.getString(KEY_API_HASH, BuildConfig.TELEGRAM_API_HASH).orEmpty()

        /** Whether TDLib can start with these credentials: a positive id and a 32-character hexadecimal hash. */
        fun isConfigured(): Boolean = isValid(apiId(), apiHash())

        /**
         * Stores the credentials and waits until they are on disk: the caller restarts the process right after, and
         * [android.content.SharedPreferences.Editor.apply]'s asynchronous write would be lost with it.
         *
         * @return whether the write succeeded.
         */
        @SuppressLint("CommitPrefEdits") // The process is killed right after: apply()'s write would be lost.
        fun save(
            apiId: Int,
            apiHash: String,
        ): Boolean =
            preferences
                .edit()
                .putInt(KEY_API_ID, apiId)
                .putString(KEY_API_HASH, apiHash)
                .commit()

        companion object {
            private const val FILE_NAME = "api_credentials"
            private const val KEY_API_ID = "api_id"
            private const val KEY_API_HASH = "api_hash"
            private const val API_HASH_LENGTH = 32

            fun isValid(
                apiId: Int,
                apiHash: String,
            ): Boolean = apiId > 0 && apiHash.length == API_HASH_LENGTH && apiHash.all { it.isHexDigit() }

            private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
        }
    }
