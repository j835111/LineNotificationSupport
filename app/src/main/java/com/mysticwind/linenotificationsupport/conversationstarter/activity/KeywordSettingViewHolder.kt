package com.mysticwind.linenotificationsupport.conversationstarter.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mysticwind.linenotificationsupport.R
import org.apache.commons.lang3.StringUtils
import java.util.function.BiConsumer

class KeywordSettingViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val warningIcon: ImageView = itemView.findViewById(R.id.warning_icon)
    private val chatNameTextView: TextView = itemView.findViewById(R.id.chat_name_text_view)
    private val keywordEditText: EditText = itemView.findViewById(R.id.keyword_edit_text)

    fun bind(keywordEntry: MutableKeywordEntry, chatIdAndKeywordUpdater: BiConsumer<String, String>) {
        warningIcon.visibility = if (shouldShowWarning(keywordEntry)) View.VISIBLE else View.INVISIBLE
        chatNameTextView.text = keywordEntry.chatName
        keywordEditText.setText(keywordEntry.keyword)
        keywordEditText.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            val originalKeyword = keywordEntry.keyword
            val newKeyword = editText.text.toString().trim()
            keywordEntry.keyword = newKeyword
            editText.setText(newKeyword)
            if (newKeyword != originalKeyword) {
                chatIdAndKeywordUpdater.accept(keywordEntry.chatId ?: "", newKeyword)
                warningIcon.visibility = if (shouldShowWarning(keywordEntry)) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    private fun shouldShowWarning(keywordEntry: MutableKeywordEntry): Boolean =
        StringUtils.isNotBlank(keywordEntry.keyword) && !keywordEntry.hasReplyAction

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup): KeywordSettingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.keyword_setting_row_item, parent, false)
            return KeywordSettingViewHolder(view)
        }
    }
}
