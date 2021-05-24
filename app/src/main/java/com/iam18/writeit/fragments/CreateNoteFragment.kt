package com.iam18.writeit.fragments

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.iam18.writeit.R
import com.iam18.writeit.database.NotesDatabase
import com.iam18.writeit.databinding.FragmentCreateNoteBinding
import com.iam18.writeit.entities.Notes
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteFragment : BaseFragment() {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    var selectedColor = "null"
    private var currentDate:String? = null
    private var noteId = -1
    private var selectedImagePath = ""
    private var webLink = ""
    private var qaK: String? = null
    private var isRecording = false
    private var recordPermission = Manifest.permission.RECORD_AUDIO
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var selectedAudioPath = ""
    private var recordFile = ""
    private var isPlaying = false
    private lateinit var updateSeekBar: Runnable
    private lateinit var seekBarHandler: Handler
    private var storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var requestCode = 1

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
    ): View {
        _binding =  FragmentCreateNoteBinding.inflate(inflater, container, false)
        return binding.root
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
                    val notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    if(notes.color!="null") {
                        selectedColor = notes.color.toString()
                        binding.colorView.setBackgroundColor(Color.parseColor(notes.color))
                        binding.layoutPlayer2.setBackgroundColor(Color.parseColor(notes.color))
                    }
                    binding.etNoteTitle.setText(notes.title)
                    binding.etNoteSubTitle.setText(notes.subTitle)
                    binding.etNoteDesc.setText(notes.noteText)
                    if (notes.imgPath != ""){
                        selectedImagePath = notes.imgPath!!
                        Glide.with(view).load(notes.imgPath).into(binding.imgNote)
                        binding.layoutImage.visibility = View.VISIBLE
                        binding.imgNote.visibility = View.VISIBLE
                        binding.imgDelete.visibility = View.VISIBLE
                    }else{
                        binding.layoutImage.visibility = View.GONE
                        binding.imgNote.visibility = View.GONE
                        binding.imgDelete.visibility = View.GONE
                    }
                    if (notes.webLink != ""){
                        webLink = notes.webLink!!
                        binding.tvWebLink.text = notes.webLink
                        binding.layoutWebUrl.visibility = View.VISIBLE
                        binding.etWebLink.setText(notes.webLink)
                        binding.imgUrlDelete.visibility = View.VISIBLE
                    }else{
                        binding.imgUrlDelete.visibility = View.GONE
                        binding.layoutWebUrl.visibility = View.GONE
                    }
                    if(notes.audioPath != ""){
                        selectedAudioPath = notes.audioPath.toString()
                        binding.layoutPlayer.visibility = View.VISIBLE
                    }else{
                        binding.layoutPlayer.visibility = View.GONE
                    }
                }
            }
        }

        if(qaK=="img"){
            readStorageTask()
        }

        if(qaK=="lnk"){
            binding.layoutWebUrl.visibility = View.VISIBLE
        }

        if(qaK=="mic"){
            recordAudio()
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                broadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a",Locale.ENGLISH)
        currentDate = sdf.format(Date())
        binding.tvDateTime.text = currentDate

        binding.imgDone.setOnClickListener{
            if (noteId != -1){
                updateNote()
            }else{
                saveNote()
            }
        }

        binding.imgBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imgMoreFrame.setOnClickListener{
            val noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager, "Note Bottom Sheet Fragment")
        }

        binding.imgDelete.setOnClickListener {
            selectedImagePath = ""
            binding.layoutImage.visibility = View.GONE

        }

        binding.btnOk.setOnClickListener {
            if (binding.etWebLink.text.toString().trim().isNotEmpty()){
                checkWebUrl()
            }else{
                Toast.makeText(requireContext(), "Url is Required.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            if (noteId != -1){
                binding.tvWebLink.visibility = View.VISIBLE
                binding.layoutWebUrl.visibility = View.GONE
            }else{
                binding.layoutWebUrl.visibility = View.GONE
            }

        }

        binding.imgUrlDelete.setOnClickListener {
            webLink = ""
            binding.tvWebLink.visibility = View.GONE
            binding.imgUrlDelete.visibility = View.GONE
            binding.layoutWebUrl.visibility = View.GONE
        }

        binding.tvWebLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.etWebLink.text.toString()))
            startActivity(intent)
        }

        binding.playPause.setOnClickListener {
            isPlaying = if(isPlaying){
                pauseAudio()
                false
            }else{
                if(mediaPlayer!= null){
                    resumeAudio()
                }else{
                    playAudio()
                }
                true
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                pauseAudio()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                mediaPlayer?.seekTo(progress)
                resumeAudio()
            }
        })

        binding.playerDelete.setOnClickListener {
            val file = File(selectedAudioPath)
            selectedAudioPath = ""
            if(file.exists()){
                file.deleteOnExit()
            }
            binding.layoutPlayer.visibility = View.GONE
        }

        binding.playerUpdate.setOnClickListener {
            val file = File(selectedAudioPath)
            selectedAudioPath = ""
            if(file.exists()){
                file.delete()
            }
            binding.layoutPlayer.visibility = View.GONE
            recordAudio()
        }

    }

    private  fun stopAudio(){
        binding.playPause.setImageResource(R.drawable.player_play_btn)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun pauseAudio(){
        mediaPlayer?.pause()
        isPlaying = false
        binding.playPause.setImageResource(R.drawable.player_play_btn)
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun resumeAudio(){
        mediaPlayer?.start()
        isPlaying = true
        binding.playPause.setImageResource((R.drawable.player_pause_btn))
        updateRunnable()
        seekBarHandler.postDelayed(updateSeekBar, 0)
    }

    private fun playAudio(){
        mediaPlayer = MediaPlayer()
        binding.playPause.setImageResource((R.drawable.player_pause_btn))
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

        binding.seekbar.max = mediaPlayer!!.duration

        seekBarHandler = Handler()
        updateRunnable()
        seekBarHandler.postDelayed(updateSeekBar, 0)


    }

    private fun updateRunnable(){
        updateSeekBar = Runnable {
            binding.seekbar.progress = mediaPlayer!!.currentPosition
            seekBarHandler.postDelayed(updateSeekBar, 0)
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

        when {
            binding.etNoteTitle.text.isNullOrEmpty() -> {
                Toast.makeText(context, "Note Title is Required.", Toast.LENGTH_SHORT).show()
            }
            binding.etNoteSubTitle.text.isNullOrEmpty() -> {

                Toast.makeText(context, "Note Sub Title is Required.", Toast.LENGTH_SHORT).show()
            }
            binding.etNoteDesc.text.isNullOrEmpty() -> {

                Toast.makeText(context, "Note Description is Required.", Toast.LENGTH_SHORT).show()
            }
            else -> {

                launch {
                    val notes = Notes()
                    notes.title = binding.etNoteTitle.text.toString()
                    notes.subTitle = binding.etNoteSubTitle.text.toString()
                    notes.noteText = binding.etNoteDesc.text.toString()
                    notes.dateTime = currentDate
                    notes.color = selectedColor
                    notes.imgPath = selectedImagePath
                    notes.audioPath = selectedAudioPath
                    notes.webLink = webLink
                    context?.let {
                        NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                        binding.etNoteTitle.setText("")
                        binding.etNoteSubTitle.setText("")
                        binding.etNoteDesc.setText("")
                        binding.layoutImage.visibility = View.GONE
                        binding.imgNote.visibility = View.GONE
                        binding.tvWebLink.visibility = View.GONE
                        binding.layoutPlayer.visibility = View.GONE
                        Toast.makeText(context, "Note Added.", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun updateNote(){
        launch {

            context?.let {
                val notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                notes.title = binding.etNoteTitle.text.toString()
                notes.subTitle = binding.etNoteSubTitle.text.toString()
                notes.noteText = binding.etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink
                notes.audioPath = selectedAudioPath

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                binding.etNoteTitle.setText("")
                binding.etNoteSubTitle.setText("")
                binding.etNoteDesc.setText("")
                binding.layoutImage.visibility = View.GONE
                binding.imgNote.visibility = View.GONE
                binding.tvWebLink.visibility = View.GONE
                binding.layoutPlayer.visibility = View.GONE
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
        if (Patterns.WEB_URL.matcher(binding.etWebLink.text.toString()).matches()){
            binding.layoutWebUrl.visibility = View.GONE
            binding.etWebLink.isEnabled = false
            webLink = binding.etWebLink.text.toString()
            binding.tvWebLink.visibility = View.VISIBLE
            binding.tvWebLink.text = binding.etWebLink.text.toString()
        }else{
            Toast.makeText(requireContext(), "Url is not valid.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recordAudio(){
        if(checkAudioPermission()){
            if(selectedAudioPath!=""){
                val file = File(selectedAudioPath)
                selectedAudioPath = ""
                if(file.exists()){
                    file.deleteOnExit()
                }
                binding.layoutPlayer.visibility = View.GONE
            }
            recordNow()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(recordPermission), requestCode)
        }
    }

    private fun recordNow(){
        binding.layoutRecordUrl.visibility = View.VISIBLE
        binding.recordBtn.setOnClickListener {
            if(isRecording){
                //Stop Recording
                    stopRecording()
                binding.recordBtn.setImageResource(R.drawable.record_btn_pause)
                Toast.makeText(requireContext(), "Recording Stopped and Saved.", Toast.LENGTH_SHORT).show()
                isRecording = false
                binding.layoutRecordUrl.visibility = View.GONE
                binding.layoutPlayer.visibility = View.VISIBLE
            }else{
                //Start Recording
                    startRecording()
                binding.recordBtn.setImageResource(R.drawable.record_btn)
                isRecording = true
            }
        }
    }

    private fun startRecording(){

        binding.recordTimer.base = SystemClock.elapsedRealtime()
        binding.recordTimer.start()

        //Get app external directory path
        val recordPath = requireActivity().getExternalFilesDir("/")!!.absolutePath

        //Get current date and time
        val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val now = Date()

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".3gp"

        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile("$recordPath/$recordFile")

        try {
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            Toast.makeText(requireContext(), "Recording Started.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopRecording(){

        binding.recordTimer.stop()
        binding.recordTimer.base = SystemClock.elapsedRealtime()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        val recordPath = requireActivity().getExternalFilesDir("/")!!.absolutePath
        selectedAudioPath = "$recordPath/$recordFile"

    }

    private fun checkAudioPermission(): Boolean{
        return ActivityCompat.checkSelfPermission(requireContext(), recordPermission) == PackageManager.PERMISSION_GRANTED
    }

    private val broadcastReceiver : BroadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            val actionColor = p1!!.getStringExtra("action")

            when(actionColor!!){

                "Orange" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }

                }

                "Red" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Blue" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Green" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Purple" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }


                "Yellow" -> {
                    if(selectedColor == p1.getStringExtra("selectedColor")){
                        binding.colorView.setBackgroundColor(Color.TRANSPARENT)
                        binding.layoutPlayer2.setBackgroundColor(Color.TRANSPARENT)
                        selectedColor = "null"
                    }else {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.layoutPlayer2.setBackgroundColor((Color.parseColor(selectedColor)))
                    }
                }

                "Image" -> {
                    readStorageTask()
                }

                "WebUrl" -> {
                    binding.layoutWebUrl.visibility = View.VISIBLE
                }

                "Audio" -> {
                    recordAudio()
                }

                "DeleteNote" -> {
                    deleteNote()
                }

                else -> {
                    binding.layoutImage.visibility = View.GONE
                    binding.imgNote.visibility = View.GONE
                    binding.layoutWebUrl.visibility = View.GONE
                }

            }
        }

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun hasReadStoragePerm():Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), storagePermission) == PackageManager.PERMISSION_GRANTED
    }


    private fun readStorageTask(){
        if (hasReadStoragePerm()){
            pickImageFromGallery()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(storagePermission), requestCode)
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null){
                val selectedImageUrl = data.data
                if (selectedImageUrl != null){
                    try {
                        Glide.with(this).load(getPathFromUri(selectedImageUrl)).override(1280, 720).into(binding.imgNote)
                        binding.imgNote.visibility = View.VISIBLE
                        binding.layoutImage.visibility = View.VISIBLE
                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    }catch (e: Exception){
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null){
            resultLauncher.launch(intent)
        }

    }

    private fun getPathFromUri(contentUri: Uri): String? {
        val filePath: String?
        val cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}