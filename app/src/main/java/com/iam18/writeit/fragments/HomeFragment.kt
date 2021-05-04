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
import com.iam18.writeit.entities.Notes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment() {

    var arrNotes = ArrayList<Notes>()
    var notesAdapter: NotesAdapter = NotesAdapter()
    private var storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var recordPermission = Manifest.permission.RECORD_AUDIO
    private var REQUEST_CODE = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getActivity()?.moveTaskToBack(true);
                getActivity()?.finish();
            }
        })
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
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

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(storagePermission,recordPermission), REQUEST_CODE)

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )

        launch {
            context?. let{
                var notes = NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                notesAdapter!!.setData(notes)
                arrNotes = notes as ArrayList<Notes>
                recycler_view.adapter = notesAdapter
            }
        }

        notesAdapter!!.setOnClickListener(onClicked)

        abouth1.setOnClickListener{
            var popUp = AboutPopUpFragment.newInstance()
            popUp.show(requireActivity().supportFragmentManager, "About Fragment")
        }

        qaMic.setOnClickListener{
            replaceFragment(CreateNoteFragment.newInstance(), "mic")
        }

        qaImage.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "img")
        }

        qaLink.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "lnk")
        }

        fabBtnCreateNote.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), "ad")
        }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                var tempArr = ArrayList<Notes>()

                for (arr in arrNotes) {
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString())) {
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

                var tempArr = ArrayList<Notes>()

                for (arr in arrNotes) {
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString())) {
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
        override fun onClicked(notesId: Int) {

            var fragment :Fragment
            var bundle = Bundle()
            bundle.putInt("noteId", notesId)
            fragment = CreateNoteFragment.newInstance()
            fragment.arguments = bundle

            val fragmentTransition = activity!!.supportFragmentManager.beginTransaction()
            fragmentTransition.replace(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
        }

    }

    private fun replaceFragment(fragment: Fragment, st: String){
        val fragmentTransition = activity!!.supportFragmentManager.beginTransaction()

        val bdl = Bundle()
        bdl.putString("qaCall", st)
        fragment.arguments = bdl

        fragmentTransition.replace(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

}