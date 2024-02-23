package com.example.AdminMatic

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.time.LocalTime


class PlannedDateRowAdapter(list: MutableList<PlannedDate>, private val plannedDateSection: PlannedDateSection, private val plannedDateDelegate: PlannedDateDelegate) : RecyclerView.Adapter<PlannedDateRowViewHolder>() {

    var filterList:MutableList<PlannedDate> = emptyList<PlannedDate>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannedDateRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlannedDateRowViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: PlannedDateRowViewHolder, position: Int) {

        val plannedDate: PlannedDate = filterList[position]
        holder.bind(plannedDate, plannedDateSection, plannedDateDelegate)
        //println("queryText = $queryText")

    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class PlannedDateRowViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.planned_date_list_item, parent, false)), AdapterView.OnItemSelectedListener {

    private var mCrewSpinner: Spinner? = null
    private var mTimeStartEt: EditText? = null
    private var mTimeStopEt: EditText? = null
    private var mDeleteText: TextView? = null
    private var plannedDateDelegate: PlannedDateDelegate? = null
    private var plannedDate: PlannedDate? = null
    private var plannedDateSection: PlannedDateSection? = null

    var timeStart: LocalTime? = null
    var timeStop: LocalTime? = null

    init {
        mCrewSpinner = itemView.findViewById(R.id.crew_spinner)
        mTimeStartEt = itemView.findViewById(R.id.time_start_et)
        mTimeStopEt = itemView.findViewById(R.id.time_stop_et)
        mDeleteText = itemView.findViewById(R.id.delete_btn_tv)

    }

    fun bind(_plannedDate: PlannedDate, _plannedDateSection: PlannedDateSection, _plannedDateDelegate: PlannedDateDelegate) {

        plannedDateDelegate = _plannedDateDelegate
        plannedDate = _plannedDate
        plannedDateSection = _plannedDateSection

        val crewNameList = mutableListOf<String>()
        GlobalVars.crews!!.forEach {
            crewNameList.add(it.name)
        }
        val crewAdapter = ArrayAdapter(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            crewNameList.toTypedArray()
        )
        mCrewSpinner!!.adapter = crewAdapter
        mCrewSpinner!!.onItemSelectedListener = this@PlannedDateRowViewHolder

        if (plannedDate!!.crewID != "") {
            for (i in 0 until GlobalVars.crews!!.size) {
                if (GlobalVars.crews!![i].ID == plannedDate!!.crewID) {
                    plannedDate!!.crewID = "0" // To fix bug with the selector thinking it found a duplicate
                    mCrewSpinner!!.setSelection(i)

                }
            }
        }

        if (plannedDate!!.startTime != "") {
            timeStart = LocalTime.parse(plannedDate!!.startTime, GlobalVars.dateFormatterHHMMSS)
            mTimeStartEt!!.setText(timeStart!!.format(GlobalVars.dateFormatterHMMA))
        }
        else {
            timeStart = LocalTime.of(9, 0)
        }
        if (plannedDate!!.endTime != "") {
            timeStop = LocalTime.parse(plannedDate!!.endTime, GlobalVars.dateFormatterHHMMSS)
            mTimeStopEt!!.setText(timeStop!!.format(GlobalVars.dateFormatterHMMA))
        }
        else {
            timeStop = LocalTime.of(17, 0)
        }

        mTimeStartEt!!.setOnClickListener {
            val timePicker = TimePickerHelper(myView.context, false, true)
            timePicker.showDialog(timeStart!!.hour, timeStart!!.minute, object : TimePickerHelper.Callback {
                override fun onTimeSelected(hourOfDay: Int, minute: Int) {
                    plannedDateDelegate!!.flagEditsMade(true)

                    val newTimeStart = LocalTime.of(hourOfDay, minute)

                    if (plannedDate!!.endTime != "" && newTimeStart >= timeStop) {
                        globalVars.simpleAlert(myView.context, myView.context.getString(R.string.dialogue_error),myView.context.getString(R.string.dialogue_start_time_after_stop))
                        if (plannedDate!!.startTime == "") {
                            mTimeStartEt!!.setText("")
                        }
                    }
                    else {
                        timeStart = newTimeStart

                        //Set DB time
                        plannedDate!!.startTime = timeStart!!.format(GlobalVars.dateFormatterHHMMSS)

                        // Display 12 hour time
                        mTimeStartEt!!.setText(timeStart!!.format(GlobalVars.dateFormatterHMMA))
                    }


                }
            })
        }

        mTimeStopEt!!.setOnClickListener {
            val timePicker = TimePickerHelper(myView.context, false, true)
            timePicker.showDialog(timeStop!!.hour, timeStop!!.minute, object : TimePickerHelper.Callback {
                override fun onTimeSelected(hourOfDay: Int, minute: Int) {
                    plannedDateDelegate!!.flagEditsMade(true)

                    val newTimeStop = LocalTime.of(hourOfDay, minute)

                    if (plannedDate!!.startTime != "" && newTimeStop <= timeStart) {
                        globalVars.simpleAlert(myView.context, myView.context.getString(R.string.dialogue_error),myView.context.getString(R.string.dialogue_stop_time_before_start))
                        if (plannedDate!!.endTime == "") {
                            mTimeStopEt!!.setText("")
                        }
                    }
                    else {
                        timeStop = newTimeStop

                        //Set DB time
                        plannedDate!!.endTime = timeStop!!.format(GlobalVars.dateFormatterHHMMSS)

                        println("plannedDate!!.endTime = ${plannedDate!!.endTime}")

                        // Display 12 hour time
                        mTimeStopEt!!.setText(timeStop!!.format(GlobalVars.dateFormatterHMMA))
                    }


                }
            })
        }

        mDeleteText!!.setOnClickListener {
            val builder = AlertDialog.Builder(myView.context)
            builder.setTitle(myView.context.getString(R.string.dialogue_delete_planned_date_row_title))
            builder.setMessage(R.string.dialogue_delete_planned_date_row_body)
            builder.setPositiveButton(myView.context.getString(R.string.yes)) { _, _ ->
                plannedDateSection!!.rows.remove(plannedDate)
                plannedDateDelegate!!.reloadRecycler()
            }
            builder.setNegativeButton(R.string.no) { _, _ ->
            }
            builder.show()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val newCrewID = GlobalVars.crews?.get(position)?.ID.toString()
        var foundCrew = false

        plannedDateSection!!.rows.forEach {
            if (it.crewID == newCrewID) {
                foundCrew = true
            }
        }

        if (foundCrew && newCrewID != "0") {
            mCrewSpinner!!.setSelection(0)
            plannedDate!!.crewID = "0"
            globalVars.simpleAlert(myView.context, myView.context.getString(R.string.dialogue_error),myView.context.getString(R.string.dialogue_crew_already_has_time))
        }
        else {
            plannedDateDelegate!!.flagEditsMade(true)
            plannedDate!!.crewID = newCrewID
            println("New crew ID: $newCrewID")
        }
    }

}