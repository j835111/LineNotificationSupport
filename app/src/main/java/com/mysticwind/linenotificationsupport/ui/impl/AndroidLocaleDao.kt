package com.mysticwind.linenotificationsupport.ui.impl

import android.content.res.Resources
import com.mysticwind.linenotificationsupport.ui.LocaleDao
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocaleDao @Inject constructor(
    private val resources: Resources
) : LocaleDao {

    init {
        Objects.requireNonNull(resources)
    }

    private var locale: String = ""

    init {
        updateLocale()
    }

    private fun updateLocale() {
        locale = resources.configuration.locales.get(0).toLanguageTag()
    }

    override fun getLocale(): String {
        return locale
    }

    override fun notifyLocaleChange() {
        updateLocale()
    }
}
