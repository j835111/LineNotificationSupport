package com.mysticwind.linenotificationsupport.reply.impl

import android.content.Context
import com.mysticwind.linenotificationsupport.ui.LocaleDao
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LocalizedMyPersonLabelProviderTest {

    @Test
    fun getMyPersonLabel_americanEnglishLabel() {
        val classUnderTest = buildWithLocale("en-US")

        assertEquals("Me", classUnderTest.getMyPersonLabel().get())
    }

    @Test
    fun getMyPersonLabel_taiwanChineseLabel() {
        val classUnderTest = buildWithLocale("zh-rTW")

        assertEquals("我", classUnderTest.getMyPersonLabel().get())
    }

    @Test
    fun getMyPersonLabel_defaultLabel() {
        val classUnderTest = buildWithLocale("xxx")

        assertEquals("Me", classUnderTest.getMyPersonLabel().get())
    }

    private fun buildWithLocale(locale: String): LocalizedMyPersonLabelProvider {
        val localeDao = mock<LocaleDao>()
        whenever(localeDao.getLocale()).thenReturn(locale)
        return LocalizedMyPersonLabelProvider(localeDao, mock<Context>())
    }
}
