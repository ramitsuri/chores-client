package com.ramitsuri.choresclient.android.ui.assigments

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentsBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.ui.BaseFragment
import com.ramitsuri.choresclient.android.ui.decoration.ItemDecorator
import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.model.ViewEvent
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentsFragment : BaseFragment<FragmentAssignmentsBinding>(), KoinComponent {

    private val logger: LogHelper by inject()
    private val viewModel: AssignmentsViewModel by viewModel()
    private val adapter = AssignmentsAdapter(listOf()) { taskAssignment, clickType ->
        onItemClickListener(taskAssignment, clickType)
    }

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAssignmentsBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activity != null) {
                    (activity as AppCompatActivity).finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun setupViews() {
        binding.listAssignments.addItemDecoration(
            ItemDecorator(
                resources.getDimensionPixelSize(R.dimen.margin_recycler_view)
            )
        )
        binding.listAssignments.adapter = adapter
        binding.listAssignments.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launchWhenResumed {
            viewModel.state.collect { viewState ->
                when (viewState) {
                    is ViewState.Event -> {
                        onViewEvent(viewState.event)
                    }
                    is ViewState.Error -> {
                        logger.v(TAG, "Error: ${viewState.error}")
                        onLoading(false)
                    }

                    is ViewState.Success -> {
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
                        adapter.update(viewState.data.assignments, allowEdits())
                        setupFilters()
                        onLoading(false)
                    }
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchAssignments(false)
        }

        binding.btnMenu.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.btnMenu)
            popup.menuInflater.inflate(R.menu.assignments_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener false
            }
            popup.show()
        }

        binding.btnMenu.setOnLongClickListener {
            viewModel.toggleLogging()
            return@setOnLongClickListener true
        }

        setupFilters()
    }

    private fun onViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.LOADING -> {
                logger.d(TAG, "Loading")
                onLoading(true)
            }
            ViewEvent.LOGIN -> {
                logger.d(TAG, "Should not happen")
            }
            ViewEvent.RELOAD -> {
                logger.d(TAG, "Reload")
                viewModel.fetchAssignments(true)
            }
        }
    }

    private fun setupFilters() {
        binding.filterGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.filterMine.id -> {
                    logger.d(TAG, "Mine")
                    viewModel.filterMine()
                }
                binding.filterOther.id -> {
                    logger.d(TAG, "Other")
                    viewModel.filterExceptMine()
                }
            }
        }
    }

    private fun onItemClickListener(taskAssignment: TaskAssignment, clickType: ClickType) {
        when (clickType) {
            ClickType.CHANGE_STATUS -> {
                viewModel.changeStateRequested(taskAssignment)
            }
            ClickType.DETAIL -> {
                logger.d(TAG, "Detail requested")
                if (!allowEdits()) {
                    return
                }
                setFragmentResultListener(AssignmentDetailsFragment.REQUEST_DONE_STATUS) { _, bundle ->
                    if (bundle[AssignmentDetailsFragment.BUNDLE_DONE] as? Boolean == true) {
                        onViewEvent(ViewEvent.RELOAD)
                    }
                }
                val action =
                    AssignmentsFragmentDirections.actionAssignmentDetails(taskAssignment.id)
                findNavController().navigate(action)
            }
        }
    }

    private fun allowEdits() = binding.filterMine.isChecked

    private fun onLoading(loading: Boolean) {
        val showContent = !loading
        binding.swipeRefresh.isRefreshing = loading
        binding.filterGroup.setVisibility(showContent)
        binding.swipeRefresh.setVisibility(showContent)
        binding.btnMenu.setVisibility(showContent)
        binding.progress.setVisibility(loading)
    }

    companion object {
        private const val TAG = "AssignmentsFragment"
    }
}