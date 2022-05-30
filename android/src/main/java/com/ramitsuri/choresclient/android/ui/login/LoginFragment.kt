package com.ramitsuri.choresclient.android.ui.login

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ramitsuri.choresclient.android.BuildConfig
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.databinding.FragmentLoginBinding
import com.ramitsuri.choresclient.android.extensions.setVisibility
import com.ramitsuri.choresclient.android.ui.BaseFragment
import com.ramitsuri.choresclient.model.ViewEvent
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

class LoginFragment : BaseFragment<FragmentLoginBinding>(), KoinComponent {

    private val logger: LogHelper by inject()
    private val viewModel: LoginViewModel by viewModel()

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentLoginBinding::inflate

    override fun setupViews() {
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
                        logger.d(TAG, "Login success")
                        findNavController().navigate(R.id.action_loginFragment_to_assignmentsFragment)
                    }
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

    private fun onViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.LOADING, ViewEvent.RELOAD -> {
                logger.d(TAG, "Loading")
                onLoading(true)
            }
            ViewEvent.LOGIN -> {
                onLoading(false)
            }
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
        }
        val alert = AlertDialog.Builder(requireActivity())
        alert.setTitle(R.string.login_server_alert_title)
        val editText = EditText(requireActivity())
        editText.setText(viewModel.getServer())
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

    companion object {
        private const val TAG = "LoginFragment"
    }
}