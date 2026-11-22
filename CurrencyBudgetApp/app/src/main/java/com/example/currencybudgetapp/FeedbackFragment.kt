package com.example.currencybudgetapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.currencybudgetapp.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {
    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    // TODO: 改成開發者(組員)真正的 Email
    private val developerEmail = "14130842@me.mcu.edu.tw"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            val text = binding.etFeedback.text.toString().trim()
            if (text.isEmpty()) {
                binding.etFeedback.error = "請輸入回饋內容"
                return@setOnClickListener
            }

            val user = AuthManager(requireContext()).currentUser()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmail))
                putExtra(Intent.EXTRA_SUBJECT, "[匯率記帳App] 使用者回饋")
                putExtra(Intent.EXTRA_TEXT, "帳號:$user\n\n$text")
            }

            try {
                startActivity(intent)
                UsageTracker(requireContext()).logEvent("送出回饋")
                binding.etFeedback.text?.clear()
                // 動畫:感謝訊息淡入
                binding.tvThanks.alpha = 0f
                binding.tvThanks.animate().alpha(1f).setDuration(600).start()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "找不到可用的 Email App", Toast.LENGTH_SHORT).show()
            }
        }

        // 返回鍵:一律回到設定頁
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val nav = androidx.navigation.fragment.NavHostFragment.findNavController(
                        this@FeedbackFragment
                    )
                    // 設定頁在堆疊裡就直接退回去,不在的話就重新導向設定頁
                    if (!nav.popBackStack(R.id.settingsFragment, false)) {
                        nav.navigate(
                            R.id.settingsFragment, null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.feedbackFragment, true)
                                .build()
                        )
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}