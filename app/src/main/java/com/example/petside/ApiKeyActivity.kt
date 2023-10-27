package com.example.petside

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import com.example.petside.network.AppClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import android.graphics.drawable.ColorDrawable
import io.reactivex.rxjava3.disposables.Disposable


class ApiKeyActivity : AppCompatActivity() {

    private var nextButton: Button? = null
    private var apiKeyEditText: EditText? = null

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_key)

        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(getColor(android.R.color.transparent)))
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        nextButton = findViewById(R.id.apiKeyScreenNextButton)
        apiKeyEditText = findViewById(R.id.apiKey)

        nextButton?.setOnClickListener {
            val appClient = AppClient.getInstance()

            disposable = appClient?.run {
                getFavourites(apiKeyEditText?.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe (
                        {
                            showDialog(getString(R.string.auth_success_title), getString(R.string.auth_success_description))
                        },
                        { error ->
                            if (error is HttpException) {
                                val errorBody = error.response()?.errorBody()?.string()

                                showDialog(getString(R.string.default_error_title), errorBody ?: getString(R.string.default_error_description))
                            } else {
                                showDialog(getString(R.string.default_error_title), getString(R.string.default_error_description))
                            }
                        },
                    )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(title: String, message: String, ) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(message)
        alertBuilder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertBuilder.create()
        val positiveButtonColor = ContextCompat.getColor(this, R.color.primary10)

        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(positiveButtonColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}