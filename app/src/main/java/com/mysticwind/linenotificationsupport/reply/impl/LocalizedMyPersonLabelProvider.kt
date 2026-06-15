package com.mysticwind.linenotificationsupport.reply.impl

import android.content.Context
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.common.collect.ImmutableMap
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.reply.MyPersonLabelProvider
import com.mysticwind.linenotificationsupport.ui.LocaleDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Objects
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizedMyPersonLabelProvider @Inject constructor(
    private val localeDao: LocaleDao,
    @ApplicationContext private val context: Context
) : MyPersonLabelProvider {

    companion object {
        private const val ENGLISH_PREFIX = "en-"
        private const val ENGLISH_LABEL = "Me"
        private const val CHINESE_PREFIX = "zh-"
        private const val CHINESE_LABEL = "我"

        const val DEFAULT_LABEL = ENGLISH_LABEL

        private val PREFIX_TO_LABEL_MAP: Map<String, String> = ImmutableMap.of(
            ENGLISH_PREFIX, ENGLISH_LABEL,
            CHINESE_PREFIX, CHINESE_LABEL
        )
    }

    init {
        Objects.requireNonNull(localeDao)
        Objects.requireNonNull(context)
    }

    override fun getMyPersonLabel(): Optional<String> {
        val locale = localeDao.getLocale()
        for ((prefix, label) in PREFIX_TO_LABEL_MAP) {
            if (locale.startsWith(prefix)) {
                return Optional.of(label)
            }
        }
        return Optional.of(DEFAULT_LABEL)
    }

    override fun getMyPerson(): Person {
        return Person.Builder()
            .setName(getMyPersonLabel().get())
            .setIcon(IconCompat.createWithResource(context, R.drawable.outline_person_24))
            .build()
    }
}
