package com.mysticwind.linenotificationsupport.debug.history.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry

class NotificationHistoryEntryViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
    private val lineVersionTextView: TextView = itemView.findViewById(R.id.line_version_text_view)
    private val notificationEntryTextView: TextView = itemView.findViewById(R.id.notification_entry_text_view)

    fun bind(entry: NotificationHistoryEntry, position: Int) {
        timestampTextView.text = entry.recordDateTime
        lineVersionTextView.text = entry.lineVersion
        notificationEntryTextView.text = entry.notification

        if (position % 2 == 1) {
            itemView.setBackgroundColor(Color.GRAY)
        } else {
            itemView.setBackgroundColor(Color.WHITE)
        }
    }

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup): NotificationHistoryEntryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_history_entry, parent, false)
            return NotificationHistoryEntryViewHolder(view)
        }
    }
}
