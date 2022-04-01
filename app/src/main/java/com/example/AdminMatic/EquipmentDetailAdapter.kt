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
import kotlinx.android.synthetic.main.customer_list_item.view.*
import kotlinx.android.synthetic.main.equipment_detail_list_item.view.*
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import java.util.*



class EquipmentDetailAdapter(private val list: MutableList<String>, private val context: Context,private val cellClickListener: EquipmentDetailCellClickListener)

    : RecyclerView.Adapter<EquipmentDetailViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<String> = emptyList<String>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EquipmentDetailViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: EquipmentDetailViewHolder, position: Int) {




        val detail: String = filterList[position]
        holder.bind(detail)
        println("queryText = $queryText")
        //text highlighting for first string

        //if (filterList[position].name != null){
        holder.itemView.list_detail_name.text = filterList[position]
        //}





        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onEquipmentDetailCellClickListener(data)
        }







    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class EquipmentDetailViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.equipment_detail_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_detail_name)

    }

    fun bind(detail: String) {
        mNameView?.text = detail
    }
}