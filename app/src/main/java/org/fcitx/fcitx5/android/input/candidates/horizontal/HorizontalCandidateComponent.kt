/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2024 Fcitx5 for Android Contributors
 */

package org.fcitx.fcitx5.android.input.candidates.horizontal

import android.content.res.Configuration
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.widget.PopupMenu
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.FcitxEvent
import org.fcitx.fcitx5.android.daemon.launchOnReady
import org.fcitx.fcitx5.android.data.InputFeedbacks
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.bar.ExpandButtonStateMachine.BooleanKey.ExpandedCandidatesEmpty
import org.fcitx.fcitx5.android.input.bar.ExpandButtonStateMachine.TransitionEvent.ExpandedCandidatesUpdated
import org.fcitx.fcitx5.android.input.bar.KawaiiBarComponent
import org.fcitx.fcitx5.android.input.broadcast.InputBroadcastReceiver
import org.fcitx.fcitx5.android.input.candidates.CandidateItemUi
import org.fcitx.fcitx5.android.input.candidates.CandidateViewHolder
import org.fcitx.fcitx5.android.input.candidates.expanded.decoration.FlexboxVerticalDecoration
import org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateMode.AlwaysFillWidth
import org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateMode.AutoFillWidth
import org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateMode.NeverFillWidth
import org.fcitx.fcitx5.android.input.dependency.UniqueViewComponent
import org.fcitx.fcitx5.android.input.dependency.context
import org.fcitx.fcitx5.android.input.dependency.fcitx
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.fcitx.fcitx5.android.input.dependency.theme
import org.fcitx.fcitx5.android.utils.item
import org.mechdancer.dependency.manager.must
import splitties.dimensions.dp
import splitties.resources.styledColor
import kotlin.math.max

class HorizontalCandidateComponent :
    UniqueViewComponent<HorizontalCandidateComponent, RecyclerView>(), InputBroadcastReceiver {

    private val service by manager.inputMethodService()
    private val context by manager.context()
    private val fcitx by manager.fcitx()
    private val theme by manager.theme()
    private val bar: KawaiiBarComponent by manager.must()

    // Since expanded candidate window is created once the expand button was clicked,
    // we need to replay the last offset
    private val _expandedCandidateOffset = MutableSharedFlow<Int>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val expandedCandidateOffset = _expandedCandidateOffset.asSharedFlow()

    private fun refreshExpanded(childCount: Int) {
        _expandedCandidateOffset.tryEmit(childCount)
        bar.expandButtonStateMachine.push(
            ExpandedCandidatesUpdated,
            ExpandedCandidatesEmpty to (adapter.total == childCount)
        )
    }

    val adapter: HorizontalCandidateViewAdapter by lazy {
        object : HorizontalCandidateViewAdapter(theme) {
            override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                // MODIFIED: 移除了 updateLayoutParams 的逻辑，让 ViewHolder 自适应宽度
                holder.itemView.setOnClickListener {
                    fcitx.launchOnReady { it.select(holder.idx) }
                }
                holder.itemView.setOnLongClickListener {
                    showCandidateActionMenu(holder.idx, candidates[position], holder.ui)
                    true
                }
            }
        }
    }

    val layoutManager: FlexboxLayoutManager by lazy {
        object : FlexboxLayoutManager(context) {
            // MODIFIED: 允许水平滚动
            override fun canScrollHorizontally() = true
            override fun canScrollVertically() = false

            override fun onLayoutCompleted(state: RecyclerView.State) {
                super.onLayoutCompleted(state)
                // MODIFIED: 移除了 secondLayoutPassNeeded 的复杂逻辑
                refreshExpanded(this.childCount)
            }
        }.apply {
            // ADDED: 确保所有候选项在同一行，不换行
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.NOWRAP
        }
    }

    private val dividerDrawable by lazy {
        ShapeDrawable(RectShape()).apply {
            val intrinsicSize = max(1, context.dp(1))
            intrinsicWidth = intrinsicSize
            intrinsicHeight = intrinsicSize
            paint.color = theme.dividerColor
        }
    }

    override val view by lazy {
        // MODIFIED: 移除了 onSizeChanged 回调
        RecyclerView(context).apply {
            id = R.id.candidate_view
            adapter = this@HorizontalCandidateComponent.adapter
            layoutManager = this@HorizontalCandidateComponent.layoutManager
            addItemDecoration(FlexboxVerticalDecoration(dividerDrawable))
            // ADDED: 隐藏水平滚动条，UI更美观
            isHorizontalScrollBarEnabled = false
        }
    }

    override fun onCandidateUpdate(data: FcitxEvent.CandidateListEvent.Data) {
        val candidates = data.candidates
        val total = data.total

        // MODIFIED: 移除了所有与 fillStyle, maxSpanCount, minWidth, flexGrow 相关的逻辑
        // 现在逻辑非常简单，直接更新 adapter 即可
        adapter.updateCandidates(candidates, total)

        // not sure why empty candidates won't trigger `FlexboxLayoutManager#onLayoutCompleted()`
        if (candidates.isEmpty()) {
            refreshExpanded(0)
        }
    }

    private fun triggerCandidateAction(idx: Int, actionIdx: Int) {
        fcitx.runIfReady { triggerCandidateAction(idx, actionIdx) }
    }

    private var candidateActionMenu: PopupMenu? = null

    fun showCandidateActionMenu(idx: Int, text: String, ui: CandidateItemUi) {
        candidateActionMenu?.dismiss()
        candidateActionMenu = null
        service.lifecycleScope.launch {
            val actions = fcitx.runOnReady { getCandidateActions(idx) }
            if (actions.isEmpty()) return@launch
            InputFeedbacks.hapticFeedback(ui.root, longPress = true)
            candidateActionMenu = PopupMenu(context, ui.root).apply {
                menu.add(buildSpannedString {
                    bold {
                        color(context.styledColor(android.R.attr.colorAccent)) {
                            append(text)
                        }
                    }
                }).apply {
                    isEnabled = false
                }
                actions.forEach { action ->
                    menu.item(action.text) {
                        triggerCandidateAction(idx, action.id)
                    }
                }
                setOnDismissListener {
                    candidateActionMenu = null
                }
                show()
            }
        }
    }
}