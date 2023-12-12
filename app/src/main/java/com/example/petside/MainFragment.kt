package com.example.petside

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.petside.data.SignUpRequestModel
import com.example.petside.databinding.FragmentMainBinding
import com.example.petside.network.AppClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private var disposable: Disposable? = null

    private val binding get() = _binding!!

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = checkButtonEnabled()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.apply {
            setOnClickListener {
                if (validateEmail()) {
                    val requestData = SignUpRequestModel(
                        binding.email.text.toString(),
                        binding.description.text.toString()
                    )
                    disposable = AppClient.getInstance()?.run {
                        signUp(requestData)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe (
                                {
                                    activity?.runOnUiThread {
                                        findNavController().navigate(R.id.action_mainFragment_to_apiKeyFragment)
                                    }
                                },
                                { error ->
                                    if (error is HttpException) {
                                        val errorBody = error.response()?.errorBody()?.string()

                                        showErrorDialog(errorBody ?: getString(R.string.default_error_description))
                                    } else {
                                        showErrorDialog(getString(R.string.default_error_description))
                                    }
                                },
                            )
                    }
                } else {
                    binding.emailLayout.error = getString(R.string.wrong_email)
                }
            }
            isEnabled = !(binding.email.text?.isEmpty() == true && binding.description.text?.isEmpty() == true)
        }

        binding.email.addTextChangedListener (textWatcher)

        binding.description.addTextChangedListener (textWatcher)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showErrorDialog(message: String) {
        ErrorDialogFragment(message = message).show(childFragmentManager, ErrorDialogFragment.TAG)
    }

    private fun validateEmail(): Boolean {
        val isValidEmail = isValidEmail(binding.email.text.toString())

        binding.emailLayout.error = if (!isValidEmail) getString(R.string.wrong_email) else ""

        return isValidEmail
    }

    private fun checkButtonEnabled() {
        val email = binding.email.text.toString()
        val description = binding.description.text.toString()

        binding.nextButton.isEnabled = email.isNotEmpty() && description.isNotEmpty()
    }
}