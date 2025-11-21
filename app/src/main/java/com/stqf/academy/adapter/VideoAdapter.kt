package com.stqf.academy.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stqf.academy.model.VideoModel
import com.stqf.academy.R

class VideoAdapter(
    private val videoList: List<VideoModel>,
    private val onItemClick: (VideoModel) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.videoTitle)
        private val thumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)

        fun bind(video: VideoModel) {
            title.text = video.title

            // Load thumbnail from drawable resource
            Glide.with(itemView.context)
                .load(video.thumbnailResId)
                .into(thumbnail)

            itemView.setOnClickListener {
                onItemClick(video)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size
}