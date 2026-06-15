package com.mysticwind.linenotificationsupport.ui.impl

import android.content.Context
import android.widget.Toast
import com.mysticwind.linenotificationsupport.ui.UserAlertDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastUserAlertDao @Inject constructor(
    @ApplicationContext private val context: Context
) : UserAlertDao {

    init {
        Objects.requireNonNull(context)
    }

    override fun notify(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
