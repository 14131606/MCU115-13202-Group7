package com.example.telephonebook

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val data: ArrayList<Contact>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var displayList = ArrayList<Contact>(data)
    private var currentQuery = ""

    init {
        filter("")
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvPhone: TextView = v.findViewById(R.id.tvPhone)
        val tvEmail: TextView = v.findViewById(R.id.tvEmail)
        val imgDelete: ImageView = v.findViewById(R.id.imgDelete)

        fun bind(item: Contact, clickListener: (Contact) -> Unit) {
            tvName.text = item.name
            tvPhone.text = item.phone
            tvEmail.text = item.email

            imgDelete.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.adapter_row, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = displayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayList[position]
        holder.bind(item) {
            data.remove(item)
            filter(currentQuery)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        currentQuery = query
        displayList.clear()
        if (query.isEmpty()) {
            displayList.addAll(data)
        } else {
            val lowerQuery = query.lowercase()
            for (item in data) {
                if (item.name.lowercase().contains(lowerQuery) ||
                    item.phone.lowercase().contains(lowerQuery) ||
                    item.email.lowercase().contains(lowerQuery)
                ) {
                    displayList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}