package com.example.threadhandlerandprogressbarwithbackgroundmusic

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ProgressFragment : Fragment(R.layout.frag_progress) {

    private val vm: WorkViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val txtProgress = view.findViewById<TextView>(R.id.txtProgress)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)

        progressBar.isIndeterminate = false

        vm.status.observe(viewLifecycleOwner) { status ->
            txtStatus.text = status
            if (status == "Idle" || status == "準備中...") {
                progressBar.progress = 0
                txtProgress.text = "0%"
            } else if (status == "背景工作結束！") {
                progressBar.progress = 100
                txtProgress.text = "100%"
            }
        }

        vm.progress.observe(viewLifecycleOwner) { p ->
            if (vm.status.value == "工作中...") {
                progressBar.progress = p
                txtProgress.text = "$p%"
            }
        }
    }
}