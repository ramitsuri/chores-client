package com.ramitsuri.choresclient.android.ui.assigments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.AssignmentItemBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.utils.formatInstant
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import java.time.Instant

class AssignmentsAdapter(
    items: List<TaskAssignment>,
    private val clickListener: (TaskAssignment, ClickType) -> Unit
):
    RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {
    private val items = mutableListOf<TaskAssignment>()

    init {
        this.items.addAll(items)
    }

    fun update(items: List<TaskAssignment>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            AssignmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(itemBinding) {position, clickType ->
            clickListener(items[position], clickType)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskAssignment: TaskAssignment = items[position]
        holder.bind(taskAssignment)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(
        private val binding: AssignmentItemBinding,
        clickAtPosition: (Int, ClickType) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

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
            binding.textTitle.text = taskAssignment.task.name
            binding.textAssignedTo.text = getString(
                R.string.assignment_assigned, taskAssignment.member.name
            )
            binding.textDueDateTime.text = getString(
                R.string.assignment_due, formatInstant(taskAssignment.dueDateTime, Instant.now())
            )

            binding.textRepeats.setVisibility(taskAssignment.task.repeatUnit != RepeatUnit.NONE)
            binding.textRepeats.text = getString(
                R.string.assignment_repeats, getRepeatsString(
                    taskAssignment.task.repeatValue, taskAssignment.task.repeatUnit
                )
            )


            when (taskAssignment.progressStatus) {
                ProgressStatus.TODO -> {
                    binding.btnChangeState.text = getString(R.string.assignment_mark_completed)
                }
                else -> {
                    binding.btnChangeState.setVisibility(false)
                }
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
}

fun <T: RecyclerView.ViewHolder> T.whenClicked(clickListener: (position: Int) -> Unit): T {
    itemView.setOnClickListener {
        clickListener.invoke(adapterPosition)
    }
    return this
}