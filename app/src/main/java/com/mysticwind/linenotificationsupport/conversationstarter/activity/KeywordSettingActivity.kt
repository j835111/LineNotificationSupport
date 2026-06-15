package com.mysticwind.linenotificationsupport.conversationstarter.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mysticwind.linenotificationsupport.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KeywordSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keyword_setting_activity)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = RecyclerViewFragment()
            transaction.replace(R.id.keyword_setting_fragment, fragment)
            transaction.commit()
        }
    }
}
