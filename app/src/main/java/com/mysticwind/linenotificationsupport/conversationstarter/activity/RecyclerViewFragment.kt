package com.mysticwind.linenotificationsupport.conversationstarter.activity

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.conversationstarter.ChatKeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.BiConsumer
import javax.inject.Inject

@AndroidEntryPoint
class RecyclerViewFragment : Fragment() {

    companion object {
        private const val TAG = "RecyclerViewFragment"
        private const val KEY_LAYOUT_MANAGER = "layoutManager"
    }

    internal enum class LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    internal var mCurrentLayoutManagerType: LayoutManagerType? = null
    protected var mRecyclerView: RecyclerView? = null
    protected var mLayoutManager: RecyclerView.LayoutManager? = null

    var keywordSettingViewModel: KeywordSettingViewModel? = null

    @Inject
    lateinit var chatKeywordDao: ChatKeywordDao

    @Inject
    lateinit var conversationStarterNotificationManager: ConversationStarterNotificationManager

    @Inject
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.keyword_setting_recycler_view_fragment, container, false)
        rootView.tag = TAG

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = LinearLayoutManager(activity)

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            @Suppress("DEPRECATION")
            mCurrentLayoutManagerType = savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType?
        }
        setRecyclerViewLayoutManager()

        val chatIdAndKeywordUpdater = BiConsumer<String, String> { chatId, keyword ->
            chatKeywordDao.createOrUpdateKeyword(chatId, keyword)

            // delay the notification updates as keyword updates are async
            // probably need to publish events whenever keyword dao finishes the updates
            handler.postDelayed({
                conversationStarterNotificationManager.publishNotification()
            }, 500)
        }

        val adapter = KeywordEntryListAdapter(KeywordEntryListAdapter.KeywordEntryDiff(), chatIdAndKeywordUpdater)

        keywordSettingViewModel = ViewModelProvider(this).get(KeywordSettingViewModel::class.java)
        keywordSettingViewModel!!.getAllKeywords().observe(viewLifecycleOwner) { keywords ->
            adapter.submitList(keywords)
        }

        mRecyclerView!!.adapter = adapter
        mRecyclerView!!.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        // END_INCLUDE(initializeRecyclerView)

        return rootView
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     */
    fun setRecyclerViewLayoutManager() {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView?.layoutManager != null) {
            scrollPosition = (mRecyclerView!!.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        mLayoutManager = LinearLayoutManager(activity)
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER

        mRecyclerView?.layoutManager = mLayoutManager
        mRecyclerView?.scrollToPosition(scrollPosition)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType)
        super.onSaveInstanceState(savedInstanceState)
    }
}
