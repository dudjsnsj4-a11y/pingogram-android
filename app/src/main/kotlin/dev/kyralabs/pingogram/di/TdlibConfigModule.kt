package dev.kyralabs.pingogram.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.kyralabs.pingogram.BuildConfig
import dev.kyralabs.pingogram.core.tdlib.TdlibConfig
import dev.kyralabs.pingogram.credentials.ApiCredentialsStore

/**
 * TDLib settings from `BuildConfig`; the API credentials come from the git-ignored `secrets.properties`, unless the
 * user entered their own on the first screen ([ApiCredentialsStore]).
 */
@Module
@InstallIn(SingletonComponent::class)
object TdlibConfigModule {
    @Provides
    fun tdlibConfig(credentials: ApiCredentialsStore): TdlibConfig =
        TdlibConfig(
            apiId = credentials.apiId(),
            apiHash = credentials.apiHash(),
            appVersionName = BuildConfig.VERSION_NAME,
            isDebug = BuildConfig.DEBUG,
        )
}
