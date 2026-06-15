package com.mysticwind.linenotificationsupport.reply.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.mysticwind.linenotificationsupport.ui.LocaleDao;

import org.junit.Test;

public class LocalizedMyPersonLabelProviderTest {

    @Test
    public void getMyPersonLabel_americanEnglishLabel() {
        LocalizedMyPersonLabelProvider classUnderTest = buildWithLocale("en-US");

        assertEquals("Me", classUnderTest.getMyPersonLabel().get());
    }

    @Test
    public void getMyPersonLabel_taiwanChineseLabel() {
        LocalizedMyPersonLabelProvider classUnderTest = buildWithLocale("zh-rTW");

        assertEquals("我", classUnderTest.getMyPersonLabel().get());
    }

    @Test
    public void getMyPersonLabel_defaultLabel() {
        LocalizedMyPersonLabelProvider classUnderTest = buildWithLocale("xxx");

        assertEquals("Me", classUnderTest.getMyPersonLabel().get());
    }

    private LocalizedMyPersonLabelProvider buildWithLocale(String locale) {
        LocaleDao localeDao = mock(LocaleDao.class);
        when(localeDao.getLocale()).thenReturn(locale);
        return new LocalizedMyPersonLabelProvider(localeDao, mock(Context.class));
    }

}
