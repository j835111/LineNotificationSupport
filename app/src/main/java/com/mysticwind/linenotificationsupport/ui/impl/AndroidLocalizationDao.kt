package com.mysticwind.linenotificationsupport.ui.impl

import android.content.Context
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocalizationDao @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalizationDao {

    init {
        Objects.requireNonNull(context)
    }

    override fun getLocalizedString(resourceId: Int, vararg arguments: Any): String {
        return context.getString(resourceId, *arguments)
    }
}
