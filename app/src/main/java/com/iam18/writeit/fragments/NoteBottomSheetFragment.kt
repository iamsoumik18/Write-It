package com.iam18.writeit.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iam18.writeit.R
import com.iam18.writeit.databinding.FragmentBottomSheetBinding

class NoteBottomSheetFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        var noteId = -1
        private var selectedColor = "null"
        fun newInstance(id:Int,col:String): NoteBottomSheetFragment {
            val args = Bundle()
            val fragment = NoteBottomSheetFragment()
            fragment.arguments = args
            noteId = id
            selectedColor = col
            return fragment
        }
    }

    @SuppressLint("RestrictedApi", "InflateParams")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_bottom_sheet,null)
        dialog.setContentView(view)

        val param = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams

        val behavior = param.behavior

        if (behavior is BottomSheetBehavior<*>){
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    dismiss()
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

            })
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (noteId != -1){
            binding.layoutDeleteNote.visibility = View.VISIBLE
        }else{
            binding.layoutDeleteNote.visibility = View.GONE
        }
        when (selectedColor) {
            "#ff7f50" -> {
                binding.imgNote1.setImageResource(R.drawable.ic_tick)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(0)
            }
            "#ff493e" -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(R.drawable.ic_tick)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(0)
            }
            "#3c50ff" -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(R.drawable.ic_tick)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(0)
            }
            "#00dc89" -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(R.drawable.ic_tick)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(0)
            }
            "#bb86fc" -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(R.drawable.ic_tick)
                binding.imgNote6.setImageResource(0)
            }
            "#ffde03" -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(R.drawable.ic_tick)
            }
            else -> {
                binding.imgNote1.setImageResource(0)
                binding.imgNote2.setImageResource(0)
                binding.imgNote3.setImageResource(0)
                binding.imgNote4.setImageResource(0)
                binding.imgNote5.setImageResource(0)
                binding.imgNote6.setImageResource(0)
            }
        }
        setListener()
    }

    private fun setListener(){

        binding.fNote1.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Orange")
            intent.putExtra("selectedColor","#ff7f50")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.fNote2.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Red")
            intent.putExtra("selectedColor","#ff493e")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.fNote3.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Blue")
            intent.putExtra("selectedColor","#3c50ff")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.fNote4.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Green")
            intent.putExtra("selectedColor","#00dc89")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.fNote5.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Purple")
            intent.putExtra("selectedColor","#bb86fc")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.fNote6.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Yellow")
            intent.putExtra("selectedColor","#ffde03")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.imgMoreFrame.setOnClickListener {
            dismiss()
        }

        binding.layoutImage.setOnClickListener{
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Image")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.layoutWebUrl.setOnClickListener{
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","WebUrl")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.layoutAudio.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","Audio")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        binding.layoutDeleteNote.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action","DeleteNote")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}