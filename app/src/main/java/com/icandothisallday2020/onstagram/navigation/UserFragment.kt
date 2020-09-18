package com.icandothisallday2020.onstagram.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user.*
import  kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.fragment_user.view.account_recycler

class UserFragment : Fragment(){
    var fragmentView : View? = null
    var firestore :FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth?= null
    var contentDTO : ArrayList<ContentDTO> ?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fragmentView?.account_recycler?.adapter = UserFragmentAdapter()
        fragmentView?.account_recycler?.layoutManager = GridLayoutManager(activity!!,3)
        contentDTO = arrayListOf()

        firestore?.collection("images")?.whereEqualTo("uid",uid)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                Log.i("aaa","ssss :"+querySnapshot.documents)
                //Get data
                for (snapshot in querySnapshot.documents){
                    Log.i("aaa","ddd")
                    contentDTO!!.add(0,snapshot.toObject(ContentDTO::class.java)!!)
                    account_recycler.adapter?.notifyItemInserted(0)//contentDTO.size-1
                    Log.i("aaa","eee")
                }
                fragmentView?.account_post_count?.text = contentDTO!!.size.toString()
            }

        return fragmentView
    }

    inner  class  UserFragmentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        init {
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            Log.i("aaa","ggg")
            var size = resources.displayMetrics.widthPixels / 3 //screen's 1/3
            var iv= ImageView(parent.context)
            iv.layoutParams = LinearLayoutCompat.LayoutParams(size,size)
            return CustomVH(iv)
        }

        inner class CustomVH(var iv: ImageView) : RecyclerView.ViewHolder(iv)

        override fun getItemCount(): Int {return  contentDTO!!.size}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var iv = (holder as CustomVH).iv
            iv.setPadding(4,8,4,8)
            Glide.with(holder.itemView.context).load("https://firebasestorage.googleapis.com/v0/b/onstagram-63eef.appspot.com/o/images%2FIMAGE_20200918_051231.png?alt=media&token=82b76ec6-1b16-41f1-99e8-6b4b37806700").apply(RequestOptions().centerCrop()).into(iv)
           // Picasso.get().load(""+contentDTO!![position].imageUrl).into(iv)
            Log.i("aaa","imgUrl : "+contentDTO!![position].imageUrl)
        }

    }
}