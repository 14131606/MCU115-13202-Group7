package com.example.foodorderingwizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.R

class SideDishesFragment : Fragment() {
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_side_dishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cbFries = view.findViewById<CheckBox>(R.id.cbFries)
        val cbSalad = view.findViewById<CheckBox>(R.id.cbSalad)
        val cbCorn = view.findViewById<CheckBox>(R.id.cbCorn)
        val btnDone = view.findViewById<Button>(R.id.btnDoneSideDishes)

        val listener = { cb: CheckBox ->
            viewModel.toggleSideDish(cb.text.toString(), cb.isChecked)
        }

        cbFries?.setOnCheckedChangeListener { _, _ -> listener(cbFries) }
        cbSalad?.setOnCheckedChangeListener { _, _ -> listener(cbSalad) }
        cbCorn?.setOnCheckedChangeListener { _, _ -> listener(cbCorn) }

        btnDone?.setOnClickListener {
            (activity as? MainActivity)?.navigateToNextPage()
        }
    }
}