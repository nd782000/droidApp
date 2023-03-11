package com.example.AdminMatic

import android.content.Context
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class EquipmentCrewAdapter(list: MutableList<EquipmentCrew>, private val context: Context, private val appContext: Context, cellClickListener: EquipmentCellClickListener)

    : RecyclerView.Adapter<EquipmentCrewViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null
    private val cCL = cellClickListener

    var filterList:MutableList<EquipmentCrew> = emptyList<EquipmentCrew>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentCrewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EquipmentCrewViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: EquipmentCrewViewHolder, position: Int) {

        val equipmentCrew: EquipmentCrew = filterList[position]
        holder.bind(equipmentCrew)
        println("queryText = $queryText")

        val adapter = EquipmentAdapter(equipmentCrew.equips, appContext, context, cCL, true)

        //holder.mRecycler!!.setHasFixedSize(true)
        holder.mRecycler!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        holder.mRecycler!!.addItemDecoration(itemDecoration)

    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class EquipmentCrewViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.department_header, parent, false)) {
    private var mNameView: TextView? = null
    private var mColorView: View? = null
    var mRecycler: RecyclerView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mColorView = itemView.findViewById(R.id.section_header_color_view)
        mRecycler = itemView.findViewById(R.id.department_recycler_view)
    }


    fun bind(equipmentCrew: EquipmentCrew) {

        if (equipmentCrew.name != null) {
            mNameView!!.text = equipmentCrew.name
        }
        if (equipmentCrew.color != null) {
            mColorView!!.background = ColorDrawable(parseColor(equipmentCrew.color))
        }
    }
}