package com.example.petside

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.petside.data.SignUpRequestModel
import com.example.petside.network.AppApi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    var emailEditText: TextInputEditText? = null
    var emailTextInputLayout: TextInputLayout? = null
    var descriptionEditText: TextInputEditText? = null

    var nextButton: Button? = null

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            checkButtonEnabled()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nextButton = findViewById(R.id.next_button)
        emailEditText = findViewById(R.id.email)
        emailTextInputLayout = findViewById(R.id.emailLayout)
        descriptionEditText = findViewById(R.id.description)

        emailEditText!!.addTextChangedListener(textWatcher)
        descriptionEditText!!.addTextChangedListener(textWatcher)

        nextButton!!.setOnClickListener {
            if (validateEmail()) {
                val context = this

                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.thecatapi.com/v1/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                val appApi = retrofit.create(AppApi::class.java)

                val requestData = SignUpRequestModel(
                    emailEditText!!.text.toString(),
                    descriptionEditText!!.text.toString()
                )
                appApi.signUp(requestData)
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe (
                         {
                             runOnUiThread {
                                 val intent = Intent(context, ApiKeyActivity::class.java)
                                 startActivity(intent)
                             }
                         },
                         { error ->
                             if (error is HttpException) {
                                 val errorBody = error.response()?.errorBody()?.string()

                                 showErrorDialog(errorBody ?: "Что-то пошло не так…")
                             } else {
                                 showErrorDialog("Что-то пошло не так…")
                             }
                         },
                     )
            } else {
                emailTextInputLayout!!.error = "Incorrect email"
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
        alertBuilder.setTitle("Error")
        alertBuilder.setMessage(message)
        alertBuilder.setPositiveButton("Ok") { dialog, _ ->
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
            emailTextInputLayout!!.error = "Incorrect email"
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
}