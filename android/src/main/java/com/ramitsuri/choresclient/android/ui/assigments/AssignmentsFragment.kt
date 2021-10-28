package com.ramitsuri.choresclient.android.ui.assigments

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentsBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssignmentsFragment: BaseFragment<FragmentAssignmentsBinding>() {

    private val viewModel: AssignmentsViewModel by viewModels()
    private val adapter = AssignmentsAdapter(listOf()) {taskAssignment, clickType ->
        onItemClickListener(taskAssignment, clickType)
    }

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAssignmentsBinding::inflate

    override fun setupViews() {
        binding.listAssignments.addItemDecoration(
            ItemDecorator(
                resources.getDimensionPixelSize(R.dimen.margin_recycler_view)
            )
        )
        binding.listAssignments.adapter = adapter
        binding.listAssignments.layoutManager = LinearLayoutManager(requireContext())
        viewModel.state.observe(viewLifecycleOwner) {viewState ->
            when (viewState) {
                is ViewState.Loading -> {
                    log("Loading")
                    onLoading(true)
                }
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                    onLoading(false)
                }

                is ViewState.Success -> {
                    adapter.update(viewState.data.assignments)
                    binding.filterGroup.setOnCheckedChangeListener(null)
                    when (viewState.data.selectedFilter) {
                        is FilterMode.ALL -> {
                            binding.filterAll.isChecked = true
                        }
                        is FilterMode.OTHER -> {
                            binding.filterOther.isChecked = true
                        }
                        is FilterMode.MINE -> {
                            binding.filterMine.isChecked = true
                        }
                        is FilterMode.NONE -> {
                            binding.filterAll.isChecked = true
                        }
                    }
                    onLoading(false)
                }
                is ViewState.Reload -> {
                    viewModel.fetchAssignments(true)
                }
            }
        }

        binding.btnMisc.setOnClickListener {
            findNavController().navigate(R.id.action_assignmentsFragment_to_miscellaneousFragment)
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.fetchAssignments(false)
        }
        setupFilters()
    }

    private fun setupFilters() {
        binding.filterGroup.setOnCheckedChangeListener {group, checkedId ->
            when (checkedId) {
                binding.filterAll.id -> {
                    log("All")
                    viewModel.filterAll()
                }
                binding.filterMine.id -> {
                    log("Mine")
                    viewModel.filterMine()
                }
                binding.filterOther.id -> {
                    log("Other")
                    viewModel.filterExceptMine()
                }
            }
        }
    }

    private fun onItemClickListener(taskAssignment: TaskAssignment, clickType: ClickType) {
        viewModel.changeStateRequested(taskAssignment, clickType)
    }

    private fun onLoading(loading: Boolean) {
        val showContent = !loading
        binding.filterGroup.setVisibility(showContent)
        binding.listAssignments.setVisibility(showContent)
        binding.btnRefresh.setVisibility(showContent)
        binding.btnMisc.setVisibility(showContent)
        binding.progress.setVisibility(loading)
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}