package com.icandothisallday2020.onstagram.navigation

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
import kotlinx.android.synthetic.main.item_detail.view.*

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
        var contentDTOs :ArrayList<ContentDTO>
        var contentUidList :ArrayList<String>


        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()
            var uid =FirebaseAuth.getInstance().currentUser?.uid
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
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
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewHolder.detail_item_iv_content)

            //Explain
            viewHolder.detail_item_explain.text =contentDTOs!![position].explain

            //likes count
            viewHolder.detail_item_favorite_count.text= "Likes "+contentDTOs!![position].favoriteCount

            //like button click event
            viewHolder.detail_item_favorite.setOnClickListener {
                favoriteEvent(position)
            }

            //when the page is loaded //likes button -> ♡ or ♥
            if(contentDTOs!![position].favorites.containsKey(uid)){
                //like ♥
                viewHolder.detail_item_favorite.setImageResource(R.drawable.ic_favorite)
            }else{
                //unlike ♡
                viewHolder.detail_item_favorite.setImageResource(R.drawable.ic_favorite_border)
            }

            //User ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewHolder.detail_item_profile_image)
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