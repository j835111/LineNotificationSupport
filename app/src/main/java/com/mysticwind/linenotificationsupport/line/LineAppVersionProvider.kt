package com.mysticwind.linenotificationsupport.line

import android.content.pm.PackageManager
import com.mysticwind.linenotificationsupport.line.Constants.LINE_PACKAGE_NAME
import timber.log.Timber
import java.util.Objects
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineAppVersionProvider @Inject constructor(
    private val packageManager: PackageManager
) {

    init {
        Objects.requireNonNull(packageManager)
    }

    fun getLineAppVersion(): Optional<String> {
        // https://stackoverflow.com/questions/50795458/android-how-to-get-any-application-version-by-package-name
        return try {
            val packageInfo = packageManager.getPackageInfo(LINE_PACKAGE_NAME, 0)
            Optional.ofNullable(packageInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "LINE not installed. Package: $LINE_PACKAGE_NAME")
            Optional.empty()
        }
    }
}
