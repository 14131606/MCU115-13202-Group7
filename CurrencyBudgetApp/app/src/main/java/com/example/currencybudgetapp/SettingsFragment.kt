package com.example.currencybudgetapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.currencybudgetapp.databinding.FragmentSettingsBinding
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val auth = AuthManager(requireContext())

        binding.tvProfile.text = "目前帳號:${auth.currentUser()}"

        binding.rowUsage.setOnClickListener {
            UsageTracker(requireContext()).logEvent("開啟使用紀錄")
            startActivity(Intent(requireContext(), UsageStatsActivity::class.java))
        }

        binding.rowFeedback.setOnClickListener {
            findNavController().navigate(R.id.feedbackFragment)
        }

        binding.btnLogout.setOnClickListener {
            UsageTracker(requireContext()).logEvent("登出")
            auth.logout()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}