package com.mysticwind.linenotificationsupport.conversationstarter.activity

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mysticwind.linenotificationsupport.conversationstarter.ChatKeywordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class KeywordSettingViewModel @Inject constructor(
    application: android.app.Application,
    private val chatKeywordManager: ChatKeywordManager
) : AndroidViewModel(application) {

    private val keywordEntryConverter = KeywordEntryConverter()

    init {
        Objects.requireNonNull(chatKeywordManager)
    }

    fun getAllKeywords(): LiveData<List<MutableKeywordEntry>> {
        val keywords = chatKeywordManager.getAllChatsWithConfiguredKeywords()
            .map { keywordEntry -> keywordEntryConverter.convert(keywordEntry)!! }

        // TODO make this really LiveData
        val liveData = MutableLiveData<List<MutableKeywordEntry>>()
        liveData.value = keywords
        return liveData
    }

    fun update(chatId: String, keyword: String) {
        // TODO
        Timber.d("Update keyword [%s] for chat ID [%s]", keyword, chatId)
    }
}
