package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.time.LocalDate


class MyScheduleSectionAdapter(list: MutableList<MyScheduleSection>, private val context: Context, private val myScheduleCellClickListener: MyScheduleCellClickListener) : RecyclerView.Adapter<MyScheduleSectionViewHolder>() {

    var showCompleted = false

    var filterList:MutableList<MyScheduleSection> = emptyList<MyScheduleSection>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyScheduleSectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyScheduleSectionViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: MyScheduleSectionViewHolder, position: Int) {

        val myScheduleSection: MyScheduleSection = filterList[position]
        holder.bind(myScheduleSection)


        var entriesList = myScheduleSection.entriesFiltered
        if (showCompleted) {
            entriesList = myScheduleSection.entries
        }

        val adapter = MyScheduleRowAdapter(entriesList, myScheduleSection, myScheduleCellClickListener)

        holder.mRecycler!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter

        if (!holder.addedDecoration) {
            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            holder.mRecycler!!.addItemDecoration(itemDecoration)
            holder.addedDecoration = true
        }





    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class MyScheduleSectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.my_schedule_header, parent, false)) {
    private var mDateText: TextView? = null
    private var mPlannedDateCl: ConstraintLayout? = null
    private var mPlannedDateText: TextView? = null

    var mRecycler: RecyclerView? = null

    var addedDecoration = false

    init {
        mDateText = itemView.findViewById(R.id.date_tv)
        mPlannedDateCl = itemView.findViewById(R.id.note_cl)
        mPlannedDateText = itemView.findViewById(R.id.note_tv)
        mRecycler = itemView.findViewById(R.id.recycler_view)
    }

    fun bind(myScheduleSection: MyScheduleSection) {
        mDateText!!.text = myScheduleSection.date
        if (myScheduleSection.dayNote.isNotBlank()) {
            mPlannedDateCl!!.visibility = View.VISIBLE
            mPlannedDateText!!.text = myScheduleSection.dayNote
        }
        else {
            mPlannedDateCl!!.visibility = View.GONE
        }
    }
}