package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.time.LocalDate


class PlannedDateSectionAdapter(list: MutableList<PlannedDateSection>, private val context: Context, private val plannedDateDelegate: PlannedDateDelegate) : RecyclerView.Adapter<PlannedDateSectionViewHolder>() {

    var filterList:MutableList<PlannedDateSection> = emptyList<PlannedDateSection>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannedDateSectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlannedDateSectionViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: PlannedDateSectionViewHolder, position: Int) {

        val plannedDateSection: PlannedDateSection = filterList[position]
        holder.bind(plannedDateSection, plannedDateDelegate)
        //println("queryText = $queryText")

        val rowList = plannedDateSection.rows


        val adapter = PlannedDateRowAdapter(rowList, plannedDateSection, plannedDateDelegate)

        holder.mRecycler!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter
        /*
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        holder.mRecycler!!.addItemDecoration(itemDecoration)

         */



    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class PlannedDateSectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.planned_date_header, parent, false)) {
    private var mDateEt: EditText? = null
    private var mlockSwitch: SwitchCompat? = null
    private var mAddCrewText: TextView? = null
    var mRecycler: RecyclerView? = null

    //var h = 0
    var m = 0


    init {
        mDateEt = itemView.findViewById(R.id.date_et)
        mlockSwitch = itemView.findViewById(R.id.lock_switch)
        mAddCrewText = itemView.findViewById(R.id.add_crew_btn_tv)
        mRecycler = itemView.findViewById(R.id.recycler_view)
    }

    fun bind(plannedDateSection: PlannedDateSection, plannedDateDelegate: PlannedDateDelegate) {



        mDateEt!!.setOnClickListener {
            val sectionDate = LocalDate.parse(plannedDateSection.plannedDate, GlobalVars.dateFormatterYYYYMMDD)
            val datePicker = DatePickerHelper(myView.context, true)
            datePicker.showDialog(sectionDate.year, sectionDate.monthValue-1, sectionDate.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    plannedDateDelegate.flagEditsMade(true)
                    val selectedDate = LocalDate.of(year, month+1, dayOfMonth)
                    mDateEt!!.setText(selectedDate.format(GlobalVars.dateFormatterWeekday))
                    val newDate = selectedDate.format(GlobalVars.dateFormatterYYYYMMDD)
                    plannedDateSection.plannedDate = newDate
                    plannedDateSection.rows.forEach {
                        it.plannedDate = newDate
                    }
                }
            })
        }

        val sectionDate = LocalDate.parse(plannedDateSection.plannedDate, GlobalVars.dateFormatterYYYYMMDD)
        mDateEt!!.setText(sectionDate.format(GlobalVars.dateFormatterWeekday))

        mlockSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            plannedDateDelegate.flagEditsMade(true)
            mDateEt!!.isEnabled = !isChecked
            if (isChecked) {
                plannedDateSection.firm = "1"
                plannedDateSection.rows.forEach {
                    it.firm = "1"
                }
            }
            else {
                plannedDateSection.firm = "0"
                plannedDateSection.rows.forEach {
                    it.firm = "0"
                }
            }
        }
        if (plannedDateSection.firm == "1") {
            mlockSwitch!!.isChecked = true
            mlockSwitch!!.jumpDrawablesToCurrentState()

        }

        mAddCrewText!!.setOnClickListener {
            val newRow = when (plannedDateSection.type) {
                PlannedDateType.WorkOrder -> {
                    PlannedDate(plannedDateSection.workOrderID, "0", "0", "0", plannedDateSection.plannedDate, plannedDateSection.firm, "", "", "0", "0")
                }
                PlannedDateType.Lead -> {
                    PlannedDate(plannedDateSection.workOrderID, "0", "0", "0", plannedDateSection.plannedDate, plannedDateSection.firm, "", "", "0", "0")
                }
                PlannedDateType.Service -> {
                    PlannedDate(plannedDateSection.workOrderID, "0", "0", "0", plannedDateSection.plannedDate, plannedDateSection.firm, "", "", "0", "0")
                }
            }

            plannedDateSection.rows.add(newRow)
            plannedDateDelegate.reloadRecycler()
        }

    }
}