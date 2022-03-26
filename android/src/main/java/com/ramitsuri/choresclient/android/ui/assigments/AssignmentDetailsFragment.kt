package com.ramitsuri.choresclient.android.ui.assigments

import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentDetailsBinding
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseBottomSheetFragment
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssignmentDetailsFragment : BaseBottomSheetFragment<FragmentAssignmentDetailsBinding>() {

    private val viewModel: AssignmentDetailsViewModel by viewModels()

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAssignmentDetailsBinding::inflate

    override fun setupViews() {
        viewModel.state.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                    dismiss()
                }

                is ViewState.Success -> {
                    showTaskAssignmentDetails(viewState.data.assignment)
                }
                else -> {
                    // Not used here
                }
            }
        }

        binding.buttonDone.setOnClickListener {
            viewModel.onComplete()
            setFragmentResult(REQUEST_DONE_STATUS, bundleOf(BUNDLE_DONE to true))
            findNavController().navigateUp()
        }
        binding.buttonSnoozeHours.setOnClickListener {
            viewModel.onSnoozeHour()
            dismiss()
        }
        binding.buttonSnoozeDay.setOnClickListener {
            viewModel.onSnoozeDay()
            dismiss()
        }
    }

    private fun showTaskAssignmentDetails(taskAssignment: TaskAssignment) {
        val task = taskAssignment.task
        binding.textTitle.text = task.name
        binding.textDescription.text = task.description
        binding.textRepeats.text =
            requireContext().formatRepeatUnit(task.repeatValue, task.repeatUnit)
    }

    private fun log(message: String) {
        Timber.d(message)
    }

    companion object {
        const val REQUEST_DONE_STATUS = "request_done_status"
        const val BUNDLE_DONE = "bundle_done"
        fun newInstance(): AssignmentDetailsFragment {
            return AssignmentDetailsFragment()
        }
    }
}