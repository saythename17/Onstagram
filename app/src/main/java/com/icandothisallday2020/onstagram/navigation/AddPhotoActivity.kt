package com.icandothisallday2020.onstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import bolts.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM=0
    var storage :FirebaseStorage? =null
    var photoUri : Uri? = null
    var auth :FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initial
        storage = FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        firestore= FirebaseFirestore.getInstance()

        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        add_photo_btn_upload.setOnClickListener { contentUpload() }
    }//onCreate........................

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM ){
            if(resultCode == Activity.RESULT_OK){
                //This is path to the selected image
                photoUri = data?.data
                add_photo_image.setImageURI(photoUri)
            }else{
                //Exit the addPhotoActivity if user leave the album without selection it
                finish()
            }
        }
    }//onActivityResult.................]




    fun contentUpload() {
        //Make filename
        //date->name :: Prevention of duplicate creation
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" +timestamp+ ".png"

        var storageReference = storage?.reference?.child("images")?.child(imageFileName)
        // The two way of Upload : callback method, promise method

        //Promise method
        storageReference?.putFile(photoUri!!)?.continueWith {
                task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
            return@continueWith storageReference.downloadUrl
        }?.addOnSuccessListener { uri -> var contentDTO = ContentDTO()

            //Insert download of image
            contentDTO.imageUrl = uri.toString()

            //Insert UID of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userID = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = add_photo_et_explain.text.toString()

            //Insert timestamp
            contentDTO.timeStamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }







        /*FileUpload Callback method
        storageReference?.putFile(photoUri!!)?.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener {
                uri -> var contentDTO = ContentDTO()

                //Insert download of image
                contentDTO.imageUrl = uri.toString()

                //Insert UID of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userID = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = add_photo_et_explain.text.toString()

                //Insert timestamp
                contentDTO.timeStamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }*/
    }
}


