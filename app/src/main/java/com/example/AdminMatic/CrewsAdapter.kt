package com.example.AdminMatic

import android.annotation.SuppressLint
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


class CrewsAdapter(list: MutableList<CrewSection>, private val crewEntryDelegate: CrewEntryDelegate, crewCellClickListener: CrewCellClickListener, crewEntryCellClickListener: CrewEntryCellClickListener)

    : RecyclerView.Adapter<CrewViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null
    private val crewCCL = crewCellClickListener
    private val crewEntryCCL = crewEntryCellClickListener

    var filterList:MutableList<CrewSection> = emptyList<CrewSection>().toMutableList()

    var queryText = ""



    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CrewViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {

        val crewSection: CrewSection = filterList[position]
        holder.bind(crewSection)
        //println("queryText = $queryText")

        val adapter = CrewEntriesAdapter(crewSection.entries, position, crewSection.ID, crewEntryDelegate, crewEntryCCL)

        //holder.mRecycler!!.setHasFixedSize(true)
        holder.mRecycler!!.layoutManager = LinearLayoutManager(myView.context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        holder.mRecycler!!.addItemDecoration(itemDecoration)

        holder.itemView.setOnClickListener {
            crewCCL.onCrewCellClickListener(crewSection)
        }

    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class CrewViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.crew_header, parent, false)) {
    private var mNameView: TextView? = null
    private var mColorView: View? = null
    var mRecycler: RecyclerView? = null


    init {
        mNameView = itemView.findViewById(R.id.name_tv)
        mColorView = itemView.findViewById(R.id.header_cl)
        mRecycler = itemView.findViewById(R.id.rv)
    }

    @SuppressLint("Range")
    fun bind(crewSection: CrewSection) {

        mNameView!!.text = crewSection.name
        mColorView!!.background = ColorDrawable(parseColor(crewSection.color))

    }
}