package com.ramitsuri.choresclient.android.ui.miscellaneous

import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentMiscellaneousBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MiscellaneousFragment: BaseFragment<FragmentMiscellaneousBinding>() {
    private val viewModel: MiscellaneousViewModel by viewModels()
    private val adapter = AssignmentsAdapter(listOf()) {_, _ ->
    }

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentMiscellaneousBinding::inflate

    override fun setupViews() {
        binding.listAssignments.adapter = adapter
        binding.listAssignments.layoutManager = LinearLayoutManager(requireContext())
        viewModel.state.observe(viewLifecycleOwner) {viewState ->
            when (viewState) {
                is ViewState.Loading -> {
                    log("Loading")
                    binding.listAssignments.setVisibility(false)
                    binding.progress.setVisibility(true)
                }
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                    binding.listAssignments.setVisibility(true)
                    binding.progress.setVisibility(false)
                }

                is ViewState.Success -> {
                    adapter.update(viewState.data)
                    binding.listAssignments.setVisibility(true)
                    binding.progress.setVisibility(false)
                }
                is ViewState.Reload -> {
                    viewModel.fetchAssignments()
                }
            }
        }

        binding.btnSetUserId.setOnClickListener {
            showUserIdAlert()
        }
    }

    private fun showUserIdAlert() {
        val alert = AlertDialog.Builder(requireActivity())
        alert.setTitle(R.string.miscellaneous_user_id_alert_title)
        val editText = EditText(requireActivity())
        alert.setView(editText)
        alert.setPositiveButton(R.string.ok) {_, _ ->
            viewModel.userIdSet(editText.text.toString())
        }
        alert.setNegativeButton(R.string.cancel, null)
        alert.show()
    }

    private fun log(message: String) {
        Log.d("MiscellaneousFragment", message)
    }
}