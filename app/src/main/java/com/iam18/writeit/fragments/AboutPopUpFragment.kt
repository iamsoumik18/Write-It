package com.iam18.writeit.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.iam18.writeit.databinding.FragmentAboutPopupBinding


class AboutPopUpFragment: DialogFragment() {

    private var _binding: FragmentAboutPopupBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding =  FragmentAboutPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClose.setOnClickListener{
            dialog?.dismiss()
        }

        binding.github.setOnClickListener {
            val uri = Uri.parse("https://www.github.com/iamsoumik18")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.github.android")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/iamsoumik18")))
            }
        }

        binding.linkedIn.setOnClickListener {
            val uri = Uri.parse("https://www.linkedin.com/in/iamsoumik18")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.linkedin.android")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/iamsoumik18")))
            }
        }

        binding.insta.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}