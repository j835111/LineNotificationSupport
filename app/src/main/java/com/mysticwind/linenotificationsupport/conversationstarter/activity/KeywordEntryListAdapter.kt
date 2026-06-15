package com.mysticwind.linenotificationsupport.conversationstarter.activity

import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import java.util.Objects
import java.util.function.BiConsumer

class KeywordEntryListAdapter(
    diffCallback: DiffUtil.ItemCallback<MutableKeywordEntry>,
    chatIdAndKeywordUpdater: BiConsumer<String, String>
) : ListAdapter<MutableKeywordEntry, KeywordSettingViewHolder>(diffCallback) {

    private val chatIdAndKeywordUpdater: BiConsumer<String, String> = Objects.requireNonNull(chatIdAndKeywordUpdater)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordSettingViewHolder =
        KeywordSettingViewHolder.create(parent)

    override fun onBindViewHolder(holder: KeywordSettingViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, chatIdAndKeywordUpdater)
    }

    class KeywordEntryDiff : DiffUtil.ItemCallback<MutableKeywordEntry>() {

        override fun areItemsTheSame(
            @NonNull oldItem: MutableKeywordEntry,
            @NonNull newItem: MutableKeywordEntry
        ): Boolean = oldItem === newItem

        override fun areContentsTheSame(
            @NonNull oldItem: MutableKeywordEntry,
            @NonNull newItem: MutableKeywordEntry
        ): Boolean = oldItem.chatId == newItem.chatId
    }
}
