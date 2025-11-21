package com.stqf.academy.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.stqf.academy.adapter.VideoAdapter
import com.stqf.academy.model.VideoModel
import com.stqf.academy.R
import com.stqf.academy.activity.OpenVideoListActivity
import com.stqf.academy.databinding.FragmentVideosBinding

class VideoClassFragment : Fragment() {

    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoAdapter

    private val videoList = listOf(
        VideoModel(
            "এসো কুরআন শিখি ( Stqf Academy )",
            "https://www.youtube.com/watch?v=YYuWKGwrVpo&list=PL9oidZxOncZqRmIgl2u6d_rwt9LIIwcmm",
            R.drawable.playlist_tb11
        ),
        VideoModel(
            "বানান করে কুরআন শিক্ষা ( Stqf Academy )",
            "https://www.youtube.com/watch?v=XsuDm_e0Aws&list=PL9oidZxOncZrlXXDWww_r2ALyP_3WMayf",
            R.drawable.playlist_tb334
        ),
        VideoModel(
            "শব্দার্থে বাংলা তরজমাসহ কুরআন শিক্ষার ক্লাশ",

            "https://www.youtube.com/watch?v=iITzXn6MJAA&list=PL9oidZxOncZosO4JzfOmBxrfSdqe5FGxB",
            R.drawable.playlist_tb11

        ),

                VideoModel(
                "বাংলা অর্থসহ আম্মাপারা ক্লাশ",

        "https://www.youtube.com/watch?v=FMfAhm6XZyU&list=PL9oidZxOncZp0d4WzwEcMxn2bAQpp1EqU&pp=gAQB",
        R.drawable.playlist_tb334
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