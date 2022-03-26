package com.ramitsuri.choresclient.android.ui.assigments

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentAssignmentsBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewEvent
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import com.ramitsuri.choresclient.android.ui.decoration.ItemDecorator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssignmentsFragment : BaseFragment<FragmentAssignmentsBinding>() {

    private val viewModel: AssignmentsViewModel by viewModels()
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
        viewModel.state.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Event -> {
                    onViewEvent(viewState.event)
                }
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                    onLoading(false)
                }

                is ViewState.Success -> {
                    binding.filterGroup.setOnCheckedChangeListener(null)
                    var showCompleteButton = false
                    when (viewState.data.selectedFilter) {
                        is FilterMode.ALL -> {
                            // Do nothing
                        }
                        is FilterMode.OTHER -> {
                            binding.filterOther.isChecked = true
                        }
                        is FilterMode.MINE -> {
                            binding.filterMine.isChecked = true
                            showCompleteButton = true
                        }
                        is FilterMode.NONE -> {
                            // Do nothing
                        }
                    }
                    adapter.update(viewState.data.assignments, showCompleteButton)
                    setupFilters()
                    onLoading(false)
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchAssignments(false)
        }

        binding.btnMenu.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.btnMenu)
            popup.menuInflater.inflate(R.menu.assignments_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                return@setOnMenuItemClickListener false
            }
            popup.show()
        }

        setupFilters()
    }

    private fun onViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.LOADING -> {
                log("Loading")
                onLoading(true)
            }
            ViewEvent.LOGIN -> {
                log("Should not happen")
            }
            ViewEvent.RELOAD -> {
                log("Reload")
                viewModel.fetchAssignments(true)
            }
        }
    }

    private fun setupFilters() {
        binding.filterGroup.setOnCheckedChangeListener { group, checkedId ->
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

    private fun onItemClickListener(taskAssignment: TaskAssignment, clickType: ClickType) {
        when (clickType) {
            ClickType.CHANGE_STATUS -> {
                viewModel.changeStateRequested(taskAssignment)
            }
            ClickType.DETAIL -> {
                log("Detail requested")
                setFragmentResultListener(AssignmentDetailsFragment.REQUEST_DONE_STATUS) { _, bundle ->
                    if (bundle[AssignmentDetailsFragment.BUNDLE_DONE] as? Boolean == true) {
                        onViewEvent(ViewEvent.RELOAD)
                    }
                }
                val action = AssignmentsFragmentDirections.actionAssignmentDetails(taskAssignment)
                findNavController().navigate(action)
            }
        }
    }

    private fun onLoading(loading: Boolean) {
        val showContent = !loading
        binding.swipeRefresh.isRefreshing = loading
        binding.filterGroup.setVisibility(showContent)
        binding.swipeRefresh.setVisibility(showContent)
        binding.btnMenu.setVisibility(showContent)
        binding.progress.setVisibility(loading)
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}