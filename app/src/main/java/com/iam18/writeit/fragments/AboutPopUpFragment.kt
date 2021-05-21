package com.iam18.writeit.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.iam18.writeit.R
import kotlinx.android.synthetic.main.fragment_about_popup.*


class AboutPopUpFragment: DialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
                AboutPopUpFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonclose.setOnClickListener{
            dialog?.dismiss()
        }

        github.setOnClickListener {
            val uri = Uri.parse("https://www.github.com/iamsoumik18")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.github.android")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/iamsoumik18")))
            }
        }

        linkedIn.setOnClickListener {
            val uri = Uri.parse("https://www.linkedin.com/in/iamsoumik18")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.linkedin.android")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/iamsoumik18")))
            }
        }

        insta.setOnClickListener {
            val uri = Uri.parse("http://instagram.com/_u/iiamsoumik")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.instagram.android")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/iiamsoumik")))
            }
        }

    }

}