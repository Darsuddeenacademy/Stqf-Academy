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
import com.stqf.academy.databinding.FragmentColorQuranBinding
import com.stqf.academy.model.BookApiModel
import com.stqf.academy.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ColorQuranFragment : Fragment() {

    private var _binding: FragmentColorQuranBinding? = null
    private val binding get() = _binding!!

    private lateinit var quranAdapter: ColorQuranAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorQuranBinding.inflate(inflater, container, false)

        setupRecyclerView()

        // শুরুতে Loader দেখাই
        showLoading(true)

        if (isNetworkAvailable(requireContext())) {
            loadQuranFromApi()
        } else {
            loadDownloadedQuran()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.colorQuranRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2)
    }

    // Loader on/off helper
    private fun showLoading(show: Boolean) {
        if (show) {
            binding.colorQuranProgress.visibility = View.VISIBLE
            binding.colorQuranLoadingText.visibility = View.VISIBLE
            binding.colorQuranRecyclerView.visibility = View.GONE
        } else {
            binding.colorQuranProgress.visibility = View.GONE
            binding.colorQuranLoadingText.visibility = View.GONE
            binding.colorQuranRecyclerView.visibility = View.VISIBLE
        }
    }

    // 🔹 অনলাইনে API থেকে কালার কুরআন লোড
    private fun loadQuranFromApi() {
        val call = ApiClient.quranApi.getQuran()

        call.enqueue(object : Callback<List<BookApiModel>> {
            override fun onResponse(
                call: Call<List<BookApiModel>>,
                response: Response<List<BookApiModel>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val quranList = response.body()!!
                    quranAdapter = ColorQuranAdapter(requireContext(), quranList)
                    binding.colorQuranRecyclerView.adapter = quranAdapter
                    showLoading(false)
                } else {
                    binding.colorQuranProgress.visibility = View.GONE
                    binding.colorQuranLoadingText.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "কালার কুরআন পাওয়া যায়নি",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<BookApiModel>>, t: Throwable) {
                binding.colorQuranProgress.visibility = View.GONE
                binding.colorQuranLoadingText.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "ত্রুটি: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // 🔹 ইন্টারনেট না থাকলে লোকাল ডাউনলোডেড কুরআন দেখাবে
    private fun loadDownloadedQuran() {
        val downloadDir = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "color_quran"
        )

        val offlineList = mutableListOf<BookApiModel>()

        if (downloadDir.exists()) {
            downloadDir.listFiles()?.forEach { file ->
                if (file.extension == "pdf") {
                    val title = file.nameWithoutExtension.replace("_", " ")

                    val thumbFile = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "quran_thumbnails/${file.nameWithoutExtension}.jpg"
                    )

                    val model = BookApiModel(
                        id = 0,
                        title = title,
                        description = "অফলাইনে সংরক্ষিত কালার কুরআন",
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
            quranAdapter = ColorQuranAdapter(requireContext(), offlineList)
            binding.colorQuranRecyclerView.adapter = quranAdapter
            showLoading(false)
            Toast.makeText(
                requireContext(),
                "ইন্টারনেট নেই, অফলাইন কালার কুরআন দেখানো হচ্ছে",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            binding.colorQuranProgress.visibility = View.GONE
            binding.colorQuranLoadingText.visibility = View.GONE
            Toast.makeText(
                requireContext(),
                "কোনো কালার কুরআন ডাউনলোড করা নেই",
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
