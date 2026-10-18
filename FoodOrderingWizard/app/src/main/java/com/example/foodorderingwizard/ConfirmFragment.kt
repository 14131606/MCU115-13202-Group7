package com.example.foodorderingwizard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodorderingwizard.R

class ConfirmFragment : Fragment() {
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvConfirmSummary = view.findViewById<TextView>(R.id.tvConfirmSummary)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        viewModel.order.observe(viewLifecycleOwner) { order ->
            if (order.mainMeal != null || order.sideDishes.isNotEmpty() || order.drink != null) {
                val summary = StringBuilder()
                if (!order.mainMeal.isNullOrEmpty()) summary.append("Main: ${order.mainMeal}\n")
                if (order.sideDishes.isNotEmpty()) summary.append("Sides: ${order.sideDishes.joinToString(", ")}\n")
                if (!order.drink.isNullOrEmpty()) summary.append("Drink: ${order.drink}")

                tvConfirmSummary?.text = summary.toString()
            }
        }

        btnConfirm?.setOnClickListener {
            val order = viewModel.order.value

            if (order?.isComplete() != true) {
                Toast.makeText(
                    requireContext(),
                    "Please select a main meal, at least one side dish, and a drink.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Submit Order")
                .setMessage(
                    "Main: ${order.mainMeal}\n" +
                            "Sides: ${order.sideDishes.joinToString(", ")}\n" +
                            "Drink: ${order.drink}\n\nSubmit this order?"
                )
                .setPositiveButton("Submit") { dialog, _ ->
                    viewModel.submitOrder()
                    Toast.makeText(requireContext(), "Order submitted!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    (activity as? MainActivity)?.navigateToFirstPage()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}