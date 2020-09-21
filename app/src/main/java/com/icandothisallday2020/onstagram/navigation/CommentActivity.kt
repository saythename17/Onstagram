package com.icandothisallday2020.onstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    var contentUid : String ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        comment_send_btn?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userID = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_et_message.text.toString()
            comment.timeStamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)
            comment_et_message.setText("")
        }
    }
}
