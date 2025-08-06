package com.darsuddeen.academy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.R
import com.darsuddeen.academy.databinding.FragmentDashBoardBinding

class DashBoardFragment : Fragment() {

    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)

        // Example cards
        binding.cardNamazTime.setOnClickListener {
            Toast.makeText(requireContext(), "নামাজের সময়সূচী", Toast.LENGTH_SHORT).show()
        }

        binding.cardHadith.setOnClickListener {
            Toast.makeText(requireContext(), "আজকের হাদিস", Toast.LENGTH_SHORT).show()
        }

        binding.cardQuranAyat.setOnClickListener {
            Toast.makeText(requireContext(), "আজকের আয়াত", Toast.LENGTH_SHORT).show()
        }

        binding.cardTasbih.setOnClickListener {
            Toast.makeText(requireContext(), "তসবিহ কাউন্টার", Toast.LENGTH_SHORT).show()
        }

        binding.cardMahasaba.setOnClickListener {
            Toast.makeText(requireContext(), "মহাসাবা", Toast.LENGTH_SHORT).show()
        }

        // ✅ Slide-in animation with LiveBooksFragment
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
