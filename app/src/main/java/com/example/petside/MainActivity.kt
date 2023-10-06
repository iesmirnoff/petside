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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
                val intent = Intent(this, ApiKeyActivity::class.java)
                startActivity(intent)
            } else {
                emailTextInputLayout!!.error = "Incorrect email"
                //showErrorDialog("Too many api key revisions have been issued for this email. Contact support for more help.")
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