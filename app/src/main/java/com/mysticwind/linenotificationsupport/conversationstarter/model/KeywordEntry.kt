package com.mysticwind.linenotificationsupport.conversationstarter.model

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import java.util.Objects
import java.util.Optional

class KeywordEntry(
    chatId: String,
    chatName: String,
    keyword: String?,
    @get:JvmName("isHasReplyAction") val hasReplyAction: Boolean
) {
    val chatId: String = Validate.notBlank(chatId)
    val chatName: String = Validate.notBlank(chatName)
    val keyword: Optional<String> = if (StringUtils.isBlank(keyword)) Optional.empty() else Optional.of(keyword!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeywordEntry) return false
        return hasReplyAction == other.hasReplyAction
                && Objects.equals(chatId, other.chatId)
                && Objects.equals(chatName, other.chatName)
                && Objects.equals(keyword, other.keyword)
    }

    override fun hashCode(): Int = Objects.hash(chatId, chatName, keyword, hasReplyAction)

    class Builder {
        private var chatId: String? = null
        private var chatName: String? = null
        private var keyword: String? = null
        private var hasReplyAction: Boolean = false

        fun chatId(chatId: String?) = apply { this.chatId = chatId }
        fun chatName(chatName: String?) = apply { this.chatName = chatName }
        fun keyword(keyword: String?) = apply { this.keyword = keyword }
        fun hasReplyAction(hasReplyAction: Boolean) = apply { this.hasReplyAction = hasReplyAction }

        fun build(): KeywordEntry = KeywordEntry(chatId!!, chatName!!, keyword, hasReplyAction)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
