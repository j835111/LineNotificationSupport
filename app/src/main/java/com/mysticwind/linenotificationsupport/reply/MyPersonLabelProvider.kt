package com.mysticwind.linenotificationsupport.reply

import androidx.core.app.Person
import java.util.Optional

interface MyPersonLabelProvider {
    fun getMyPersonLabel(): Optional<String>
    fun getMyPerson(): Person
}
