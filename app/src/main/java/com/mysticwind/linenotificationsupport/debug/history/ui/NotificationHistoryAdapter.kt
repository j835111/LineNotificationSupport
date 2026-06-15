package com.mysticwind.linenotificationsupport.debug.history.ui

import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry
import org.apache.commons.lang3.StringUtils

class NotificationHistoryAdapter(
    diffCallback: DiffUtil.ItemCallback<NotificationHistoryEntry>
) : ListAdapter<NotificationHistoryEntry, NotificationHistoryEntryViewHolder>(diffCallback) {

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): NotificationHistoryEntryViewHolder {
        return NotificationHistoryEntryViewHolder.create(parent)
    }

    override fun onBindViewHolder(@NonNull holder: NotificationHistoryEntryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, position)
    }

    class NotificationHistoryEntryDiff : DiffUtil.ItemCallback<NotificationHistoryEntry>() {

        override fun areItemsTheSame(
            @NonNull oldItem: NotificationHistoryEntry,
            @NonNull newItem: NotificationHistoryEntry
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            @NonNull oldItem: NotificationHistoryEntry,
            @NonNull newItem: NotificationHistoryEntry
        ): Boolean {
            return oldItem.id == newItem.id &&
                    StringUtils.equals(oldItem.recordDateTime, newItem.recordDateTime) &&
                    StringUtils.equals(oldItem.lineVersion, newItem.lineVersion) &&
                    StringUtils.equals(oldItem.notification, newItem.notification)
        }
    }
}
