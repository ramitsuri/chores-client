package com.ramitsuri.choresclient.android.ui.miscellaneous

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentMiscellaneousBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsAdapter
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import com.ramitsuri.choresclient.android.ui.decoration.ItemDecorator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MiscellaneousFragment: BaseFragment<FragmentMiscellaneousBinding>() {
    private val viewModel: MiscellaneousViewModel by viewModels()
    private val adapter = AssignmentsAdapter(listOf()) {_, _ ->
    }

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentMiscellaneousBinding::inflate

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
                            // Do nothing
                        }
                        is FilterMode.OTHER -> {
                            binding.filterOther.isChecked = true
                        }
                        is FilterMode.MINE -> {
                            binding.filterMine.isChecked = true
                        }
                        is FilterMode.NONE -> {
                            // Do nothing
                        }
                    }
                    setupFilters()
                    onLoading(false)
                }
                is ViewState.Reload -> {
                    viewModel.fetchAssignments()
                }
            }
        }
        setupFilters()
    }

    private fun setupFilters() {
        binding.filterGroup.setOnCheckedChangeListener {group, checkedId ->
            when (checkedId) {
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

    private fun onLoading(loading: Boolean) {
        val showContent = !loading
        binding.filterGroup.setVisibility(showContent)
        binding.listAssignments.setVisibility(showContent)
        binding.progress.setVisibility(loading)
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}