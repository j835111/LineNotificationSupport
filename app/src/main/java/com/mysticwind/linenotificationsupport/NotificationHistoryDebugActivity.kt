package com.mysticwind.linenotificationsupport

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mysticwind.linenotificationsupport.debug.history.ui.NotificationHistoryAdapter
import com.mysticwind.linenotificationsupport.debug.history.ui.NotificationHistoryViewModel

class NotificationHistoryDebugActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationHistoryAdapter
    private lateinit var notificationHistoryViewModel: NotificationHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_notification_history_view)

        val recyclerView = findViewById<RecyclerView>(R.id.notification_history_recyclerview)
        adapter = NotificationHistoryAdapter(NotificationHistoryAdapter.NotificationHistoryEntryDiff())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        notificationHistoryViewModel = ViewModelProvider(this).get(NotificationHistoryViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        notificationHistoryViewModel.notificationHistory.observe(this) { notificationHistory ->
            adapter.submitList(notificationHistory)
        }
    }

    override fun onPause() {
        super.onPause()

        notificationHistoryViewModel.notificationHistory.removeObservers(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
