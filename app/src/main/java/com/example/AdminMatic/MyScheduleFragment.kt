package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentMyScheduleBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*

interface MyScheduleCellClickListener {
    fun onMyScheduleCellClickListener(data:MyScheduleEntry, listIndex:Int)
}

class MyScheduleSection(_date:String, _dayNote: String, _entries: MutableList<MyScheduleEntry>) {
    var date:String = _date
    var dayNote:String = _dayNote
    var entries: MutableList<MyScheduleEntry> = _entries
}

class MyScheduleFragment : Fragment(), MyScheduleCellClickListener, AdapterView.OnItemSelectedListener {

    lateinit var myView: View
    private lateinit var sectionAdapter: MyScheduleSectionAdapter

    private lateinit var employee:Employee

    var showCompleted = false

    var datesArray: Array<String> = arrayOf()

    var selectedDateIndex = 0

    var currentIteratedDate:LocalDate? = null

    var dataLoaded = false
    var isSelectionFromTouch = false

    private var _binding: FragmentMyScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")!!
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")

        globalVars = GlobalVars()
        _binding = FragmentMyScheduleBinding.inflate(inflater, container, false)
        myView = binding.root

        ((activity as AppCompatActivity).supportActionBar?.customView!!
            .findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_schedule, employee.fname)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {



        // Cache images
        Picasso.with(context).load(R.drawable.ic_not_started).fetch()
        Picasso.with(context).load(R.drawable.ic_in_progress).fetch()
        Picasso.with(context).load(R.drawable.ic_done).fetch()
        Picasso.with(context).load(R.drawable.ic_canceled).fetch()
        Picasso.with(context).load(R.drawable.ic_waiting).fetch()
        Picasso.with(context).load(R.drawable.ic_schedule).fetch()
        Picasso.with(context).load(R.drawable.ic_leads).fetch()
        Picasso.with(context).load(R.drawable.ic_equipment).fetch()


        Picasso.with(context)
            .load(GlobalVars.thumbBase + employee.pic)
            .placeholder(R.drawable.user_placeholder)
            .into(binding.empPicIv)

        binding.crewsBtn.setOnClickListener {

            when (selectedDateIndex) {
                0 -> {
                    val directions = MyScheduleFragmentDirections.navigateToCrews(employee)
                    directions.dateValue = "today"
                    myView.findNavController().navigate(directions)
                }
                1 -> {
                    val directions = MyScheduleFragmentDirections.navigateToCrews(employee)
                    directions.dateValue = "tomorrow"
                    myView.findNavController().navigate(directions)
                }
                2 -> {
                    val directions = MyScheduleFragmentDirections.navigateToCrewsWeek(employee)
                    directions.dateValue = "this week"
                    myView.findNavController().navigate(directions)
                }
                3 -> {
                    val directions = MyScheduleFragmentDirections.navigateToCrewsWeek(employee)
                    directions.dateValue = "next week"
                    myView.findNavController().navigate(directions)
                }
            }



        }

        binding.addPayrollBtn.setOnClickListener {
            val directions = MyScheduleFragmentDirections.navigateToPayroll(GlobalVars.loggedInEmployee)
            myView.findNavController().navigate(directions)
        }

        binding.mapBtn.setOnClickListener {
            val directions = MyScheduleFragmentDirections.navigateToMap(3)
            directions.name = employee.fname ?: ""
            directions.showCompleted = showCompleted
            myView.findNavController().navigate(directions)
        }

        datesArray = arrayOf(
            getString(R.string.today),
            getString(R.string.tomorrow),
            getString(R.string.this_week),
            getString(R.string.next_week),
        )

        binding.daySpinner.setBackgroundResource(R.drawable.text_view_layout)
        binding.daySpinner.onItemSelectedListener = this@MyScheduleFragment

        binding.daySpinner.setOnTouchListener { _, _ ->
            isSelectionFromTouch = true
            view.performClick()
            false
        }

        val datesAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, datesArray
        )
        datesAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        binding.daySpinner.adapter = datesAdapter

        binding.swipeContainer.setOnRefreshListener {
            getMySchedule()
        }

        binding.refreshButton.setOnClickListener {
            getMySchedule()
        }

        sectionAdapter = MyScheduleSectionAdapter(GlobalVars.globalMyScheduleSections, myView.context, this@MyScheduleFragment)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = sectionAdapter
        }
        val itemDecoration: RecyclerView.ItemDecoration = DividerItemDecoration(com.example.AdminMatic.myView.context, DividerItemDecoration.VERTICAL)
        binding.recycler.addItemDecoration(itemDecoration)


        binding.showCompletedSwitch.setOnCheckedChangeListener { _, isChecked ->
            showCompleted = isChecked
            //sectionAdapter.showCompleted = isChecked
            sectionAdapter.updateShowCompleted(isChecked)

            updateTable()
        }


        // Update tasks remaining footer label
        updateTable()

        binding.crewsBtn.text = getString(R.string.crews_x, datesArray[selectedDateIndex])
        binding.dayLblTv.text = getString(R.string.xs_schedule_for_label, employee.fname)

        println("checking for data loaded")
        if (!dataLoaded) {
            showProgressView()
            setFragmentResultListener("refresh") { _, bundle ->
                if (bundle.getBoolean("refresh")) {
                    getMySchedule()
                }
            }
            dataLoaded = true
            getMySchedule()
        }
        else {
            sectionAdapter.notifyDataSetChanged()
            hideProgressView()
        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("mySchedule")
    }

    private fun getMySchedule() {
        println("getMySchedule")


        var dateValueParam = ""

        when (selectedDateIndex) {
            0 -> {
                showProgressView()
                GlobalVars.globalMyScheduleSections.clear()
                dateValueParam = "today"
            }
            1 -> {
                showProgressView()
                GlobalVars.globalMyScheduleSections.clear()
                dateValueParam = "tomorrow"
            }
            2 -> {
                if (currentIteratedDate == null) {
                    showProgressView()
                    GlobalVars.globalMyScheduleSections.clear()
                    currentIteratedDate = LocalDate.now().with(TemporalAdjusters.previousOrSame( DayOfWeek.SUNDAY ))
                    dateValueParam = currentIteratedDate!!.format(GlobalVars.dateFormatterYYYYMMDD)
                }
                else {
                    dateValueParam = currentIteratedDate!!.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            }
            else -> { // 3, this week
                if (currentIteratedDate == null) {
                    showProgressView()
                    GlobalVars.globalMyScheduleSections.clear()
                    currentIteratedDate = LocalDate.now().with(TemporalAdjusters.next( DayOfWeek.SUNDAY ))
                    dateValueParam = currentIteratedDate!!.format(GlobalVars.dateFormatterYYYYMMDD)
                }
                else {
                    dateValueParam = currentIteratedDate!!.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            }
        }



        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/mySchedule.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("getMySchedule Response: $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        val myScheduleEntryArray = gson.fromJson(parentObject.toString(), MyScheduleEntryArray::class.java)
                        val newEntryArray = myScheduleEntryArray.entries
                        val newEntryArrayFiltered = mutableListOf<MyScheduleEntry>()

                        newEntryArray!!.forEach {

                            if (it.type == "work") {
                                it.entryType = MyScheduleEntryType.workOrder
                            }
                            else if (it.type == "lead") {
                                it.entryType = MyScheduleEntryType.lead
                            }
                            else {
                                it.entryType = MyScheduleEntryType.service
                            }

                            // Have to do this because service uses different indices for the statuses
                            if (it.entryType == MyScheduleEntryType.service) {
                                if (it.status != "2" && it.status != "3" && it.status != "4") {
                                    print("adding service to filtered")
                                    newEntryArrayFiltered.add(it)
                                }
                            }
                            else {
                                if (it.status != "3" && it.status != "4") {
                                    print("adding non-service to filtered")
                                    newEntryArrayFiltered.add(it)
                                }
                            }
                        }

                        var currentDate = currentIteratedDate
                        if (selectedDateIndex == 0) {
                            currentDate = LocalDate.now()
                        }
                        else if (selectedDateIndex == 1) {
                            currentDate = LocalDate.now().plusDays(1)
                        }

                        println("Sections size: ${GlobalVars.globalMyScheduleSections.size}")
                        println("Current date: $currentDate")
                        println("myScheduleEntryArray.note: ${myScheduleEntryArray.note}")

                        GlobalVars.globalMyScheduleSections.add(MyScheduleSection(currentDate!!.format(GlobalVars.dateFormatterWeekday), myScheduleEntryArray.note!!, newEntryArray.toMutableList()))


                        // If this or next week mode:
                        if (selectedDateIndex > 1) {

                            if (currentIteratedDate!!.dayOfWeek == DayOfWeek.SATURDAY) { // if we just filled out saturday

                                // Update map view
                                /*
                                var totalEntriesArray:[MyScheduleEntry] = []
                                for sec in self.sections {
                                    totalEntriesArray.append(contentsOf: sec.entriesFiltered)
                                }
                                if self.scheduleMapViewController != nil {
                                    self.scheduleMapViewController!.getNewMySchedule(_myScheduleEntries: totalEntriesArray)
                                }
                                */

                                // Reset the iterator for next update
                                currentIteratedDate = null
                                binding.swipeContainer.isRefreshing = false
                                updateTable()
                                hideProgressView()
                                //sectionAdapter.notifyDataSetChanged()
                            }
                            else {
                                // Move to the next day and re-call get
                                currentIteratedDate = currentIteratedDate!!.plusDays(1)
                                getMySchedule()
                            }
                        }
                        else { // Normal single-day modes

                            // Map stuff
                            /*
                            if self.scheduleMapViewController != nil{
                                self.scheduleMapViewController!.getNewMySchedule(_myScheduleEntries: self.sections[0].entriesFiltered)
                            }

                             */
                            binding.swipeContainer.isRefreshing = false
                            updateTable()
                            hideProgressView()
                            //sectionAdapter.notifyDataSetChanged()


                        }

                        sectionAdapter.notifyDataSetChanged()


                    }

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["date"] = dateValueParam
                params["employeeID"] = employee.ID
                println("getMySchedule params = $params")
                return params
            }
        }
        postRequest1.tag = "mySchedule"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun updateTable() {

        // Hide or show the "no work found" label
        var foundWork = false
        /*
        if (showCompleted) {
            for (sec in GlobalVars.globalMyScheduleSections) {
                if (sec.entries.size > 0) {foundWork = true}
            }
        }
        else {
            for (sec in GlobalVars.globalMyScheduleSections) {
                if (sec.entriesFiltered.size > 0) {foundWork = true}
            }
        }
         */

        var wosFound = 0
        var leadsFound = 0
        var servicesFound = 0

        var scheduleIsEmpty = true


        for (sec in GlobalVars.globalMyScheduleSections) {
            for (entry in sec.entries) {

                scheduleIsEmpty = false

                if (showCompleted) {
                    foundWork = true
                    when (entry.entryType) {
                        MyScheduleEntryType.workOrder -> {
                            wosFound += 1
                        }
                        MyScheduleEntryType.lead -> {
                            leadsFound += 1
                        }
                        MyScheduleEntryType.service -> {
                            servicesFound += 1
                        }
                    }
                }
                else {
                    if (!entry.checkIfCompleted()) {
                        foundWork = true
                        when (entry.entryType) {
                            MyScheduleEntryType.workOrder -> {
                                wosFound += 1
                            }
                            MyScheduleEntryType.lead -> {
                                leadsFound += 1
                            }
                            MyScheduleEntryType.service -> {
                                servicesFound += 1
                            }
                        }
                    }
                }
            }
        }


        if (foundWork) {
            binding.swipeContainer.visibility = View.VISIBLE
            binding.noWorkScheduledCl.visibility = View.INVISIBLE

            if (scheduleIsEmpty) {
                binding.noWorkScheduledTv.text = getString(R.string.no_work_scheduled)
            }
            else {
                binding.noWorkScheduledTv.text = getString(R.string.all_work_completed)
            }

        }
        else {
            binding.swipeContainer.visibility = View.INVISIBLE
            binding.noWorkScheduledCl.visibility = View.VISIBLE
            if (scheduleIsEmpty) {
                binding.noWorkScheduledTv.text = getString(R.string.no_work_scheduled)
            }
            else {
                binding.noWorkScheduledTv.text = getString(R.string.all_work_completed)
            }
        }


        /*
        for (sec in GlobalVars.globalMyScheduleSections) {
            for (entry in sec.entriesFiltered) {
                when (entry.entryType) {
                    MyScheduleEntryType.workOrder -> {
                        wosFound += 1
                    }
                    MyScheduleEntryType.lead -> {
                        leadsFound += 1
                    }
                    MyScheduleEntryType.service -> {
                        servicesFound += 1
                    }
                }
            }
        }
        */

        binding.countTv.text = getString(R.string.my_schedule_count, wosFound, leadsFound, servicesFound)

        sectionAdapter.notifyDataSetChanged()

    }

    override fun onMyScheduleCellClickListener(data:MyScheduleEntry, listIndex: Int) {
        when (data.entryType) {
            MyScheduleEntryType.workOrder -> {
                val directions = MyScheduleFragmentDirections.navigateToWorkOrder(null)
                directions.workOrderID = data.refID
                myView.findNavController().navigate(directions)
            }
            MyScheduleEntryType.lead -> {
                val directions = MyScheduleFragmentDirections.navigateToLead(data.refID)
                myView.findNavController().navigate(directions)
            }
            MyScheduleEntryType.service -> {
                val newEquipment = Equipment(
                    data.equipmentID!!,
                    data.name!!,
                    data.status!!,
                    "",
                    "1",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    data.usage,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    data.usageType,
                    null
                )

                val historyMode = data.status == "2" || data.status == "3" || data.status == "4"

                println("Type: ${data.type}")

                if (data.serviceType == "4") {
                    val directions = MyScheduleFragmentDirections.navigateToServiceInspection(null, newEquipment, historyMode)
                    directions.serviceID = data.refID
                    directions.fromMySchedule = true
                    myView.findNavController().navigate(directions)
                }
                else {
                    val directions = MyScheduleFragmentDirections.navigateToService(null, newEquipment, historyMode)
                    directions.serviceID = data.refID
                    directions.fromMySchedule = true
                    myView.findNavController().navigate(directions)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!isSelectionFromTouch) { return }
        selectedDateIndex = position

        binding.crewsBtn.text = getString(R.string.crews_x, datesArray[selectedDateIndex])

        isSelectionFromTouch = false
        getMySchedule()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        isSelectionFromTouch = false
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        println("hideProgressView")
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}