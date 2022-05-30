package com.ramitsuri.choresclient.android.ui.assigments

import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentDetailsBinding
import com.ramitsuri.choresclient.android.ui.BaseBottomSheetFragment
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentDetailsFragment : BaseBottomSheetFragment<FragmentAssignmentDetailsBinding>(),
    KoinComponent {

    private val logger: LogHelper by inject()
    private val args: AssignmentDetailsFragmentArgs by navArgs()
    private val viewModel: AssignmentDetailsViewModel by viewModel()

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAssignmentDetailsBinding::inflate

    override fun setupViews() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setAssignmentId(args.assignmentId)
                viewModel.state.collect { viewState ->
                    when (viewState) {
                        is ViewState.Error -> {
                            logger.v(TAG, "Error: ${viewState.error}")
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

    private fun showTaskAssignmentDetails(details: AssignmentDetails) {
        binding.textTitle.text = details.name
        binding.textDescription.text = details.description
        binding.textRepeats.text =
            requireContext().formatRepeatUnit(details.repeatValue, details.repeatUnit)
        binding.textReminderTime.text = requireContext().formatReminderAt(details.notificationTime)
    }

    companion object {
        private const val TAG = "AssignmentDetailsFragment"
        const val REQUEST_DONE_STATUS = "request_done_status"
        const val BUNDLE_DONE = "bundle_done"
    }
}