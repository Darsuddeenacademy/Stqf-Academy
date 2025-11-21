package com.stqf.academy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stqf.academy.fragment.LiveBooksFragment
import com.stqf.academy.R
import com.stqf.academy.databinding.FragmentDashBoardBinding

class DashBoardFragment : Fragment() {

    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)

        // 🕌 নামাজের সময় কার্ড
        binding.cardNamazTime.setOnClickListener {
            Toast.makeText(requireContext(), "নামাজের সময়সূচী", Toast.LENGTH_SHORT).show()
        }

        /* 📘 কালার কুরআন কার্ড (XML: android:id="@+id/hafizi_quran") */
        binding.cardHafeziQuran.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, HafeziQuranFragment())
                .addToBackStack(null)
                .commit()
        }


        // 📘 কালার কুরআন কার্ড (XML: android:id="@+id/color_quran")
        binding.colorQuran.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, ColorQuranFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🕋 আজকের হাদিস
        binding.cardHadith.setOnClickListener {
            Toast.makeText(requireContext(), "আজকের হাদিস", Toast.LENGTH_SHORT).show()
        }

        // 📖 আজকের আয়াত
        binding.cardQuranAyat.setOnClickListener {
            Toast.makeText(requireContext(), "আজকের আয়াত", Toast.LENGTH_SHORT).show()
        }

        // 🔢 তসবিহ কাউন্টার
        binding.cardTasbih.setOnClickListener {
            Toast.makeText(requireContext(), "তসবিহ কাউন্টার", Toast.LENGTH_SHORT).show()
        }

        // 👥 মহাসাবা
        binding.cardMahasaba.setOnClickListener {
            Toast.makeText(requireContext(), "মহাসাবা", Toast.LENGTH_SHORT).show()
        }

        // 📚 লাইভ অনলাইন বই
        binding.cardOnlineBooks.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, LiveBooksFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
