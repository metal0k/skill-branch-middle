package ru.skillbranch.skillarticles.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import ru.skillbranch.skillarticles.R

class EditProfileDialog: DialogFragment() {

    companion object {
        const val EDIT_PROFILE_KEY = "EDIT_PROFILE_KEY"
        const val EDIT_NAME_VALUE = "EDIT_NAME_VALUE"
        const val EDIT_ABOUT_VALUE = "EDIT_ABOUT_VALUE"

    }

    private val args: EditProfileDialogArgs by navArgs()

    lateinit var edtName: EditText
    lateinit var edtAbout: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = layoutInflater.inflate(R.layout.fragment_edit_profile_dialog, null) as ViewGroup;

        edtName = layout.findViewById<EditText>(R.id.edt_name)
            .apply { setText(savedInstanceState?.getString(EDIT_NAME_VALUE) ?: args.profileName) }
        edtAbout = layout.findViewById<EditText>(R.id.edt_about)
            .apply { setText(savedInstanceState?.getString(EDIT_ABOUT_VALUE) ?: args.profileAbout) }



        return AlertDialog.Builder(requireContext())
            .setTitle("Edit profile")
            .setPositiveButton("Save") { _, _ ->

                val valName = edtName.text.toString()
                val valAbout = edtAbout.text.toString()
                if (args.profileName == valName && args.profileAbout == valAbout)
                    return@setPositiveButton
                setFragmentResult(
                      EDIT_PROFILE_KEY, bundleOf( EDIT_NAME_VALUE to valName,
                    EDIT_ABOUT_VALUE to valAbout)
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setView(layout)
            .create()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EDIT_NAME_VALUE, edtName.text.toString())
        outState.putString(EDIT_ABOUT_VALUE, edtAbout.text.toString())
        super.onSaveInstanceState(outState)
    }
}