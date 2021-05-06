package com.iam18.writeit.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.iam18.writeit.R
import com.iam18.writeit.database.NotesDatabase
import com.iam18.writeit.entities.Notes
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteFragment : BaseFragment(),EasyPermissions.PermissionCallbacks,EasyPermissions.RationaleCallbacks {

    var selectedColor = "null"
    var currentDate:String? = null
    private var noteId = -1
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var webLink = ""
    private var qaK: String? = null
    private var isRecording = false
    private var recordPermission = Manifest.permission.RECORD_AUDIO
    private var REQUEST_CODE_AUDIO = 21
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var selectedAudioPath = ""
    private var recordFile = ""
    private var isPlaying = false
    private lateinit var updateSeekBar: Runnable
    private lateinit var seekBarHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("noteId", -1)
        qaK = requireArguments().getString("qaCall", "none")
        arguments?.let {
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
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
                    if(notes.color!="null") {
                        selectedColor = notes.color.toString()
                        colorView.setBackgroundColor(Color.parseColor(notes.color))
                        layoutPlayer2.setBackgroundColor(Color.parseColor(notes.color))
                    }
                    etNoteTitle.setText(notes.title)
                    etNoteSubTitle.setText(notes.subTitle)
                    etNoteDesc.setText(notes.noteText)
                    if (notes.imgPath != ""){
                        selectedImagePath = notes.imgPath!!
                        Glide.with(view).load(notes.imgPath).into(imgNote)
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    }else{
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }
                    if (notes.webLink != ""){
                        webLink = notes.webLink!!
                        tvWebLink.text = notes.webLink
                        layoutWebUrl.visibility = View.VISIBLE
                        etWebLink.setText(notes.webLink)
                        imgUrlDelete.visibility = View.VISIBLE
                    }else{
                        imgUrlDelete.visibility = View.GONE
                        layoutWebUrl.visibility = View.GONE
                    }
                    if(notes.audioPath != ""){
                        selectedAudioPath = notes.audioPath.toString()
                        layoutPlayer.visibility = View.VISIBLE
                    }else{
                        layoutPlayer.visibility = View.GONE
                    }
                }
            }
        }

        if(qaK=="img"){
            readStorageTask()
        }

        if(qaK=="lnk"){
            layoutWebUrl.visibility = View.VISIBLE
        }

        if(qaK=="mic"){
            recordAudio()
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a")
        currentDate = sdf.format(Date())
        tvDateTime.text = currentDate

        imgDone.setOnClickListener{
            if (noteId != -1){
                updateNote()
            }else{
                saveNote()
            }
        }

        imgBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        imgMoreFrame.setOnClickListener{
            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager, "Note Bottom Sheet Fragment")
        }

        imgDelete.setOnClickListener {
            selectedImagePath = ""
            layoutImage.visibility = View.GONE

        }

        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()){
                checkWebUrl()
            }else{
                Toast.makeText(requireContext(), "Url is Required.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            if (noteId != -1){
                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE
            }else{
                layoutWebUrl.visibility = View.GONE
            }

        }

        imgUrlDelete.setOnClickListener {
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        tvWebLink.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }

        play_pause.setOnClickListener {
            if(isPlaying){
                pauseAudio()
                isPlaying = false
            }else{
                if(mediaPlayer!= null){
                    resumeAudio()
                }else{
                    playAudio()
                }
                isPlaying = true
            }
        }

        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                pauseAudio()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                var progress = seekBar.progress
                mediaPlayer?.seekTo(progress)
                resumeAudio()
            }
        })

        player_delete.setOnClickListener {
            var file = File(selectedAudioPath)
            selectedAudioPath = ""
            if(file.exists()){
                file.deleteOnExit()
            }
            layoutPlayer.visibility = View.GONE
        }

        player_update.setOnClickListener {
            var file = File(selectedAudioPath)
            selectedAudioPath = ""
            if(file.exists()){
                file.delete()
            }
            layoutPlayer.visibility = View.GONE
            recordAudio()
        }

    }

    private  fun stopAudio(){
        play_pause.setImageResource(R.drawable.player_play_btn)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun pauseAudio(){
        mediaPlayer?.pause()
        isPlaying = false
        play_pause.setImageResource(R.drawable.player_play_btn)
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun resumeAudio(){
        mediaPlayer?.start()
        isPlaying = true
        play_pause.setImageResource((R.drawable.player_pause_btn))
        updateRunnable()
        seekBarHandler.postDelayed(updateSeekBar, 0)
    }

    private fun playAudio(){
        mediaPlayer = MediaPlayer()
        play_pause.setImageResource((R.drawable.player_pause_btn))
        try {
            mediaPlayer?.setDataSource(selectedAudioPath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }catch (e: IOException){
            e.printStackTrace()
        }

        isPlaying = true
        mediaPlayer!!.setOnCompletionListener {
            stopAudio()
        }

        seekbar.max = mediaPlayer!!.duration

        seekBarHandler = Handler()
        updateRunnable()
        seekBarHandler.postDelayed(updateSeekBar, 0)


    }

    private fun updateRunnable(){
        updateSeekBar = Runnable {
            seekbar.progress = mediaPlayer!!.currentPosition
            seekBarHandler.postDelayed(updateSeekBar, 500)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPlaying) {
            stopAudio()
        }
        if(isRecording){
            stopRecording()
        }
    }

    private fun saveNote(){

        if (etNoteTitle.text.isNullOrEmpty()){
            Toast.makeText(context, "Note Title is Required.", Toast.LENGTH_SHORT).show()
        }
        else if (etNoteSubTitle.text.isNullOrEmpty()){

            Toast.makeText(context, "Note Sub Title is Required.", Toast.LENGTH_SHORT).show()
        }

        else if (etNoteDesc.text.isNullOrEmpty()){

            Toast.makeText(context, "Note Description is Required.", Toast.LENGTH_SHORT).show()
        }

        else{

            launch {
                var notes = Notes()
                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.audioPath = selectedAudioPath
                notes.webLink = webLink
                context?.let {
                    NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                    etNoteTitle.setText("")
                    etNoteSubTitle.setText("")
                    etNoteDesc.setText("")
                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    tvWebLink.visibility = View.GONE
                    layoutPlayer?.visibility = View.GONE
                    Toast.makeText(context, "Note Added.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateNote(){
        launch {

            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink
                notes.audioPath = selectedAudioPath

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                etNoteTitle.setText("")
                etNoteSubTitle.setText("")
                etNoteDesc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                layoutPlayer?.visibility = View.GONE
                Toast.makeText(context, "Note Updated.", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun deleteNote(){

        launch {
            context?.let {
                NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                Toast.makeText(requireActivity(), "Note Deleted.", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun checkWebUrl(){
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()){
            layoutWebUrl.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            tvWebLink.visibility = View.VISIBLE
            tvWebLink.text = etWebLink.text.toString()
        }else{
            Toast.makeText(requireContext(), "Url is not valid.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recordAudio(){
        if(checkAudioPermission()){
            if(selectedAudioPath!=""){
                var file = File(selectedAudioPath)
                selectedAudioPath = ""
                if(file.exists()){
                    file.deleteOnExit()
                }
                layoutPlayer.visibility = View.GONE
            }
            recordNow()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(recordPermission), REQUEST_CODE_AUDIO)
        }
    }

    private fun recordNow(){
        layoutRecordUrl.visibility = View.VISIBLE
        record_btn.setOnClickListener {
            if(isRecording){
                //Stop Recording
                    stopRecording()
                record_btn.setImageResource(R.drawable.record_btn_pause)
                Toast.makeText(requireContext(), "Recording Stopped and Saved.", Toast.LENGTH_SHORT).show()
                isRecording = false
                layoutRecordUrl.visibility = View.GONE
                layoutPlayer.visibility = View.VISIBLE
            }else{
                //Start Recording
                    startRecording()
                record_btn.setImageResource(R.drawable.record_btn)
                isRecording = true
            }
        }
    }

    private fun startRecording(){

        recordTimer.base = SystemClock.elapsedRealtime()
        recordTimer.start()

        //Get app external directory path
        val recordPath = activity!!.getExternalFilesDir("/")!!.absolutePath

        //Get current date and time
        val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val now = Date()

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".3gp";

        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile("$recordPath/$recordFile");

        try {
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            Toast.makeText(requireContext(), "Recording Started.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopRecording(){

        recordTimer.stop()
        recordTimer.base = SystemClock.elapsedRealtime()
        mediaRecorder?.stop();
        mediaRecorder?.release();
        mediaRecorder = null;
        val recordPath = activity!!.getExternalFilesDir("/")!!.absolutePath
        selectedAudioPath = "$recordPath/$recordFile"

    }

    private fun checkAudioPermission(): Boolean{
        return ActivityCompat.checkSelfPermission(requireContext(), recordPermission) == PackageManager.PERMISSION_GRANTED
    }

    private val BroadcastReceiver : BroadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            var actionColor = p1!!.getStringExtra("action")

            when(actionColor!!){

                "Orange" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }

                }

                "Red" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Blue" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Green" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Purple" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Yellow" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        colorView.setBackgroundColor(Color.TRANSPARENT)
                        layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }

                "Image" -> {
                    readStorageTask()
                }

                "WebUrl" -> {
                    layoutWebUrl.visibility = View.VISIBLE
                }

                "Audio" -> {
                    recordAudio()
                }

                "DeleteNote" -> {
                    deleteNote()
                }

                else -> {
                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    layoutWebUrl.visibility = View.GONE
                }

            }
        }

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    private fun hasReadStoragePerm():Boolean{
        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun readStorageTask(){
        if (hasReadStoragePerm()){
            pickImageFromGallery()
        }else{
            EasyPermissions.requestPermissions(
                    requireActivity(),
                    "This app needs access to your storage to add images.",
                    READ_STORAGE_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageFromGallery(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath:String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                var selectedImageUrl = data.data
                if (selectedImageUrl != null){
                    try {
                        Glide.with(this).load(getPathFromUri(selectedImageUrl)).override(1280, 720).into(imgNote)
                        imgNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    }catch (e: Exception){
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requireActivity())
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {

    }
}