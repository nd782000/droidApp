package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UsageEntryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */



interface UsageEditListener {
    fun deleteUsage(row:Int)
    fun editStart(row:Int)
    fun editStop(row:Int)
    fun editBreak(row:Int,lunch:String, actionID:Int)
    fun editQty(row:Int,qtyDouble: Double, actionID:Int)
    fun editVendor(row:Int,vendor:String)
    fun editCost(row: Int, costDouble: Double, actionID:Int, updateUsageTable:Boolean)
    fun showHistory()
}



class UsageEntryFragment : Fragment(), UsageEditListener, AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    private var param2: String? = null

    private  var woItem: WoItem? = null
    lateinit var workOrder:WorkOrder

    lateinit var globalVars:GlobalVars

    //private var adapter: UsageAdapter? = null


    private lateinit var pgsBar: ProgressBar
    private lateinit var empSpinner: Spinner
    private lateinit var startStopCl: ConstraintLayout
    private lateinit var usageRecyclerView: RecyclerView
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var submitBtn: Button

    //lateinit var empsOnWo:Array<Employee>

    var usageToLog:MutableList<Usage> = mutableListOf()
    private var usageToLogJSONMutableList:MutableList<String> = mutableListOf()



    private lateinit var timePicker: TimePickerHelper

    private var editsMade: Boolean = false

    val gson = Gson()
    val gsonPretty = GsonBuilder().setPrettyPrinting().create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")
            workOrder = it.getParcelable("workOrder")!!

            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_usage_entry, container, false)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade){
                    println("edits made")
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle("Unsaved Changes")
                    builder.setMessage("Go back without saving?")
                    builder.setPositiveButton("YES") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()
                        println("go back")

                        parentFragmentManager.popBackStackImmediate()


                    }
                    builder.setNegativeButton("NO") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()

                        println("stay here")
                    }
                    builder.show()
                }else{
                    println("go back")

                   parentFragmentManager.popBackStackImmediate()

                    //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    //val navController = navHostFragment.navController


                    //navController.navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


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


        pgsBar = myView.findViewById(R.id.progressBar)




        startStopCl = myView.findViewById(R.id.start_stop_cl)



        empSpinner = myView.findViewById(R.id.usage_emp_spinner)
        empSpinner.setBackgroundResource(R.drawable.text_view_layout)

        startBtn = myView.findViewById(R.id.start_btn)
        startBtn.setOnClickListener{
            start()
        }
        stopBtn = myView.findViewById(R.id.stop_btn)
        stopBtn.setOnClickListener{
            stop()
        }

        submitBtn = myView.findViewById(R.id.usage_submit_btn)
        submitBtn.setOnClickListener{
            submitUsage()
        }


        val empAdapter = EmpAdapter(myView.context,GlobalVars.employeeList!!.toList())
        empSpinner.adapter = empAdapter



        empSpinner.onItemSelectedListener = this@UsageEntryFragment

        usageRecyclerView = myView.findViewById(R.id.usage_entry_rv)


        hideProgressView()

        println("woItem!!.type = ${woItem!!.type}")
        if (woItem!!.type != "1"){
            empSpinner.visibility = View.GONE
            startStopCl.visibility = View.GONE
        }


        usageToLog.clear()
        addActiveUsage()


        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        usageRecyclerView.addItemDecoration(itemDecoration)

    }



    private fun addActiveUsage() {
        //loop thru usage array and edit start time
        println("addActiveUsage()")

        print("usageToLog.count = ${usageToLog.count()}")
        var openUsage = false

        val todayDate = LocalDateTime.now()

        val formatterLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val formatterShort = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayShort = todayDate.format(formatterShort)

        println("Current Date short: $todayShort")



        for (usage in woItem!!.usage) {
            println("usage.start = ${usage.start}")

            println("usage.stop = ${usage.stop}")

            var formattedUsageDateString = ""

            if (usage.start != null) {


                val date = LocalDate.parse(usage.start!!, formatterLong)

                println("date from usage = $date")

                formattedUsageDateString = date.toString()
                println("usage date string = $formattedUsageDateString")

            }

            if(usage.stop == null || usage.stop == "0000-00-00 00:00:00" || todayShort == formattedUsageDateString){
                openUsage = true
                usageToLog.add(usage)



            }

        }

        println("usageToLog.count = ${usageToLog.count()}")
        println("openUsage = $openUsage")

        //add rows for emps on work order if there is no open usage rows
        if(woItem!!.type == "1"){
            //labor type

            if(!openUsage) {
                for (emp in workOrder.emps) {
                    print("empName = ${emp.name}")

                    val usage = Usage("0", workOrder.woID,woItem!!.ID,woItem!!.type,GlobalVars.loggedInEmployee!!.ID,"0.00")
                    usage.empID = emp.ID
                    usage.empName = emp.name
                    usage.pic = emp.pic
                    usage.depID = emp.dep
                    usage.unitPrice = woItem!!.price
                    usage.totalPrice = woItem!!.total
                    usage.usageCharge = woItem!!.charge
                    usage.override = "1"

                    usageToLog.add(0,usage)



                }
                updateUsageTable()
            }


        }else{
            //material type

            if (usageToLog.count() == 0){


                val usage = Usage("0", workOrder.woID,woItem!!.ID,woItem!!.type,GlobalVars.loggedInEmployee!!.ID,"0.00")
                usage.empID = null
                usage.empName = null
                usage.pic = null
                usage.depID = null
                usage.unitPrice = woItem!!.price
                usage.totalPrice = woItem!!.total
                usage.usageCharge = woItem!!.charge
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






        /*



        for usage in self.woItem.usages! {
            // print("usage.qty = \(String(describing: usage.qty))")
            print("usage.startString = \(String(describing: usage.startString))")
            print("usage.start = \(String(describing: usage.start))")

            print("usage.stopString = \(String(describing: usage.stopString))")
            print("usage.stop = \(String(describing: usage.stop))")

            let todaysDate = Date()
            let formatLong = DateFormatter()
            formatLong.dateFormat = "yyyy-MM-dd HH:mm:ss"
            let formatShort = DateFormatter()
            formatShort.dateFormat = "yyyy-MM-dd"
            let formattedTodaysDate = formatShort.string(from: todaysDate)
            print("todays date = \(formattedTodaysDate)")

            var formattedUsageDateString:String = ""

            if usage.startString != nil{
                //let formattedUsageDate = format.string(from: usage.startString!)
                let formattedUsageDate = formatLong.date(from: usage.startString!)

                print("usage date = \(String(describing: formattedUsageDate))")

                formattedUsageDateString = formatShort.string(from: formattedUsageDate!)
                print("usage date string = \(formattedUsageDateString)")
            }

            if(usage.stop == nil || formattedTodaysDate == formattedUsageDateString){
                openUsage = true
                usageToLog.append(usage)//append to your list
            }
        }

        print("usageToLog.count = \(usageToLog.count)")

        //add rows for emps on work order if there is no open usage rows
        if(woItem.type == "1"){
            print("openUsage = \(openUsage)")
            if(openUsage == false){
                for employee in self.empsOnWo {
                    print("empName = \(employee.name)")

                    let usage = Usage2(_ID: "0", _woID: self.workOrderID, _itemID: self.woItem.ID, _type: self.woItem.type!, _addedBy: appDelegate.loggedInEmployee!.ID,_qty: "")

                    usage.empID = employee.ID
                    usage.empName = employee.name
                    usage.pic = employee.pic
                    usage.depID = employee.depID
                    usage.unitPrice = self.woItem.price
                    usage.totalPrice = self.woItem.total
                    usage.chargeType = self.woItem.charge
                    usage.override = "1"
                    usage.locked = false

                    usage.start = nil
                    usage.stop = nil
                    usage.lunch = ""

                    usage.vendor = ""
                    usage.unitCost = ""
                    usage.totalCost = ""
                    usage.del = ""

                    usageToLog.insert(usage, at: 0)
                }

            }
        }else{
            if usageToLog.count == 0{

                let usage = Usage2(_ID: "0", _woID: self.workOrderID, _itemID: self.woItem.ID, _type: self.woItem.type!, _addedBy: appDelegate.loggedInEmployee!.ID,_qty: "")

                usage.empID = nil
                usage.empName = nil
                usage.pic = nil
                usage.depID = nil
                usage.unitPrice = self.woItem.price
                usage.totalPrice = self.woItem.total
                usage.chargeType = self.woItem.charge
                usage.override = "1"
                usage.locked = false

                usage.start = nil
                usage.stop = nil
                usage.lunch = ""

                usage.vendor = ""
                usage.unitCost = ""
                usage.totalCost = ""
                usage.del = ""

                usageToLog.insert(usage, at: 0)
            }

        }
        self.usageTableView.reloadData()
    }


*/




    }



    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")


        if (position != 0){
            addEmployee(GlobalVars.employeeList!![position - 1])
            empSpinner.setSelection(0,false)
        }

    }



    private fun addEmployee(emp:Employee){
        println("add employee with $emp")


        val usage = Usage("0", workOrder.woID,woItem!!.ID,woItem!!.type,GlobalVars.loggedInEmployee!!.ID,"0.00")
        usage.empID = emp.ID
        usage.empName = emp.name
        usage.pic = emp.pic
        usage.depID = emp.dep
        usage.unitPrice = woItem!!.price
        usage.totalPrice = woItem!!.total
        usage.usageCharge = woItem!!.charge
        usage.override = "1"

        usageToLog.add(0,usage)
        editsMade = true

        updateUsageTable()

    }

    private fun updateUsageTable(){
        println("updateUsageTable")

        usageRecyclerView.apply {
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


            (adapter as UsageAdapter).notifyDataSetChanged()
        }
    }






//methods for button taps
    private fun start(){
        println("start")

        for (usage in usageToLog){

            if(usage.stop != null && usage.stop != "0000-00-00 00:00:00"){
                println("date from stop string = ${globalVars.getLocalDateFromString(usage.stop!!)}")
                println("time from stop string = ${globalVars.getTimeFromString(usage.stop!!)}")
            }


            if((usage.locked == false  || usage.locked == null) && usage.start == null){

                //test if start is before stop or stop is nil
                if(usage.stop == null || usage.stop == "0000-00-00 00:00:00"){
                    println("usage.stop == null || usage.stop == \"0000-00-00 00:00:00\"")


                    usage.start = globalVars.getDBDateStringFromDate(Date())

                    println("usage.start = ${usage.start}")

                   // editsMade = true



                }else if(globalVars.getTimeFromString(usage.stop!!)!!  < Date().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()){
                    println("start is after stop")

                    //start is after stop
                    globalVars.simpleAlert(myView.context,"Start Time Error","${usage.empName!!}'s start time can not be later then their stop time.")
                    usage.start = null
                }else{
                    println("start before stop")

                    usage.start = Date().toString()
                   // editsMade = true
                }
            }
        }

        setQty()

    }


    private fun stop(){
        println("stop")
        for (usage in usageToLog){

            println("usage.locked = ${usage.locked}")
            if(usage.locked == false || usage.locked == null){
                //test if stop is after start or start is nil
                if(usage.start == null){
                    println("no start time")

                    //no start time
                    globalVars.playErrorSound(myView.context)
                    globalVars.simpleAlert(myView.context,"Stop Time Error","${usage.empName} has no start time.  Enter start time first.")

                }else if (LocalTime.now() <  globalVars.getTimeFromString(usage.start!!)){
                    println("stop is before start")

                    //stop is before start
                    globalVars.playErrorSound(myView.context)
                    globalVars.simpleAlert(myView.context,"Stop Time Error","${usage.empName}'s stop time can not be earlier then their start time.")

                }else{

                    println("else")
                    if(usage.stop == null || usage.stop == "0000-00-00 00:00:00"){
                        println("set stop")
                        usage.stop =  globalVars.getDBDateStringFromDate(Date())
                       // editsMade = true
                    }

                }
            }

        }

        setQty()
    }



//methods for text edits
    override fun editStart(row: Int) {

        val cal:Calendar
        val h:Int
        val m:Int
        if (usageToLog[row].start == null || usageToLog[row].start == "0000-00-00 00:00:00"){
            println("usage start == null")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }else{

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            cal = Calendar.getInstance()

            cal.time = sdf.parse(usageToLog[row].start!!)
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }


        timePicker = TimePickerHelper(myView.context, false, true)
        timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
            override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                println("hourOfDay = $hourOfDay")
                println("minute = $minute")

                val current = LocalDate.now()

                val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
                val minuteStr = if (minute < 10) "0${minute}" else "$minute"

                val startString = "$current $hourStr:$minuteStr:00"
                println("startString is  $startString")





                if(usageToLog[row].stop != null && usageToLog[row].stop != "0000-00-00 00:00:00"){
                    if(globalVars.getTimeFromString(usageToLog[row].stop!!)!!  < globalVars.getTimeFromString(startString)!!){
                        println("start is after stop")

                        //start is after stop
                        globalVars.playErrorSound(myView.context)
                        globalVars.simpleAlert(myView.context,"Start Time Error","${usageToLog[row].empName!!}'s start time can not be later then their stop time.")
                        usageToLog[row].start = null
                        updateUsageTable()
                        return
                    }
                }


                    usageToLog[row].start = startString
                   // editsMade = true


                //check if usageToLog is greater then 1
                //loop through usage
                //check if start is null




                //check to update other starts
                var locked = true
                if(usageToLog.count() > 1){
                    for (usage in usageToLog){
                        if(usage.locked == false){
                            locked = false
                        }
                    }
                    if (!locked){

                        val builder = AlertDialog.Builder(myView.context)
                        builder.setTitle("Update Everyone's Start Time?")
                        builder.setPositiveButton("YES") { _, _ ->
                            //Toast.makeText(context,
                            // android.R.string.yes, Toast.LENGTH_SHORT).show()
                            editOthersStart(row)
                        }
                        builder.setNegativeButton("NO") { _, _ ->
                            //Toast.makeText(context,
                            // android.R.string.yes, Toast.LENGTH_SHORT).show()
                            setQty()

                        }
                        builder.show()
                    }else{
                        setQty()
                    }
                }else{
                    setQty()
                }





            }
        })
    }


    override fun editStop(row: Int) {

        val cal:Calendar
        val h:Int
        val m:Int
        if (usageToLog[row].stop == null || usageToLog[row].stop == "0000-00-00 00:00:00"){
            println("usage start == null")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }else{

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            cal = Calendar.getInstance()

            cal.time = sdf.parse(usageToLog[row].stop!!)
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }


        timePicker = TimePickerHelper(myView.context, false, true)
        timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
            override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                println("hourOfDay = $hourOfDay")
                println("minute = $minute")

                val current = LocalDate.now()

                val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
                val minuteStr = if (minute < 10) "0${minute}" else "$minute"

                val stopString = "$current $hourStr:$minuteStr:00"
                println("stopString is  $stopString")


                if (usageToLog[row].start == null){
                    globalVars.playErrorSound(myView.context)
                    globalVars.simpleAlert(myView.context, "Start Time Error","${usageToLog[row].empName} has no start time.")
                    return
                    }

                println("got past return")



                if (globalVars.getTimeFromString(stopString)!! <  globalVars.getTimeFromString(usageToLog[row].start!!)){
                    println("stop is before start")

                    //stop is before start
                    globalVars.playErrorSound(myView.context)
                    globalVars.simpleAlert(myView.context,"Stop Time Error","${usageToLog[row].empName}'s stop time can not be earlier then their start time.")
                    usageToLog[row].stop = null

                }else{

                    usageToLog[row].stop = stopString
                 //   editsMade = true
                }


                //check to update other stops
                var locked = true
                if(usageToLog.count() > 1){
                    for (usage in usageToLog){
                        if(usage.locked == false){
                            locked = false
                        }
                    }
                    if (!locked){

                        val builder = AlertDialog.Builder(myView.context)
                        builder.setTitle("Update Everyone's Stop Time?")
                        builder.setPositiveButton("YES") { _, _ ->
                            //Toast.makeText(context,
                            // android.R.string.yes, Toast.LENGTH_SHORT).show()
                            editOthersStop(row)
                        }
                        builder.setNegativeButton("NO") { _, _ ->
                            //Toast.makeText(context,
                            // android.R.string.yes, Toast.LENGTH_SHORT).show()
                            setQty()

                        }
                        builder.show()
                    }else{
                        setQty()
                    }
                }else{
                    setQty()
                }



              //  setQty()

            }
        })
    }




    override fun editBreak(row: Int, lunch: String, actionID:Int) {

        try {
            if (isResumed) {
                val num = java.lang.Double.parseDouble(lunch)
            }
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

            println("currentPayroll.startTime = ${ usageToLog[row].start}")
            //validate break time
            if (usageToLog[row].start == null || usageToLog[row].start == "0000-00-00 00:00:00" || usageToLog[row].stop == null || usageToLog[row].stop == "0000-00-00 00:00:00")
            {
                // Toast.makeText(myView.context,"Add Start Time First",Toast.LENGTH_LONG).show()

                globalVars.playErrorSound(myView.context)
                globalVars.simpleAlert(myView.context,"Break Time Error","Add start and stop time before break time.")


                //breakTxt.setText("0")
                usageToLog[row].lunch = null
                updateUsageTable()
                return
            }


            usageToLog[row].lunch = lunch

           // setQty()


            //check to update other breaks
            var locked = true
            if(usageToLog.count() > 1){
                for (usage in usageToLog){
                    if(usage.locked == false){
                        locked = false
                    }
                }
                if (!locked){

                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle("Update Everyone's Break Time?")
                    builder.setPositiveButton("YES") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()
                        editOthersBreak(row)
                    }
                    builder.setNegativeButton("NO") { _, _ ->
                        //Toast.makeText(context,
                        // android.R.string.yes, Toast.LENGTH_SHORT).show()
                        setQty()
                    }
                    builder.show()
                }else{
                    setQty()
                }
            }else{
                setQty()
            }



            /*
            println("currentPayroll.total = ${currentPayroll.total}")
            // make sure there is a stop time
            if (currentPayroll.total  != null){

                val totalMins = currentPayroll!!.total!!.toFloat() * 60

                if (breakTxt.text.toString().toFloat() >= totalMins){

                    globalVars.simpleAlert(myView.context,"Break Time Error","Break time can not be equal or greater then total shift time.")

                    // Toast.makeText(myView.context,"Break time can not be equal or greater then total shift time.",Toast.LENGTH_LONG).show()
                    breakTxt.setText("0")
                    return false
                }

            }

            breakTxt.hideKeyboard()

            currentPayroll.lunch = breakTxt.text.toString()
            submitPayroll()
            return true
            */



        }
    }


    fun editOthersStart(row:Int){

        println("editOthersStart with row $row  ${usageToLog[row].start}")
        for (usage in usageToLog){
            println("usage locked = ${usage.locked}")
            if (usage.locked == false){
                usage.start = usageToLog[row].start
            }
        }

        setQty()


    }

    fun editOthersStop(row:Int){

        println("editOthersStop")
        for (usage in usageToLog){
            if (usage.locked == false){
                usage.stop = usageToLog[row].stop
            }
        }

        setQty()


    }

    private fun editOthersBreak(row:Int){

        println("editOthersBreak")
        for (usage in usageToLog){
            if (usage.locked == false){
                usage.lunch = usageToLog[row].lunch
            }
        }

        setQty()

    }










    fun setQty(){
        println("setQty")

        for (usage in usageToLog) {
            if(usage.start == null && usage.stop == null){
                println("blank row")
            }else{

                var qtySeconds: Long

                if(usage.start == null || usage.start == "0000-00-00 00:00:00" || usage.stop == null || usage.stop == "0000-00-00 00:00:00"){
                    println("start or stop is null")
                    usage.qty = "0.00"
                    //updateUsageTable()

                }else{

                    val startTime = globalVars.getTimeFromString(usage.start!!)
                    val stopTime = globalVars.getTimeFromString(usage.stop!!)


                    if(startTime != null && stopTime != null){
                        qtySeconds = startTime.until(stopTime, ChronoUnit.SECONDS)
                      usage.qty =   startTime.until(stopTime, ChronoUnit.HOURS).toString()

                        var breakTime = 0.0
                        if(usage.lunch != null && usage.lunch != "" && usage.lunch != "0"){
                            breakTime = usage.lunch!!.toDouble() * 60
                            if(breakTime >= qtySeconds){
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


                    }else{
                        usage.qty = "0.00"
                    }


                }

            }

        }

        editsMade = true

        updateUsageTable()

    }




    override fun deleteUsage(row: Int) {
        println("updateUsageToLog row = $row")

        editsMade = false //resets edit checker

        if (usageToLog[row].ID == "0"){
            usageToLog.removeAt(row)
            updateUsageTable()
        }else{
            usageToLog[row].del = "1"
            submitUsage()

            usageToLog.removeAt(row)
            updateUsageTable()
        }





    }

    private fun submitUsage(){
        println("submitUsage")


        usageToLogJSONMutableList = mutableListOf()


        //loop thru usage array and build JSON array
        editsMade = false //resets edit checker

        for ((i, usage) in usageToLog.withIndex()) {
            var usageQty = 0.0
            println("usage.qty = ${usage.qty}")
            if(usage.qty != "0.0" && usage.qty != ""){
                println("set usage.qty to 0.0")
                usageQty = usage.qty.toDouble()
            }
            usage.usageCharge = woItem!!.charge
            //usage.del = "0"

            println("usageQty = $usageQty")

            if(usage.locked == false){
                if(usage.type == "1"){
                    //labor
                    if(usage.start == null && usage.del != "1"){
                        globalVars.playErrorSound(myView.context)
                        globalVars.simpleAlert(myView.context, "Start Time Error","${usage.empName} has no start time.")
                        return
                    }else{

                        if(usageQty > 0.0){
                            if( GlobalVars.loggedInEmployee!!.ID != usage.addedBy){

                                usage.locked = true

                            }
                        }


                    }

                }else{
                    //material
                    if (usage.vendor == null || usage.vendor == "") {
                        //globalVars.simpleAlert(myView.context, "Error","No vendor selected.")
                        //return
                        usage.vendor = "0"
                    }

                }



            }




           val jsonUsagePretty: String = gsonPretty.toJson(usageToLog[i])
            println("jsonUsagePretty = $jsonUsagePretty")



            usageToLogJSONMutableList.add(jsonUsagePretty)


        }

        callDB(usageToLogJSONMutableList.toString())



    }

    private fun callDB(json:String){
        println("callDB w json: $json")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/update/usage.php"



        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        globalVars.playSaveSound(myView.context)

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
                params["usageToLog"] = json

                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)


    }


     fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        empSpinner.visibility = View.INVISIBLE
        usageRecyclerView.visibility = View.INVISIBLE
        startStopCl.visibility = View.INVISIBLE
        submitBtn.visibility = View.INVISIBLE

    }

     fun hideProgressView() {
        println("hideProgressView")
        pgsBar.visibility = View.INVISIBLE
        empSpinner.visibility = View.VISIBLE
        usageRecyclerView.visibility = View.VISIBLE
        startStopCl.visibility = View.VISIBLE
        submitBtn.visibility = View.VISIBLE
    }








    override fun editQty(row: Int, qtyDouble: Double, actionID:Int) {

        if (actionID == EditorInfo.IME_ACTION_DONE) {

            if (usageToLog[row].unitCost == null || usageToLog[row].unitCost == "") {
                usageToLog[row].unitCost = "0.00"
            }

            usageToLog[row].qty = String.format("%.2f", qtyDouble)
            val totalCost = (usageToLog[row].unitCost!!.toDouble() * usageToLog[row].qty.toDouble())
            usageToLog[row].totalCost = String.format("%.2f", totalCost)
            editsMade = true
            updateUsageTable()
        }
    }

    override fun editCost(row: Int, costDouble: Double, actionID:Int, updateUsageTable:Boolean) {

        if (actionID == EditorInfo.IME_ACTION_DONE) {

            if (usageToLog[row].qty == "") {
                usageToLog[row].qty = "0"
            }

            usageToLog[row].unitCost = String.format("%.2f", costDouble)
            val totalCost = (usageToLog[row].qty.toDouble() * usageToLog[row].unitCost!!.toDouble())
            usageToLog[row].totalCost = String.format("%.2f", totalCost)
            editsMade = true
            if (updateUsageTable) {
                updateUsageTable()
            }

        }
    }

    override fun editVendor(row: Int, vendor: String) {
        usageToLog[row].vendor = vendor

        editsMade = true

        //This isn't needed because the spinner display updates itself, also calling it here creates an infinite loop?
        //updateUsageTable()
    }

    override fun showHistory() {
        TODO("Not yet implemented")
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UsageEntryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UsageEntryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}