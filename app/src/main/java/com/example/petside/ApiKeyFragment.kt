package com.example.petside

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petside.databinding.FragmentApiKeyBinding
import com.example.petside.network.AppClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

class ApiKeyFragment : Fragment() {

    private var _binding: FragmentApiKeyBinding? = null

    private val binding get() = _binding!!

    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiKeyBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apiKeyScreenNextButton.setOnClickListener {
            val appClient = AppClient.getInstance()

            disposable = appClient?.run {
                getFavourites(binding.apiKey.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            showDialog(
                                getString(R.string.auth_success_title),
                                getString(R.string.auth_success_description)
                            )
                        },
                        { error ->
                            if (error is HttpException) {
                                val errorBody = error.response()?.errorBody()?.string()

                                showDialog(
                                    getString(R.string.default_error_title),
                                    errorBody ?: getString(R.string.default_error_description)
                                )
                            } else {
                                showDialog(
                                    getString(R.string.default_error_title),
                                    getString(R.string.default_error_description)
                                )
                            }
                        },
                    )
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        ErrorDialogFragment(title, message).show(childFragmentManager, ErrorDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
    }
}