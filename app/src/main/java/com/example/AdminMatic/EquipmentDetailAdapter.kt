package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class EquipmentDetailAdapter(list: MutableList<String>, private val cellClickListener: EquipmentDetailCellClickListener)

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
        holder.bind(detail, position)
        println("queryText = $queryText")
        //text highlighting for first string

        //if (filterList[position].name != null){
        holder.itemView.findViewById<TextView>(R.id.list_detail_name).text = filterList[position]
        //}





        //val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onEquipmentDetailCellClickListener(position)
        }







    }

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class EquipmentDetailViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.equipment_detail_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_detail_name)

    }

    fun bind(detail: String, position: Int) {
        mNameView?.text = detail
        if (position == 9) { // vendor link
            mNameView!!.setTextColor(mNameView!!.context.getColor(R.color.link))
        }
    }
}