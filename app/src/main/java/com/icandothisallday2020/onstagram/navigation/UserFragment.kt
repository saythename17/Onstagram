package com.icandothisallday2020.onstagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.ContactsContract.ProfileSyncState.set
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icandothisallday2020.onstagram.LoginActivity
import com.icandothisallday2020.onstagram.MainActivity
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import com.icandothisallday2020.onstagram.navigation.model.FollowDTO
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import  kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.fragment_user.view.account_recycler
import java.lang.reflect.Array.set

class UserFragment : Fragment(){
    var fragmentView : View? = null
    var firestore :FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth?= null
    var contentDTO : ArrayList<ContentDTO> ?=null
    var currentUserUid :String?= null
    companion object{//static
        var PICK_PROFILE_FROM_ALBUM= 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        contentDTO = arrayListOf()

        currentUserUid= auth?.currentUser?.uid
        if(uid == currentUserUid){
            //My Page
            account_btn_follow?.text= getString(R.string.signout)
            account_btn_follow?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            //OtherUser Page
            fragmentView?.account_btn_follow?.text= getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.username?.text = arguments?.getString("userID")
            mainActivity?.back?.setOnClickListener {
                mainActivity.bottom_navigation.selectedItemId=R.id.action_home
            }
            app_title.visibility=View.GONE
            username.visibility=View.VISIBLE
            back.visibility=View.VISIBLE
            account_btn_follow?.setOnClickListener {
                requestFollow()
            }

        }

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
        account_recycler?.adapter = UserFragmentAdapter()
        account_recycler?.layoutManager = GridLayoutManager(activity!!,3)

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent= Intent(Intent.ACTION_PICK)
            photoPickerIntent.type= "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowing()
        return fragmentView
    }

    fun getFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot ==null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followerCounter != null){
                account_following_count?.text =followDTO.followingCount?.toString()
            }
            if(followDTO?.followingCount != null){
                account_follower_count?.text =followDTO.followerCounter?.toString()
                if(followDTO?.follwers?.containsKey(currentUserUid!!)){
                    account_btn_follow?.text = getString(R.string.follow_cancel)
                    account_btn_follow?.background?.setColorFilter(ContextCompat.getColor(
                        activity!!,android.R.color.holo_blue_light),PorterDuff.Mode.MULTIPLY)
                }else{
                    if (uid != currentUserUid){
                        account_btn_follow?.text = getString(R.string.follow)
                        fragmentView?.account_btn_follow?.background?.colorFilter = null
                    }
                }
            }
        }
    }

    fun requestFollow(){
        //my following
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.follwers[uid!!] = true//to avoid duplicate following
                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }
            if(followDTO.followings.containsKey(uid)){
                //remove following-  when the user follow me
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.follwers?.remove(uid)
            }else{
                //add following- when the user do not follow me
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.follwers[uid!!] = true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        //save the data to access my following user's account
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!. followingCount = 1
                followDTO!!.follwers[currentUserUid!!] = true

                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.follwers.containsKey(currentUserUid)){
                //cancel my following-- when I follow the user
                followDTO!!.followerCounter = followDTO!!.followerCounter - 1
                followDTO!!.follwers.remove(currentUserUid!!)
            }else{
                //add my follower-- when I don't follow the user
                followDTO!!.followerCounter = followDTO!!.followerCounter + 1
                followDTO!!.follwers[currentUserUid!!] = true
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
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