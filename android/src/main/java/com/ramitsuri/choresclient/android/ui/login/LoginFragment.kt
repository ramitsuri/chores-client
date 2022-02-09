package com.ramitsuri.choresclient.android.ui.login

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.BuildConfig
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentLoginBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.system.exitProcess

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

        binding.btnLogin.setOnLongClickListener {
            if (BuildConfig.DEBUG) {
                showServerSelectionAlert()
                return@setOnLongClickListener true
            } else {
                return@setOnLongClickListener false
            }
        }

        if (BuildConfig.DEBUG) {
            binding.textServer.visibility = View.VISIBLE
            binding.textServer.text = viewModel.getServer()
        }
    }

    private fun showServerSelectionAlert() {
        if (!BuildConfig.DEBUG) {
            return
        }
        // Check if server has been set but hasn't been made available, then restart the app
        val savedServerAddress = viewModel.getServer()
        val showingServerAddress = binding.textServer.text
        if (savedServerAddress.isNotEmpty() && showingServerAddress != savedServerAddress) {
            val activity = requireActivity()
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            activity.finishAffinity()
            activity.startActivity(intent)
            exitProcess(0)
            return
        }
        val alert = AlertDialog.Builder(requireActivity())
        alert.setTitle(R.string.login_server_alert_title)
        val editText = EditText(requireActivity())
        alert.setView(editText)
        alert.setPositiveButton(R.string.ok) { _, _ ->
            viewModel.setDebugServer(editText.text.toString())
        }
        alert.setNegativeButton(R.string.cancel, null)
        alert.show()
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