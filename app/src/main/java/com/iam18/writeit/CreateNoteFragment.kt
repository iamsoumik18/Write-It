package com.iam18.writeit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.iam18.writeit.database.NotesDatabase
import com.iam18.writeit.entities.Notes
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteFragment : BaseFragment() {

    var currentDate:String? = null
    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("noteId",-1)
        arguments?.let {
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                CreateNoteFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != -1){
            launch{
                context?.let {
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    etNoteTitle.setText(notes.title)
                    etNoteSubTitle.setText(notes.subTitle)
                    etNoteDesc.setText(notes.noteText)
                }
            }
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a")
        currentDate = sdf.format(Date())
        tvDateTime.text = currentDate

        imgDone.setOnClickListener{
            saveNote()
        }

        imgBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun saveNote(){

        if (etNoteTitle.text.isNullOrEmpty()){
            Toast.makeText(context,"Note Title is Required",Toast.LENGTH_SHORT).show()
        }
        else if (etNoteSubTitle.text.isNullOrEmpty()){

            Toast.makeText(context,"Note Sub Title is Required",Toast.LENGTH_SHORT).show()
        }

        else if (etNoteDesc.text.isNullOrEmpty()){

            Toast.makeText(context,"Note Description is Required",Toast.LENGTH_SHORT).show()
        }

        else{

            launch {
                var notes = Notes()
                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                context?.let {
                    NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                    etNoteTitle.setText("")
                    etNoteSubTitle.setText("")
                    etNoteDesc.setText("")
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }
}