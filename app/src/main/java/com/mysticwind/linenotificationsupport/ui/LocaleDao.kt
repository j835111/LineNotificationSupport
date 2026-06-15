package com.mysticwind.linenotificationsupport.ui

interface LocaleDao {
    fun getLocale(): String
    fun notifyLocaleChange()
}
