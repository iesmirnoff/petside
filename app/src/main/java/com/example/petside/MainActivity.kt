package com.example.petside

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.petside.data.SignUpRequestModel
import com.example.petside.network.AppClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

const val MAIN_ACTIVITY_PREFERENCES = "MAIN_ACTIVITY_PREFERENCES"
const val EMAIL_DATA = "EMAIL_DATA"
const val DESCRIPTION_DATA = "DESCRIPTION_DATA"

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private var emailEditText: TextInputEditText? = null
    private var emailTextInputLayout: TextInputLayout? = null
    private var descriptionEditText: TextInputEditText? = null

    private var nextButton: Button? = null

    private var disposable: Disposable? = null

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            checkButtonEnabled()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences(MAIN_ACTIVITY_PREFERENCES, Context.MODE_PRIVATE)

        val emailData = sharedPreferences.getString(EMAIL_DATA, "")
        val descriptionData = sharedPreferences.getString(DESCRIPTION_DATA, "")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nextButton = findViewById(R.id.next_button)
        emailEditText = findViewById(R.id.email)
        emailTextInputLayout = findViewById(R.id.emailLayout)
        descriptionEditText = findViewById(R.id.description)

        emailEditText?.setText(emailData)
        descriptionEditText?.setText(descriptionData)
        nextButton?.isEnabled = !(emailData!!.isEmpty() && descriptionData!!.isEmpty())

        emailEditText!!.addTextChangedListener(textWatcher)
        descriptionEditText!!.addTextChangedListener(textWatcher)

        nextButton!!.setOnClickListener {
            if (validateEmail()) {
                val context = this

                val requestData = SignUpRequestModel(
                    emailEditText!!.text.toString(),
                    descriptionEditText!!.text.toString()
                )
                disposable = AppClient.getInstance()
                .signUp(requestData)
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe (
                         {
                             runOnUiThread {
                                 val editor = sharedPreferences.edit()
                                 editor.putString(EMAIL_DATA, emailEditText!!.text.toString())
                                 editor.putString(DESCRIPTION_DATA, descriptionEditText!!.text.toString())
                                 editor.apply()

                                 val intent = Intent(context, ApiKeyActivity::class.java)
                                 startActivity(intent)
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
            } else {
                emailTextInputLayout!!.error = getString(R.string.wrong_email)
            }
        }

        emailEditText!!.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail()
            }

            checkButtonEnabled()
        }
    }

    private fun showErrorDialog(message: String) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(getString(R.string.default_error_title))
        alertBuilder.setMessage(message)
        alertBuilder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertBuilder.create()
        val positiveButtonColor = ContextCompat.getColor(this, R.color.primary10)

        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(positiveButtonColor)
    }

    private fun validateEmail(): Boolean {
        val emailText = emailEditText!!.text.toString()

        val isValidEmail = isValidEmail(emailText)

        if (!isValidEmail) {
            emailTextInputLayout!!.error = getString(R.string.wrong_email)
        } else {
            emailTextInputLayout!!.error = ""
        }

        return isValidEmail
    }

    private fun checkButtonEnabled() {
        val email = emailEditText!!.text.toString()
        val description = descriptionEditText!!.text.toString()

        nextButton!!.isEnabled = email.isNotEmpty() && description.isNotEmpty()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}