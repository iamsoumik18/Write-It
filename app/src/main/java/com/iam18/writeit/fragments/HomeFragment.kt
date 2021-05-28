package com.iam18.writeit.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.iam18.writeit.R
import com.iam18.writeit.adapter.NotesAdapter
import com.iam18.writeit.database.NotesDatabase
import com.iam18.writeit.databinding.FragmentHomeBinding
import com.iam18.writeit.entities.Notes
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var arrNotes = ArrayList<Notes>()
    var notesAdapter: NotesAdapter = NotesAdapter()
    private var storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var recordPermission = Manifest.permission.RECORD_AUDIO
    private var requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.moveTaskToBack(true)
                activity?.finish()
            }
        })
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(storagePermission,recordPermission), requestCode)

        launch {
            context?. let{
                val notes = NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                if(notes.isNotEmpty()){
                    notesAdapter.setData(notes)
                    arrNotes = notes as ArrayList<Notes>
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.alternate.visibility = View.GONE
                    binding.recyclerView.setHasFixedSize(true)
                    binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    binding.recyclerView.adapter = notesAdapter
                }else{
                    binding.recyclerView.visibility = View.GONE
                    binding.alternate.visibility = View.VISIBLE
                }
            }
        }

        notesAdapter.setOnClickListener(onClicked)

        binding.about.setOnClickListener{
            val popUp = AboutPopUpFragment.newInstance()
            popUp.show(requireActivity().supportFragmentManager, "About Fragment")
        }

        binding.qaMic.setOnClickListener{
            replaceFragment(CreateNoteFragment.newInstance(), "mic")
        }

        binding.qaImage.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "img")
        }

        binding.qaLink.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "lnk")
        }

        binding.fabBtnCreateNote.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "ad")
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                val tempArr = ArrayList<Notes>()

                for (arr in arrNotes) {
                    if (arr.title!!.lowercase(Locale.getDefault()).contains(p0.toString())) {
                        tempArr.add(arr)
                    }
                }
                if (tempArr.size != 0) {
                    notesAdapter.setData(tempArr)
                    notesAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireActivity(), "Sorry!!! Note not found.", Toast.LENGTH_SHORT)
                        .show()
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                val tempArr = ArrayList<Notes>()

                for (arr in arrNotes) {
                    if (arr.title!!.lowercase(Locale.getDefault()).contains(p0.toString())) {
                        tempArr.add(arr)
                    }
                }

                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }

        })

    }

    private val onClicked = object :NotesAdapter.OnItemClickListener{
        override fun onClicked(noteId: Int) {

            val fragment :Fragment
            val bundle = Bundle()
            bundle.putInt("noteId", noteId)
            fragment = CreateNoteFragment.newInstance()
            fragment.arguments = bundle

            val fragmentTransition = activity!!.supportFragmentManager.beginTransaction()
            fragmentTransition.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right, R.anim.enter_from_right,R.anim.exit_to_right)
            fragmentTransition.replace(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
        }

    }

    private fun replaceFragment(fragment: Fragment, st: String){
        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        val bdl = Bundle()
        bdl.putString("qaCall", st)
        fragment.arguments = bdl

        fragmentTransition.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right, R.anim.enter_from_right,R.anim.exit_to_right)

        fragmentTransition.replace(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}