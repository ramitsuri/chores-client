package com.ramitsuri.choresclient.android.ui.assigments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.AssignmentHeaderBinding
import com.ramitsuri.choresclient.android.databinding.AssignmentItemBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.android.ui.decoration.StickyHeaderItemDecoration
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit

class AssignmentsAdapter(
    items: List<TaskAssignmentWrapper>,
    private val clickListener: (TaskAssignment, ClickType) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    StickyHeaderItemDecoration.StickyHeaderInterface {
    private val items = mutableListOf<TaskAssignmentWrapper>()
    private var showCompleteButton = false

    init {
        this.items.addAll(items)
    }

    fun update(items: List<TaskAssignmentWrapper>, showCompleteButton: Boolean) {
        this.showCompleteButton = showCompleteButton
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val itemBinding =
                AssignmentHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(itemBinding)
        } else {
            val itemBinding =
                AssignmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(itemBinding) { position, clickType ->
                items[position].itemView?.let { itemView ->
                    clickListener(itemView, clickType)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val wrapper = items[position]
        if (holder is HeaderViewHolder) {
            wrapper.headerView?.let {
                holder.bind(it)
            }
        } else if (holder is ItemViewHolder) {
            wrapper.itemView?.let {
                holder.bind(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val wrapper = items[position]
        return if (wrapper.headerView != null) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    inner class HeaderViewHolder(
        private val binding: AssignmentHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(headerView: String) {
            binding.textTitle.text = headerView
        }
    }

    inner class ItemViewHolder(
        private val binding: AssignmentItemBinding,
        clickAtPosition: (Int, ClickType) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickAtPosition(adapterPosition, ClickType.DETAIL)
                }
            }

            binding.btnChangeState.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickAtPosition(adapterPosition, ClickType.CHANGE_STATUS)
                }
            }
            binding.root.context
        }

        fun bind(taskAssignment: TaskAssignment) {
            binding.btnChangeState.visibility = if (showCompleteButton) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.textTitle.text = taskAssignment.task.name

            binding.textRepeats.setVisibility(taskAssignment.task.repeatUnit != RepeatUnit.NONE)
            binding.textRepeats.text =
                getRepeatsString(taskAssignment.task.repeatValue, taskAssignment.task.repeatUnit)


            if (taskAssignment.progressStatus != ProgressStatus.TODO) {
                binding.btnChangeState.setVisibility(false)
            }
        }

        private fun getString(@StringRes resId: Int, formatArg: String? = null): String {
            return if (formatArg == null) {
                binding.root.context.getString(resId)
            } else {
                binding.root.context.getString(resId, formatArg)
            }
        }

        private fun getRepeatsString(repeatValue: Int, repeatUnit: RepeatUnit): String {
            return binding.root.context.formatRepeatUnit(repeatValue, repeatUnit)
        }
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var position = itemPosition
        while (position >= 0) {
            if (getItemViewType(itemPosition) == TYPE_HEADER) {
                break
            }
            position--
        }
        return position
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return getItemViewType(itemPosition) == TYPE_HEADER
    }

    override val headerLayout: Int
        get() = R.layout.assignment_header

    override fun bindHeaderData(header: View, headerPosition: Int) {
        if (headerPosition == RecyclerView.NO_POSITION) {
            return
        }
        val wrapper = items[headerPosition]
        if (wrapper.headerView != null) {
            header.findViewById<TextView>(R.id.textTitle)?.text = wrapper.headerView
        }
    }

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER = 1
    }
}

fun <T : RecyclerView.ViewHolder> T.whenClicked(clickListener: (position: Int) -> Unit): T {
    itemView.setOnClickListener {
        clickListener.invoke(adapterPosition)
    }
    return this
}