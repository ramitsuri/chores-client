package com.ramitsuri.choresclient.android.ui.login

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentLoginBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentLoginBinding::inflate

    override fun setupViews() {
        viewModel.state.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Loading, ViewState.Reload -> {
                    log("Loading")
                    onLoading(true)
                }
                is ViewState.Error -> {
                    log("Error: ${viewState.error}")
                    onLoading(false)
                }

                is ViewState.Success -> {
                    log("Login success")
                    findNavController().navigate(R.id.action_loginFragment_to_assignmentsFragment)
                }
                is ViewState.Login -> {
                    onLoading(false)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val id = binding.editId.text.toString()
            val key = binding.editKey.text.toString()
            if (id.isEmpty() || key.isEmpty()) {
                return@setOnClickListener
            }
            viewModel.login(id, key)
        }
    }

    private fun onLoading(loading: Boolean) {
        val showContent = !loading
        binding.contentGroup.setVisibility(showContent)
        binding.progress.setVisibility(loading)
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}