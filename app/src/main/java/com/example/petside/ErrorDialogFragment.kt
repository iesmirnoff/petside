package com.example.petside

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment (private val title: String? = null, private val message: String): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return  AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setTitle(if (title !== null) title else getString(R.string.default_error_title))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->  dialog.dismiss()}
            .create()
    }

    companion object {
        const val TAG = "ErrorDialog"
    }
}