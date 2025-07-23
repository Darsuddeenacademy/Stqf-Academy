package com.darsuddeen.academy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoClassFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter

    private val videoList = listOf(
        VideoModel(
            "কায়দা থেকে কুরআন মাজীদ শিক্ষা",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdcpwNcQlcOqeMDo-C9ItSlx&si=nJJNg1anh4fesO8J"
        ),

        VideoModel(
            "নামাজের অর্থ ও মর্মার্থ বুঝে নামাজ শিক্ষা",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdctUJK5H4VI694gtM5md_OC&si=dA1O06SiAxgcKL_e"
        ),
        VideoModel(
            "শুদ্ধভাবে সূরা শিখি ( Darsud Deen Academy )",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdfYT_UKpdhjOKwSzLceNaSy&si=cuT88yQXSezGyl29"
        )

        //
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.videoRecyclerView)
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = VideoAdapter(videoList) { video ->
            val intent = Intent(requireContext(), OpenVideoListActivity::class.java)
            intent.putExtra("playlist_url", video.playlistUrl)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}
