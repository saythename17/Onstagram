package com.icandothisallday2020.onstagram.navigation

import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.icandothisallday2020.onstagram.R
import com.icandothisallday2020.onstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_grid.*

class GridFragment : Fragment(){
    var firesotre : FirebaseFirestore? = null
    var fragmentView : View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        firesotre = FirebaseFirestore.getInstance()
        grid_recycler.adapter = GridRecyclerAdapter()
        grid_recycler.layoutManager = GridLayoutManager(activity,3)
        return fragmentView
    }
    inner class GridRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firesotre?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var iv = ImageView(parent.context)
            iv.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return VH(iv)
        }

        inner class VH(iv: ImageView) : RecyclerView.ViewHolder(iv)

        override fun getItemCount(): Int { return contentDTOs.size }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var iv = (holder as VH).itemView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop()).into(iv as ImageView)
        }

    }
}