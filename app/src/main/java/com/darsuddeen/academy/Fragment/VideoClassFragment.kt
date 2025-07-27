package com.darsuddeen.academy.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.darsuddeen.academy.Adapter.VideoAdapter
import com.darsuddeen.academy.Model.VideoModel
import com.darsuddeen.academy.R
import com.darsuddeen.academy.Activity.OpenVideoListActivity
import com.darsuddeen.academy.databinding.FragmentVideosBinding

class VideoClassFragment : Fragment() {

    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoAdapter

    private val videoList = listOf(
        VideoModel(
            "কায়দা থেকে কুরআন মাজীদ শিক্ষা",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdcpwNcQlcOqeMDo-C9ItSlx",
            R.drawable.playlist_tb11
        ),
        VideoModel(
            "শুদ্ধভাবে সূরা শিখি ( Darsud Deen Academy )",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdctUJK5H4VI694gtM5md_OC",
            R.drawable.playlist_tb33
        ),
        VideoModel(
            "নামাজের অর্থ ও মর্মার্থ বুঝে নামাজ শিক্ষা",
            "https://youtube.com/playlist?list=PLdZH8A1MbXdfYT_UKpdhjOKwSzLceNaSy",
            R.drawable.playlist_tb22


        )

    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.videoRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = VideoAdapter(videoList) { video ->
            val intent = Intent(requireContext(), OpenVideoListActivity::class.java)
            intent.putExtra("playlist_url", video.playlistUrl)
            startActivity(intent)
        }

        binding.videoRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}