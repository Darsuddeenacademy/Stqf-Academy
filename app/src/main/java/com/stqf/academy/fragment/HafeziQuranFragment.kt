package com.stqf.academy.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.stqf.academy.adapter.ColorQuranAdapter
import com.stqf.academy.databinding.FragmentHafeziQuranBinding
import com.stqf.academy.model.BookApiModel
import com.stqf.academy.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class HafeziQuranFragment : Fragment() {

    private var _binding: FragmentHafeziQuranBinding? = null
    private val binding get() = _binding!!

    private lateinit var hafeziAdapter: ColorQuranAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHafeziQuranBinding.inflate(inflater, container, false)

        setupRecyclerView()

        showLoading(true)

        if (isNetworkAvailable(requireContext())) {
            loadFromApi()
        } else {
            loadDownloadedHafezi()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.hafeziRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.hafeziProgress.visibility = View.VISIBLE
            binding.hafeziLoadingText.visibility = View.VISIBLE
            binding.hafeziRecyclerView.visibility = View.GONE
        } else {
            binding.hafeziProgress.visibility = View.GONE
            binding.hafeziLoadingText.visibility = View.GONE
            binding.hafeziRecyclerView.visibility = View.VISIBLE
        }
    }

    // 🔹 API থেকে Hafezi Quran
    private fun loadFromApi() {
        val call = ApiClient.hafeziApi.getHafeziQuran()

        call.enqueue(object : Callback<List<BookApiModel>> {
            override fun onResponse(
                call: Call<List<BookApiModel>>,
                response: Response<List<BookApiModel>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    hafeziAdapter = ColorQuranAdapter(requireContext(), list)
                    binding.hafeziRecyclerView.adapter = hafeziAdapter
                    showLoading(false)
                } else {
                    binding.hafeziProgress.visibility = View.GONE
                    binding.hafeziLoadingText.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "হাফেজী কুরআন পাওয়া যায়নি",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<BookApiModel>>, t: Throwable) {
                binding.hafeziProgress.visibility = View.GONE
                binding.hafeziLoadingText.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "ত্রুটি: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // 🔹 অফলাইনে ডাউনলোড করা Hafezi Quran
    private fun loadDownloadedHafezi() {
        val downloadDir = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "hafezi_quran"
        )

        val offlineList = mutableListOf<BookApiModel>()

        if (downloadDir.exists()) {
            downloadDir.listFiles()?.forEach { file ->
                if (file.extension == "pdf") {
                    val title = file.nameWithoutExtension.replace("_", " ")

                    val thumbFile = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "hafezi_thumbnails/${file.nameWithoutExtension}.jpg"
                    )

                    val model = BookApiModel(
                        id = 0,
                        title = title,
                        description = "অফলাইনে সংরক্ষিত হাফেজী কুরআন",
                        pdf_url = "",
                        thumbnail_url = if (thumbFile.exists())
                            thumbFile.absolutePath
                        else
                            ""
                    )
                    offlineList.add(model)
                }
            }
        }

        if (offlineList.isNotEmpty()) {
            hafeziAdapter = ColorQuranAdapter(requireContext(), offlineList)
            binding.hafeziRecyclerView.adapter = hafeziAdapter
            showLoading(false)
            Toast.makeText(
                requireContext(),
                "ইন্টারনেট নেই, অফলাইন হাফেজী কুরআন দেখানো হচ্ছে",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            binding.hafeziProgress.visibility = View.GONE
            binding.hafeziLoadingText.visibility = View.GONE
            Toast.makeText(
                requireContext(),
                "কোনো হাফেজী কুরআন ডাউনলোড করা নেই",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetworkInfo
        return active != null && active.isConnected
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
