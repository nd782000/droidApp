package com.example.AdminMatic

import android.annotation.SuppressLint
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


class CrewDaysAdapter(list: MutableList<CrewDaySection>, crewCellClickListener: CrewCellClickListener, crewEntryCellClickListener: CrewEntryCellClickListener)

    : RecyclerView.Adapter<CrewDayViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null
    private val crewCCL = crewCellClickListener
    private val crewEntryCCL = crewEntryCellClickListener

    var filterList:MutableList<CrewDaySection> = emptyList<CrewDaySection>().toMutableList()


    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewDayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CrewDayViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: CrewDayViewHolder, position: Int) {

        val crewDaySection: CrewDaySection = filterList[position]
        holder.bind(crewDaySection)
        //println("queryText = $queryText")

        val adapter = CrewsAdapter(crewDaySection.entries, null, crewCCL, crewEntryCCL, true)

        //holder.mRecycler!!.setHasFixedSize(true)
        holder.mRecycler!!.layoutManager = LinearLayoutManager(myView.context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        holder.mRecycler!!.addItemDecoration(itemDecoration)



    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class CrewDayViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.crews_week_header, parent, false)) {
    private var mDateView: TextView? = null
    var mRecycler: RecyclerView? = null


    init {
        mDateView = itemView.findViewById(R.id.date_tv)
        mRecycler = itemView.findViewById(R.id.recycler_view)
    }

    @SuppressLint("Range")
    fun bind(crewDaySection: CrewDaySection) {
        mDateView!!.text = crewDaySection.dateString
    }
}