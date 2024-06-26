package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.LocalTime

class MyScheduleRowAdapter(list: MutableList<MyScheduleEntry>, private val myScheduleSection: MyScheduleSection, private val myScheduleCellClickListener: MyScheduleCellClickListener) : RecyclerView.Adapter<MyScheduleRowViewHolder>() {

    var entryList:MutableList<MyScheduleEntry> = emptyList<MyScheduleEntry>().toMutableList()
    var entryListFiltered = mutableListOf<MyScheduleEntry>()

    var showCompleted = false

    init {
        entryList = list

        for (entry in entryList) {
            if (!entry.checkIfCompleted()) {
                entryListFiltered.add(entry)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyScheduleRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyScheduleRowViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: MyScheduleRowViewHolder, position: Int) {

        //val myScheduleEntry: MyScheduleEntry = entryList[position]

        val myScheduleEntry: MyScheduleEntry = if (showCompleted) {
            entryList[position]
        }
        else {
            entryListFiltered[position]
        }


        holder.bind(myScheduleEntry)

        holder.itemView.setOnClickListener {
            myScheduleCellClickListener.onMyScheduleCellClickListener(myScheduleEntry, position)
        }

    }

    override fun getItemCount(): Int {
        if (showCompleted) {
            return entryList.size
        }
        else {
            return entryListFiltered.size
        }
    }
}


class MyScheduleRowViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.my_schedule_list_item, parent, false)) {

    private var mTypeIcon: ImageView? = null
    private var mStatusIcon: ImageView? = null
    private var mTitleText: TextView? = null
    private var mNameText: TextView? = null
    private var mDaySortText: TextView? = null
    private var mDateText: TextView? = null
    private var mLockedCl: ConstraintLayout? = null

    init {
        mTypeIcon = itemView.findViewById(R.id.type_iv)
        mStatusIcon = itemView.findViewById(R.id.status_iv)
        mTitleText = itemView.findViewById(R.id.title_tv)
        mNameText = itemView.findViewById(R.id.name_tv)
        mDaySortText = itemView.findViewById(R.id.sort_tv)
        mDateText = itemView.findViewById(R.id.time_tv)
        mDateText = itemView.findViewById(R.id.time_tv)
        mLockedCl = itemView.findViewById(R.id.locked_cl)

    }

    fun bind(myScheduleEntry: MyScheduleEntry) {

        // Type icon
        when (myScheduleEntry.entryType) {
            MyScheduleEntryType.workOrder -> {
                Picasso.with(myView.context).load(R.drawable.ic_work_orders).into(mTypeIcon)
            }
            MyScheduleEntryType.lead -> {
                Picasso.with(myView.context).load(R.drawable.ic_leads).into(mTypeIcon)
            }
            else -> { // service
                Picasso.with(myView.context).load(R.drawable.ic_equipment).into(mTypeIcon)
            }
        }

        // Status icon
        when (myScheduleEntry.status) {
            "0" -> {
                Picasso.with(myView.context).load(R.drawable.ic_not_started).into(mStatusIcon)
            }
            "1" -> {
                if (myScheduleEntry.entryType == MyScheduleEntryType.service) {
                    Picasso.with(myView.context).load(R.drawable.ic_in_progress).into(mStatusIcon)
                }
                else {
                    Picasso.with(myView.context).load(R.drawable.ic_not_started).into(mStatusIcon)
                }
            }
            "2" -> {
                if (myScheduleEntry.entryType == MyScheduleEntryType.service) {
                    Picasso.with(myView.context).load(R.drawable.ic_done).into(mStatusIcon)
                }
                else {
                    Picasso.with(myView.context).load(R.drawable.ic_in_progress).into(mStatusIcon)
                }
            }
            "3" -> {
                if (myScheduleEntry.entryType == MyScheduleEntryType.service) {
                    Picasso.with(myView.context).load(R.drawable.ic_canceled).into(mStatusIcon)
                }
                else {
                    Picasso.with(myView.context).load(R.drawable.ic_done).into(mStatusIcon)
                }
            }
            "4" -> {
                if (myScheduleEntry.entryType == MyScheduleEntryType.service) {
                    Picasso.with(myView.context).load(R.drawable.ic_skipped).into(mStatusIcon)
                }
                else {
                    Picasso.with(myView.context).load(R.drawable.ic_canceled).into(mStatusIcon)
                }
            }
            "5" -> {
                Picasso.with(myView.context).load(R.drawable.ic_waiting).into(mStatusIcon)
            }
            else -> {
                Picasso.with(myView.context).load(R.drawable.ic_not_started).into(mStatusIcon)
            }
        }

        // Name and title
        mTitleText!!.text = myScheduleEntry.title
        mNameText!!.text = myScheduleEntry.name

        // Day sort
        if (myScheduleEntry.daySort != "0") {
            mDaySortText!!.text = myView.context.getString(R.string.num, myScheduleEntry.daySort)
        }
        else {
            mDaySortText!!.text = ""
        }

        if (myScheduleEntry.firm == "1") {
            mLockedCl?.visibility = View.VISIBLE
        }
        else {
            mLockedCl?.visibility = View.GONE
        }

        // Time
        if (!myScheduleEntry.startTime.isNullOrBlank() && !myScheduleEntry.endTime.isNullOrBlank()) {
            val startTime = LocalTime.parse(myScheduleEntry.startTime, GlobalVars.dateFormatterHHMMSS)
            val endTime = LocalTime.parse(myScheduleEntry.endTime, GlobalVars.dateFormatterHHMMSS)

            mDateText!!.text = myView.context.getString(R.string.x_dash_y, startTime.format(GlobalVars.dateFormatterHMMA), endTime.format(GlobalVars.dateFormatterHMMA))
        }
        else if (!myScheduleEntry.startTime.isNullOrBlank()) {
            val startTime = LocalTime.parse(myScheduleEntry.startTime, GlobalVars.dateFormatterHHMMSS)

            mDateText!!.text = startTime.format(GlobalVars.dateFormatterHMMA)
        }


    }


}