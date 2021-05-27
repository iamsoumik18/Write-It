package com.iam18.writeit.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.iam18.writeit.R
import com.iam18.writeit.databinding.FragmentAboutPopupBinding


class AboutPopUpFragment: DialogFragment() {

    private var _binding: FragmentAboutPopupBinding? = null
    private val binding get() = _binding!!

    private val DialogFragment.window: Window? get() = dialog?.window

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
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding =  FragmentAboutPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSlideFromBottomAnimation()

        dialog?.setOnShowListener {
            Handler().post {
                setupSlideToBottomAnimation()
            }
        }

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

    private fun DialogFragment.setupSlideFromBottomAnimation() {
        window?.setWindowAnimations(R.style.SlideFromBottom)
    }

    private fun DialogFragment.setupSlideToBottomAnimation() {
        window?.setWindowAnimations(R.style.SlideToBottom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}