package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.customer_list_item.view.*
import kotlinx.android.synthetic.main.service_list_item.view.*
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import java.util.*



class ServiceInspectionAdapter(private val list: MutableList<InspectionQuestion>, private val context: Context,private val cellClickListener: ServiceInspectionCellClickListener)

    : RecyclerView.Adapter<ServiceInspectionViewHolder>() {


    var filterList:MutableList<InspectionQuestion> = emptyList<InspectionQuestion>().toMutableList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceInspectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ServiceInspectionViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: ServiceInspectionViewHolder, position: Int) {




        val question: InspectionQuestion = filterList[position]
        holder.bind(question)

        holder.itemView.list_service_name.text = question.name
        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onServiceInspectionCellClickListener(data)
        }
    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class ServiceInspectionViewHolder(inflater: LayoutInflater, parent: ViewGroup, ) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_inspection_list_item, parent, false)) {
    private var mNameView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.service_inspection_item_name_txt)
    }

    fun bind(question: InspectionQuestion) {
        mNameView?.text = question.name
    }
}