package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentUsageEntryBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

interface UsageEditListener {
    fun deleteUsage(row:Int)
    fun editStart(row:Int)
    fun editStop(row:Int)
    fun editBreak(row:Int, lunch:String, actionID:Int)
    fun editTotal(row:Int, total:String, actionID:Int)
    fun editQty(row:Int, qty:String, actionID:Int)
    fun editVendor(row:Int,vendor:String)
    fun editCost(row: Int, cost:String, actionID:Int, updateUsageTable:Boolean)
    fun showHistory()
    fun toggleTotalOnly(row:Int)
    fun setEditsMade(row:Int)
}



class UsageEntryFragment : Fragment(), UsageEditListener, AdapterView.OnItemSelectedListener {

    private  var woItem: WoItem? = null
    lateinit var workOrder:WorkOrder

    lateinit var globalVars:GlobalVars

    //private var adapter: UsageAdapter? = null

    //lateinit var empsOnWo:Array<Employee>

    private var dataLoaded = false

    private var initialViewsLaidOut = false

    var usageToLog:MutableList<Usage> = mutableListOf()

    private var dateValue: LocalDate = LocalDate.now()
    private lateinit var datePicker: DatePickerHelper

    private lateinit var timePicker: TimePickerHelper

    //private var editsMade: Boolean = false
    private var submitIndex = 0
    private var usageExceedsEstimate = false

    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")
            workOrder = it.getParcelable("workOrder")!!
        }

        setFragmentResultListener("refreshWoItemListener") { _, bundle ->
            val shouldRefresh = bundle.getBoolean("shouldRefreshWoItemListener")
            if (shouldRefresh) {
                println("got result listener")
                getWoItem()
            }
        }

    }

    private var _binding: FragmentUsageEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentUsageEntryBinding.inflate(inflater, container, false)
        myView = binding.root

        setHasOptionsMenu(true)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")

                var localEditsMade = false

                for (usage in usageToLog) {
                    if (usage.editsMade) {
                        localEditsMade = true
                    }
                }

                if (localEditsMade) {
                    println("edits made")
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle("Unsaved Changes")
                    builder.setMessage("Go back without saving?")
                    builder.setPositiveButton("YES") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()
                        println("go back")

                        myView.findNavController().navigateUp()


                    }
                    builder.setNegativeButton("NO") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()

                        println("stay here")
                    }
                    builder.show()
                }
                else {
                    println("go back")

                    myView.findNavController().navigateUp()

                    //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    //val navController = navHostFragment.navController


                    //navController.navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)


        // Inflate the layout for this fragment
        return myView


        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_usage_entry, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("Usage Entry View")

        println("item = ${woItem!!.item}")

        println("workOrder.ID = ${workOrder.woID}")

        println("workOrder.emps = ${workOrder.emps}")


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.enter_usage)

        binding.usageDateEt.setText(dateValue.format(GlobalVars.dateFormatterShort))
        binding.usageDateEt.setOnClickListener {
            datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
            datePicker.showDialog(dateValue.year, dateValue.monthValue-1, dateValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {

                    var pendingEdits = false

                    if (usageToLog.isNotEmpty()) {
                        for (usage in usageToLog) {
                            if (usage.editsMade) {
                                pendingEdits = true
                            }
                        }
                    }
                    else {
                        dateValue = LocalDate.of(year, month+1, dayOfMonth)
                        binding.usageDateEt.setText(dateValue.format(GlobalVars.dateFormatterShort))
                        addActiveUsage()
                    }

                    if (pendingEdits) {
                        val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                        builder.setTitle(getString(R.string.save_usage_before_date_change_title))
                        builder.setMessage(getString(R.string.save_usage_before_date_change_body))

                        builder.setPositiveButton(getString(R.string.submit)) { _, _ ->
                            submitUsage()
                        }

                        builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->

                        }

                        builder.show()
                    }
                    else {
                        dateValue = LocalDate.of(year, month+1, dayOfMonth)
                        binding.usageDateEt.setText(dateValue.format(GlobalVars.dateFormatterShort))
                        addActiveUsage()
                    }

                }
            })
        }

        // We do already have the woitem passed, but we call get here to ensure that when the receipt is added, it will show up. A bit brute force, but works for now

        if (!dataLoaded) {
            showProgressView()
            getWoItem()
        }
        else {
            layoutViews()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.usage_entry_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.history_item)



        if ((woItem?.extraUsage ?: "0") == "1") {
            item.isEnabled = true
        }
        else {
            item.isEnabled = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.history_item) {
            val directions = UsageEntryFragmentDirections.navigateUsageEntryToUsageHistory(woItem!!)
            myView.findNavController().navigate(directions)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("usageEntry")
    }

    private fun addActiveUsage() {

        println("addActiveUsage()")

        usageToLog.clear()


        val dateValueShort = dateValue.format(GlobalVars.dateFormatterShort)
        for (usage in woItem!!.usage!!) {

            println("iterating through a usage item on woItem")
            println("total only: ${usage.total_only}")


            var startShort = ""

            if ((usage.start != null && usage.start != "0000-00-00 00:00:00") || usage.total_only == "1") {
                val dateFromStart = LocalDate.parse(usage.start!!, GlobalVars.dateFormatterPHP)
                startShort = dateFromStart.format(GlobalVars.dateFormatterShort)
            }

            println(usage.start!!)
            println(startShort)
            println(dateValueShort)

            if (startShort == dateValueShort) {
                if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID) {
                    usage.locked = true
                }

                println("adding usage")
                usageToLog.add(usage)

            }

        }

        println("usageToLog.count = ${usageToLog.count()}")

        //add rows for emps on work order, only if no existing usage has been logged
        if (woItem!!.type == "1") {

            for (emp in workOrder.emps) {
                print("empName = ${emp.name}")

                var empAlreadyIncluded = false

                for (usage in usageToLog) {
                    if (usage.empID == emp.ID) {
                        empAlreadyIncluded = true
                    }
                }

                if (!empAlreadyIncluded) {
                    val usage = Usage("0", workOrder.woID, woItem!!.ID, woItem!!.type, GlobalVars.loggedInEmployee!!.ID,"0.00", "0")

                    usage.empID = emp.ID
                    usage.empName = emp.name
                    usage.pic = emp.pic
                    usage.depID = emp.depID
                    usage.unitPrice = woItem!!.price
                    usage.totalPrice = woItem!!.total
                    usage.chargeType = woItem!!.charge
                    usage.start = null
                    usage.stop = null
                    usage.lunch = ""
                    usage.vendor = ""
                    usage.unitCost = ""
                    usage.totalCost = ""
                    usage.del = ""
                    usage.override = "0"
                    usage.locked = false

                    usageToLog.add(0, usage)
                }



            }

            updateUsageTable()



        }
        else {
            //material type

            if (usageToLog.isEmpty()){


                val usage = Usage("0", workOrder.woID,woItem!!.ID,woItem!!.type,GlobalVars.loggedInEmployee!!.ID,"0.00", "0")
                usage.empID = null
                usage.empName = null
                usage.pic = null
                usage.depID = null
                usage.unitPrice = woItem!!.price
                usage.totalPrice = woItem!!.total
                usage.chargeType = woItem!!.charge
                usage.override = "1"
                usage.locked = false

                usage.start = globalVars.getDBDateStringFromDate(Date())
                usage.stop = globalVars.getDBDateStringFromDate(Date())
                usage.lunch = "0.00"

                usage.vendor = ""
                usage.unitCost = ""
                usage.totalCost = ""
                usage.del = "0"

                usageToLog.add(0,usage)

            }

        }

        updateUsageTable()

    }



    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")


        if (position != 0){
            addEmployee(GlobalVars.employeeList!![position - 1])
            binding.usageEmpSpinner.setSelection(0,false)
        }

    }



    private fun addEmployee(emp:Employee){
        println("add employee with $emp")

        val usage = Usage("0", workOrder.woID,woItem!!.ID,woItem!!.type,GlobalVars.loggedInEmployee!!.ID,"0.00", "0")
        usage.unitPrice = woItem?.price
        usage.totalPrice = woItem?.total
        usage.chargeType = woItem?.charge
        usage.override = "0"
        usage.locked = false

        usage.empID = emp.ID
        usage.empName = emp.name
        usage.pic = emp.pic
        usage.depID = emp.depID
        usage.start = null
        usage.stop = null
        usage.lunch = ""

        usage.vendor = ""
        usage.unitCost = ""
        usage.totalCost = ""
        usage.del = ""
        usage.editsMade = true

        usageToLog.add(usage)

        updateUsageTable()

    }

    private fun updateUsageTable(){
        println("updateUsageTable")

        binding.usageEntryRv.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = activity?.let {
                UsageAdapter(
                    usageToLog,
                    it,
                    this@UsageEntryFragment,
                    woItem as WoItem,
                    workOrder
                )
            }



        }

        binding.usageEntryRv.adapter?.notifyDataSetChanged()

    }

    /*
    private fun refreshUsageTable(){
        println("refreshUsageTable")
        binding.usageEntryRv.adapter!!.notifyDataSetChanged()
    }

     */



    private fun insertStartValue(usage: Usage, startValue: LocalDateTime) : Boolean {
        if (!usage.locked) {

            if (usage.stopDateTime != null && usage.stopDateTime!! < startValue) {
                println("start is after stop")
                globalVars.playErrorSound(myView.context)
                globalVars.simpleAlert(myView.context,"Start Time Error","${usage.empName!!}'s start time can not be later then their stop time.")
                usage.startDateTime = null
                usage.start = null
                usage.editsMade = true
                return false

            }
            else {
                usage.startDateTime = startValue
                usage.start = GlobalVars.dateFormatterPHP.format(startValue)
                usage.editsMade = true
            }
        }
        return true
    }


    private fun insertStopValue(usage: Usage, stopValue: LocalDateTime) : Boolean {
        if (!usage.locked) {
            if (usage.startDateTime == null){
                globalVars.playErrorSound(myView.context)
                globalVars.simpleAlert(myView.context, "Stop Time Error","${usage.empName} has no start time.")
                return false
            }
            else if (usage.startDateTime!! > stopValue) {
                println("stop is before start")
                globalVars.playErrorSound(myView.context)
                globalVars.simpleAlert(myView.context,"Stop Time Error","${usage.empName}'s stop time can not be earlier then their start time.")
                usage.stopDateTime = null
                usage.stop = null
                usage.editsMade = true
                return false
            }
            else {
                usage.stopDateTime = stopValue
                usage.stop = GlobalVars.dateFormatterPHP.format(stopValue)
                usage.editsMade = true
                println("New stopDateTime: ${usage.stopDateTime}")
            }
        }
        return true
    }

    private fun insertBreakValue(usage: Usage, breakValue: String) : Boolean {

        if (usage.startDateTime == null || usage.stopDateTime == null) {

            globalVars.playErrorSound(myView.context)
            globalVars.simpleAlert(myView.context,"Break Time Error","Add start and stop time before break time.")

            usage.lunch = null

            return false
        }

        usage.lunch = breakValue.replace(",",".")
        
        return true
    }


    private fun insertTotalValue(usage: Usage, totalMinutes: String) : Boolean {

        if (usage.total_only == "0") {
            usage.total_only = "1"
            usage.start = null
            usage.startDateTime = null
            usage.stop = null
            usage.stopDateTime = null
            usage.lunch = null

        }

        usage.qty = "%.2f".format(totalMinutes.toDouble()/60)

        println("Setting qty to ${"%.2f".format(totalMinutes.toDouble()/60)}")

        usage.editsMade = true

        updateUsageTable()
        return true
    }


    //methods for button taps
    private fun startBtnPressed() {
        println("startBtnPressed")

        val cal:Calendar = Calendar.getInstance()
        val currentDateTime = LocalDateTime.of(dateValue.year, dateValue.month, dateValue.dayOfMonth, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        for (usage in usageToLog) {
            if (usage.startDateTime == null && usage.total_only != "1") {
                insertStartValue(usage, currentDateTime)
            }
        }

        setQty()
    }


    private fun stopBtnPressed(){
        println("stopBtnPressed")

        val cal:Calendar = Calendar.getInstance()
        val currentDateTime = LocalDateTime.of(dateValue.year, dateValue.month, dateValue.dayOfMonth, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        for (usage in usageToLog) {
            insertStopValue(usage, currentDateTime)
        }

        setQty()
    }


    //methods for text edits
    override fun editStart(row: Int) {

        val cal:Calendar
        val h:Int
        val m:Int
        if (usageToLog[row].startDateTime == null) {
            println("usage start == null")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }
        else {
            h = usageToLog[row].startDateTime!!.hour
            m = usageToLog[row].startDateTime!!.minute
        }

        timePicker = TimePickerHelper(myView.context, false, true)
        timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
            override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                val selectedDateTime = LocalDateTime.of(dateValue.year, dateValue.month, dateValue.dayOfMonth, hourOfDay, minute)

                if (insertStartValue(usageToLog[row], selectedDateTime)) {
                    editOthersStart(row)
                }
                setQty()
            }
        })
    }


    override fun editStop(row: Int) {

        val cal:Calendar
        val h:Int
        val m:Int
        if (usageToLog[row].stopDateTime == null) {
            println("usage stop == null")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }
        else {
            h = usageToLog[row].stopDateTime!!.hour
            m = usageToLog[row].stopDateTime!!.minute
        }

        timePicker = TimePickerHelper(myView.context, false, true)
        timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
            override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                val selectedDateTime = LocalDateTime.of(dateValue.year, dateValue.month, dateValue.dayOfMonth, hourOfDay, minute)

                if (insertStopValue(usageToLog[row], selectedDateTime)) {
                    editOthersStop(row)
                }
                setQty()
            }
        })

    }




    override fun editBreak(row: Int, lunch: String, actionID:Int) {

        try {
            val num = java.lang.Double.parseDouble(lunch.replace(',', '.'))
        } catch (e: NumberFormatException) {
            // numeric = false

            globalVars.playErrorSound(myView.context)
            globalVars.simpleAlert(myView.context,"Break Time Error","Break time must be a number of minutes.")

            usageToLog[row].lunch = null
            setQty()
            // updateUsageTable()
            return
        }

        if (actionID == EditorInfo.IME_ACTION_DONE) {

            println("done btn hit")
            
            if (insertBreakValue(usageToLog[row], lunch)) {
                editOthersBreak(row)
            }
            setQty()

        }
    }

    override fun editTotal(row: Int, total: String, actionID:Int) {
        if (actionID == EditorInfo.IME_ACTION_DONE) {
            println("done btn hit")
            if (insertTotalValue(usageToLog[row], total)) {
                editOthersTotal(total)
            }
        }
    }


    fun editOthersStart(row:Int) {
        println("editOthersStart")

        var locked = true
        if (usageToLog.count() > 1){
            for (usage in usageToLog) {
                if (!usage.locked) {
                    locked = false
                }
            }
            if (!locked) {

                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle("Update Everyone's Start Time?")
                builder.setPositiveButton(getString(R.string.dialogue_yes)) { _, _ ->
                    for (usage in usageToLog) {
                        if (usage.startDateTime == null) {
                            insertStartValue(usage, usageToLog[row].startDateTime!!)
                        }
                    }
                    setQty()
                }
                builder.setNegativeButton(getString(R.string.dialogue_no)) { _, _ ->
                    setQty()
                }
                builder.show()
            }
            else {
                setQty()
            }
        }
        else {
            setQty()
        }
    }

    fun editOthersStop(row:Int){

        println("editOthersStop")

        var locked = true
        if (usageToLog.count() > 1){
            for (usage in usageToLog) {
                if (!usage.locked) {
                    locked = false
                }
            }
            if (!locked) {

                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle("Update Everyone's Stop Time?")
                builder.setPositiveButton(getString(R.string.dialogue_yes)) { _, _ ->
                    for (usage in usageToLog) {
                        insertStopValue(usage, usageToLog[row].stopDateTime!!)
                    }
                    setQty()
                }
                builder.setNegativeButton(getString(R.string.dialogue_no)) { _, _ ->
                    setQty()
                }
                builder.show()
            }
            else {
                setQty()
            }
        }
        else {
            setQty()
        }


    }

    private fun editOthersBreak(row:Int){

        var locked = true
        if (usageToLog.count() > 1){
            for (usage in usageToLog) {
                if (!usage.locked) {
                    locked = false
                }
            }
            if (!locked) {

                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle("Update Everyone's Break Time?")
                builder.setPositiveButton(getString(R.string.dialogue_yes)) { _, _ ->
                    for (usage in usageToLog) {
                        if (!usage.locked && usage.total_only != "1") {
                            insertBreakValue(usage, usageToLog[row].lunch!!)
                        }
                    }
                    setQty()
                }
                builder.setNegativeButton(getString(R.string.dialogue_no)) { _, _ ->
                    setQty()
                }
                builder.show()
            }
            else {
                setQty()
            }
        }
        else {
            setQty()
        }

    }


    private fun editOthersTotal(total:String) {

        var locked = true
        if (usageToLog.count() > 1){
            for (usage in usageToLog) {
                if (!usage.locked) {
                    locked = false
                }
            }
            if (!locked) {

                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.edit_everyones_total_time))
                builder.setPositiveButton(getString(R.string.dialogue_yes)) { _, _ ->
                    for (usage in usageToLog) {
                        if (!usage.locked) {
                            insertTotalValue(usage, total)
                        }
                    }
                }
                builder.setNegativeButton(getString(R.string.dialogue_no)) { _, _ ->

                }
                builder.show()
            }
        }
    }



    private fun setQty(){
        println("setQty")

        for (usage in usageToLog) {
            if ((usage.startDateTime == null && usage.stopDateTime == null) || usage.total_only == "1") {
                println("blank row")
            }
            else {

                var qtySeconds: Long

                if (usage.startDateTime == null || usage.stopDateTime == null) {
                    println("start or stop is null")
                    usage.qty = "0.00"
                }
                else {

                    val startTime = usage.startDateTime
                    val stopTime = usage.stopDateTime

                    if (startTime != null && stopTime != null) {
                        qtySeconds = startTime.until(stopTime, ChronoUnit.SECONDS)
                        usage.qty =   startTime.until(stopTime, ChronoUnit.HOURS).toString()

                        var breakTime = 0.0
                        if (usage.lunch != null && usage.lunch != "" && usage.lunch != "0") {
                            breakTime = usage.lunch!!.toDouble() * 60
                            if (breakTime >= qtySeconds) {
                                globalVars.playErrorSound(myView.context)
                                globalVars.simpleAlert(myView.context,"Break Time Error","${usage.empName!!}'s break time can not be greater or equal to total time.")
                                usage.lunch = "0"
                                breakTime = 0.0
                            }
                        }

                        val qtyHours = (qtySeconds - breakTime) / 3600
                        val decimal = BigDecimal(qtyHours).setScale(2, RoundingMode.HALF_EVEN)
                        println(decimal)

                        usage.qty = decimal.toString()

                    }
                    else {
                        usage.qty = "0.00"
                    }

                }

                usage.editsMade = true

            }

        }

        updateUsageTable()

    }


    override fun deleteUsage(row: Int) {
        println("deleteUsage row = $row")

        if (usageToLog.isNotEmpty()) {
            if (!usageToLog[row].locked) {
                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.delete_usage_title))
                builder.setMessage(getString(R.string.delete_usage_title))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    if (usageToLog[row].ID == "0") {
                        usageToLog.removeAt(row)
                        updateUsageTable()
                    }
                    else {
                        usageToLog[row].del = "1"
                        usageToLog[row].editsMade = true
                        println("Setting .del to '1' and .editsMade to true for row $row")
                        //usageToLog.removeAt(row)
                        submitUsage()

                        //usageToLog.removeAt(row)
                        //updateUsageTable()
                    }

                }
                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }
                builder.show()
            }
            else {
                globalVars.simpleAlert(myView.context, getString(R.string.cant_delete_saved_rows))
            }
        }
    }

    private fun submitUsage(){
        println("submitUsage")

        if (usageToLog.isEmpty()) {
            return
        }

        var foundStartTime = false
        usageToLog.forEach {
            if (it.start != null) {
                foundStartTime = true
            }

            if (it.total_only == "1" && it.qty != "0.0" && it.qty != "") {
                foundStartTime = true
            }
        }

        for ((i, usage) in usageToLog.withIndex()) {
            var usageQty = 0.0
            println("usage.qty = ${usage.qty}")
            if(usage.qty != "0.0" && usage.qty != ""){
                println("set usage.qty to 0.0")
                usageQty = usage.qty.toDouble()
            }
            //usage.del = "0"

            println("usageQty = $usageQty")

            if (!usage.locked) {
                if (usage.type == "1") {
                    //labor
                    if (!foundStartTime && usage.del != "1") {
                        globalVars.playErrorSound(myView.context)
                        globalVars.simpleAlert(myView.context, "Start Time Error","No employees have a start time.")
                        return
                    }
                    else {
                        if (usageQty > 0.0) {
                            if (GlobalVars.loggedInEmployee!!.ID != usage.addedBy) {
                                usage.locked = true
                            }
                        }
                    }
                }
                else {
                    //material

                    if (usageQty > 0.0) {
                        if (GlobalVars.loggedInEmployee!!.ID != usage.addedBy) {
                            usage.locked = true
                        }

                        usage.startDateTime = dateValue.atStartOfDay()
                        usage.stopDateTime = dateValue.atStartOfDay()
                        usage.override = "1"

                    }
                    else {
                        globalVars.simpleAlert(myView.context, getString(R.string.qty_error_title),getString(R.string.qty_error_body))
                    }

                    if (usage.vendor == null || usage.vendor == "") {
                        //globalVars.simpleAlert(myView.context, "Error","No vendor selected.")
                        //return
                        usage.vendor = "0"
                    }
                }
            }
        }

        callDB()

    }

    private fun callDB(override: String = "0") {
        println("callDB with index $submitIndex")

        var usageWithEdits = 0

        for (usage in usageToLog) {
            if (usage.editsMade) {
                usageWithEdits += 1
            }
        }

        println("Usage with edits: $usageWithEdits")

        if (usageWithEdits > 0) {

            // Skip this cell if it's not flagged with edits made or if it has no start time unless it's in total only mode
            if (!usageToLog[submitIndex].editsMade || (usageToLog[submitIndex].startDateTime == null && usageToLog[submitIndex].total_only == "0")) {
                handleSaveComplete()
                return
            }

            if (usageToLog[submitIndex].empID == null) { usageToLog[submitIndex].empID = ""}
            if (usageToLog[submitIndex].empName == null) { usageToLog[submitIndex].empName = ""}
            if (usageToLog[submitIndex].depID == null) { usageToLog[submitIndex].depID = ""}
            if (usageToLog[submitIndex].lunch == null) { usageToLog[submitIndex].lunch = ""}
            if (usageToLog[submitIndex].vendor == null) { usageToLog[submitIndex].vendor = ""}
            if (usageToLog[submitIndex].unitPrice == null) { usageToLog[submitIndex].unitPrice = ""}
            if (usageToLog[submitIndex].totalPrice == null) { usageToLog[submitIndex].totalPrice = ""}
            if (usageToLog[submitIndex].unitCost == null) { usageToLog[submitIndex].unitCost = ""}
            if (usageToLog[submitIndex].unitCost == null) { usageToLog[submitIndex].unitCost = ""}
            if (usageToLog[submitIndex].totalCost == null) { usageToLog[submitIndex].totalCost = ""}
            if (usageToLog[submitIndex].chargeType == null) { usageToLog[submitIndex].chargeType = ""}
            if (usageToLog[submitIndex].del == null) { usageToLog[submitIndex].del = ""}



            if (usageToLog[submitIndex].total_only == "1") {
                val newDateValue = LocalDateTime.of(dateValue.year, dateValue.month, dateValue.dayOfMonth, 0, 0)
                usageToLog[submitIndex].start = GlobalVars.dateFormatterPHP.format(newDateValue)
                usageToLog[submitIndex].startDateTime = newDateValue
                usageToLog[submitIndex].stop = GlobalVars.dateFormatterPHP.format(newDateValue)
                usageToLog[submitIndex].stopDateTime = newDateValue

            }

            

            usageToLog[submitIndex].progressViewVisible = true
            binding.usageEntryRv.adapter?.notifyItemChanged(submitIndex)

            var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/usage_single.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView, suppressWarnings = true)) {

                            usageToLog[submitIndex].progressViewVisible = false
                            println("before notifyitemchanged 2")
                            binding.usageEntryRv.adapter?.notifyItemChanged(submitIndex)
                            println("after notifyitemchanged 2")


                            val gson = GsonBuilder().create()
                            val overlap: String = gson.fromJson(parentObject["overlap"].toString(), String::class.java)

                            if (overlap == "1") {
                                val builder = AlertDialog.Builder(myView.context)
                                builder.setTitle(getString(R.string.overlapping_usage_title))
                                builder.setMessage(getString(R.string.overlapping_usage_body))
                                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                                    callDB("1")
                                }
                                builder.setNegativeButton(android.R.string.cancel) { _, _ ->
                                    handleSaveComplete()
                                }
                                builder.show()
                            }
                            else {
                                handleSaveComplete()
                            }


                        }


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
                    params["ID"] = usageToLog[submitIndex].ID
                    params["woID"] = usageToLog[submitIndex].woID
                    params["itemID"] = usageToLog[submitIndex].woItemID
                    params["type"] = usageToLog[submitIndex].type
                    params["addedBy"] = GlobalVars.loggedInEmployee!!.ID
                    params["qty"] = usageToLog[submitIndex].qty
                    params["empID"] = usageToLog[submitIndex].empID!!
                    params["empName"] = usageToLog[submitIndex].empName!!
                    params["depID"] = usageToLog[submitIndex].depID!!
                    params["lunch"] = usageToLog[submitIndex].lunch!!
                    params["vendor"] = usageToLog[submitIndex].vendor!!
                    params["unitCost"] = usageToLog[submitIndex].unitCost!!
                    params["totalCost"] = usageToLog[submitIndex].totalCost!!
                    params["unitPrice"] = usageToLog[submitIndex].unitPrice!!
                    params["totalPrice"] = usageToLog[submitIndex].totalPrice!!
                    params["usageCharge"] = usageToLog[submitIndex].chargeType!!
                    params["del"] = usageToLog[submitIndex].del!!

                    if (usageToLog[submitIndex].total_only == "1") {
                        params["total_only"] = "1"
                        params["override"] = "1"
                    }
                    else {
                        params["override"] = override
                    }

                    if (usageToLog[submitIndex].startDateTime != null) {
                        params["start"] = GlobalVars.dateFormatterPHP.format(usageToLog[submitIndex].startDateTime!!)
                    }

                    if (usageToLog[submitIndex].stopDateTime != null) {
                        params["stop"] = GlobalVars.dateFormatterPHP.format(usageToLog[submitIndex].stopDateTime!!)
                    }

                    println("update usage single params = $params")
                    return params

                }
            }
            postRequest1.tag = "usageEntry"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
        }
        else {
            globalVars.simpleAlert(myView.context, getString(R.string.no_usage_edited))
        }


    }

    private fun handleSaveComplete() {
        submitIndex += 1

        if (submitIndex < usageToLog.size) {
            callDB()
        }
        else {
            //done submitting
            globalVars.playSaveSound(myView.context)

            submitIndex = 0

            println("done submitting")

            getWoItem()

            //todo: update wo status:
            if (workOrder.status == "1") {
                // workOrder.status = 2
                // UPDATE WORK ORDER
            }

            if (usageExceedsEstimate) {
                globalVars.simpleAlert(myView.context, getString(R.string.notice), getString(R.string.usage_exceeds_estimated))
            }

            usageExceedsEstimate = false

        }
    }


    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }


    override fun editQty(row: Int, qty: String, actionID:Int) {

        try {
            val num = java.lang.Double.parseDouble(qty.replace(',', '.'))
        } catch (e: NumberFormatException) {
            // numeric = false

            globalVars.playErrorSound(myView.context)
            globalVars.simpleAlert(myView.context,"Quantity Error","Quantity must be a number.")

            usageToLog[row].qty = "0.00"
            updateUsageTable()

            return
        }

        if (actionID == EditorInfo.IME_ACTION_DONE) {

            if (usageToLog[row].unitCost == null || usageToLog[row].unitCost == "") {
                usageToLog[row].unitCost = "0.00"
            }

            val qtyFormatted = java.lang.Double.parseDouble(qty.replace(',', '.'))
            val qtyTrimmed = (qtyFormatted * 100.0).roundToInt() / 100.0

            usageToLog[row].qty = String.format("%.2f", qtyTrimmed)
            val totalCost = (usageToLog[row].unitCost!!.toDouble() * usageToLog[row].qty.toDouble())
            usageToLog[row].totalCost = String.format("%.2f", totalCost)
            usageToLog[row].editsMade = true
            updateUsageTable()
        }
    }

    override fun editCost(row: Int, cost: String, actionID:Int, updateUsageTable:Boolean) {
        try {
            val num = java.lang.Double.parseDouble(cost.replace(',', '.'))
        } catch (e: NumberFormatException) {
            // numeric = false

            globalVars.playErrorSound(myView.context)
            globalVars.simpleAlert(myView.context,"Cost Error","Cost must be a number.")

            usageToLog[row].unitCost = null
            usageToLog[row].totalCost = null
            updateUsageTable()

            return
        }

        if (actionID == EditorInfo.IME_ACTION_DONE) {

            if (usageToLog[row].qty == "") {
                usageToLog[row].qty = "0"
            }

            val costFormatted = java.lang.Double.parseDouble(cost.replace(',', '.'))
            val costTrimmed = (costFormatted * 100.0).roundToInt() / 100.0

            usageToLog[row].unitCost = String.format("%.2f", costTrimmed)
            val totalCost = (usageToLog[row].qty.toDouble() * usageToLog[row].unitCost!!.toDouble())
            usageToLog[row].totalCost = String.format("%.2f", totalCost)
            usageToLog[row].editsMade = true
            if (updateUsageTable) {
                updateUsageTable()
            }

        }
    }

    override fun editVendor(row: Int, vendor: String) {
        usageToLog[row].vendor = vendor
        usageToLog[row].editsMade = true

    }

    private fun getWoItem(){
        println("get woItem")


        //if (!pgsBar.isVisible){
        //showProgressView()
        // }


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/workOrderItem.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        woItem = gson.fromJson(parentObject.toString(), WoItem::class.java)

                        for (usage in woItem!!.usage!!) {
                            if (usage.start != null && usage.start != "0000-00-00 00:00:00") {
                                usage.startDateTime = LocalDateTime.parse(usage.start, GlobalVars.dateFormatterPHP)
                            }
                            if (usage.stop != null && usage.stop != "0000-00-00 00:00:00") {
                                usage.stopDateTime = LocalDateTime.parse(usage.stop, GlobalVars.dateFormatterPHP)
                            }
                            usage.chargeType = woItem!!.charge
                        }

                        dataLoaded = true

                        if (!initialViewsLaidOut) {
                            layoutViews()
                        }
                        else {
                            addActiveUsage()
                        }

                        hideProgressView()

                    }



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
                params["woItemID"] = woItem!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "usageEntry"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {
        binding.startBtn.setOnClickListener {
            startBtnPressed()
        }
        binding.stopBtn.setOnClickListener {
            stopBtnPressed()
        }

        binding.usageSubmitBtn.setOnClickListener {
            submitUsage()
        }

        val empAdapter = EmpAdapter(myView.context, GlobalVars.employeeList!!.toList())
        binding.usageEmpSpinner.adapter = empAdapter


        binding.usageEmpSpinner.onItemSelectedListener = this@UsageEntryFragment

        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        binding.usageEntryRv.addItemDecoration(itemDecoration)

        println("woItem!!.type = ${woItem!!.type}")
        if (woItem!!.type != "1") {
            binding.usageEmpSpinner.visibility = View.GONE
            binding.startStopCl.visibility = View.GONE
        }
        initialViewsLaidOut = true

        addActiveUsage()
    }

    override fun showHistory() {
        TODO("Not yet implemented")
    }

    override fun toggleTotalOnly(row:Int) {
        if (usageToLog[row].total_only == "1") {
            usageToLog[row].total_only = "0"
        }
        else {
            usageToLog[row].total_only = "1"
        }
        usageToLog[row].start = null
        usageToLog[row].startDateTime = null
        usageToLog[row].stop = null
        usageToLog[row].stopDateTime = null
        usageToLog[row].qty = "0"
        usageToLog[row].lunch = null
        usageToLog[row].editsMade = true

        binding.usageEntryRv.adapter?.notifyItemChanged(row)
    }

    override fun setEditsMade(row:Int) {
        usageToLog[row].editsMade = true
    }

}