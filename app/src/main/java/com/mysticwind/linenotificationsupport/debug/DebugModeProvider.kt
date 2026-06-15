package com.mysticwind.linenotificationsupport.debug

import com.mysticwind.linenotificationsupport.BuildConfig
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugModeProvider @Inject constructor() {

    fun isDebugMode(): Boolean {
        return StringUtils.equals(BuildConfig.BUILD_TYPE, "debug")
    }
}
