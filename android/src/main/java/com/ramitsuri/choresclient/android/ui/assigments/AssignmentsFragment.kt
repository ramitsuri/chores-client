package com.ramitsuri.choresclient.android.ui.assigments

import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentsBinding
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssignmentsFragment: BaseFragment<FragmentAssignmentsBinding>() {

    private val viewModel: AssignmentsViewModel by viewModels()
    private val adapter = AssignmentsAdapter(listOf()) {taskAssignment, clickType ->
        onItemClickListener(taskAssignment, clickType)
    }

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAssignmentsBinding::inflate

    override fun setupViews() {
        binding.listAssignments.adapter = adapter
        binding.listAssignments.layoutManager = LinearLayoutManager(requireContext())
        viewModel.state.observe(viewLifecycleOwner) {viewState ->
            when (viewState) {
                is ViewState.Loading -> {
                    log("Loading")
                }
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                }

                is ViewState.Success -> {
                    adapter.update(viewState.data)
                }
                is ViewState.Reload -> {
                    viewModel.fetchAssignments()
                }
            }
        }
    }

    private fun onItemClickListener(taskAssignment: TaskAssignment, clickType: ClickType) {
        viewModel.changeStateRequested(taskAssignment, clickType)
    }

    private fun log(message: String) {
        Log.d("AssignmentsFragment", message)
    }

    companion object {

        fun newInstance() =
            AssignmentsFragment().apply {

            }
    }
}