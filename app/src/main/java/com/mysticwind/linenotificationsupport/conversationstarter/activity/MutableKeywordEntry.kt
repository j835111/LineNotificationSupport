package com.mysticwind.linenotificationsupport.conversationstarter.activity

class MutableKeywordEntry {
    var chatId: String? = null
    var chatName: String? = null
    var keyword: String? = null
    var hasReplyAction: Boolean = false

    class Builder {
        private val entry = MutableKeywordEntry()

        fun chatId(chatId: String?) = apply { entry.chatId = chatId }
        fun chatName(chatName: String?) = apply { entry.chatName = chatName }
        fun keyword(keyword: String?) = apply { entry.keyword = keyword }
        fun hasReplyAction(hasReplyAction: Boolean) = apply { entry.hasReplyAction = hasReplyAction }

        fun build(): MutableKeywordEntry = entry
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
