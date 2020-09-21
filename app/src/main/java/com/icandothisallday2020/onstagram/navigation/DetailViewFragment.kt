package com.icandothisallday2020.onstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.detail_item_profile_image

class DetailViewFragment : Fragment(){
    var firestore: FirebaseFirestore? =null
    var uid : String?=null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid


        view.detail_fragment_recyclerview.adapter= DetailRecyclerViewAdapter()
        view.detail_fragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }





    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs :ArrayList<ContentDTO> = ArrayList()
        var contentUidList :ArrayList<String> = ArrayList()


        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, this code return null of querySnapshot when it signout
                    if(querySnapshot == null) return@addSnapshotListener
                    for(snapshot in querySnapshot!!.documents){
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return  CustomViewHolder(view)
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(view!!){

        }



        override fun getItemCount(): Int {
            return  contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder).itemView

            //UserId
            viewHolder.detail_item_profile_name.text =contentDTOs!![position].userID

            //Image
            Glide.with(holder.itemView.context).load("https://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg").into(viewHolder.detail_item_iv_content)

            //Explain
            viewHolder.detail_item_explain.text =contentDTOs!![position].explain

            //likes count
            viewHolder.detail_item_favorite_count.text= "Likes "+contentDTOs!![position].favoriteCount

            //like button click event
            viewHolder.detail_item_favorite.setOnClickListener {
                favoriteEvent(position)
            }

            //User ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewHolder.detail_item_profile_image)


            //when the page is loaded //likes button -> ♡ or ♥
            if(contentDTOs!![position].favorites.containsKey(uid)){
                //like ♥
                viewHolder.detail_item_favorite.setImageResource(R.drawable.ic_favorite)
            }else{
                //unlike ♡
                viewHolder.detail_item_favorite.setImageResource(R.drawable.ic_favorite_border)
            }

            //When the profile image is clicked
            detail_item_profile_image.setOnClickListener {
                var fragment =UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userID",contentDTOs[position].userID)
                fragment.arguments= bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }

            viewHolder.detail_item_comment.setOnClickListener { v->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                startActivity(intent)
            }

        }

        fun  favoriteEvent(position : Int){
            var tsDoc =firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    //When the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount -1
                    contentDTO.favorites.remove(uid)
                }else{
                    //When the button is not clicked
                    contentDTO.favoriteCount= contentDTO?.favoriteCount +1
                    contentDTO?.favorites[uid!!]=true
                }
                transaction.set(tsDoc,contentDTO)
            }
        }

    }
}