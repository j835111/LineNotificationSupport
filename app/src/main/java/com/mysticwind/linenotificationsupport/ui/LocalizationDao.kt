package com.mysticwind.linenotificationsupport.ui

interface LocalizationDao {
    fun getLocalizedString(resourceId: Int, vararg arguments: Any): String
}
