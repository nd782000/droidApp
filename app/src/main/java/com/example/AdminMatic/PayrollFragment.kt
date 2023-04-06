package com.example.AdminMatic

//import androidx.test.core.app.ApplicationProvider.getApplicationContext

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentPayrollBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.employeeList
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*



class PayrollFragment : Fragment(),AdapterView.OnItemSelectedListener{

    lateinit  var globalVars:GlobalVars
    private  var employee: Employee? = null
    lateinit var currentPayroll:Payroll

    lateinit var myView:View

    private lateinit var timePicker: TimePickerHelper

    var firstLoad = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    private var _binding: FragmentPayrollBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentPayrollBinding.inflate(inflater, container, false)
        myView = binding.root

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Payroll View")

        println("employee = ${employee!!.name}")


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.enter_payroll)

        binding.startBtn.setOnClickListener {
            if (GlobalVars.permissions!!.payrollApp == "1") {
                start()
            }
            else {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context,getString(R.string.access_denied),getString(R.string.no_permission_payroll))
            }
        }
        binding.stopBtn.setOnClickListener {
            if (GlobalVars.permissions!!.payrollApp == "1") {
                stop()
            }
            else {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context,getString(R.string.access_denied),getString(R.string.no_permission_payroll))
            }
        }
        binding.resetBtn.setOnClickListener {
            if (GlobalVars.permissions!!.payrollApp == "1") {
                val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                builder.setTitle(getString(R.string.dialogue_reset_payroll_title))
                builder.setMessage(getString(R.string.dialogue_reset_payroll_body))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    reset()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ ->

                }
                builder.show()
            }
            else {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context,getString(R.string.access_denied),getString(R.string.no_permission_payroll))
            }
        }

        binding.startEditTxt.setOnClickListener {
            if (GlobalVars.permissions!!.payrollApp == "1") {
                editStart()
            }
            else {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context,getString(R.string.access_denied),getString(R.string.no_permission_payroll))
            }
        }
        binding.stopEditTxt.setOnClickListener {
            if (GlobalVars.permissions!!.payrollApp == "1") {
                editStop()
            }
            else {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context,getString(R.string.access_denied),getString(R.string.no_permission_payroll))
            }
        }

        if (GlobalVars.permissions!!.payrollApp == "1") {
            binding.breakEditTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            binding.breakEditTxt.setSelectAllOnFocus(true)
            binding.breakEditTxt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override  fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {

                    //var numeric = true

                    try {
                        if (isResumed) {
                            val num = parseDouble(binding.breakEditTxt.text.toString())
                        }
                    } catch (e: NumberFormatException) {
                        // numeric = false

                        globalVars.simpleAlert(myView.context,"Break Time Error","Break Time must be a Number of Minutes")
                        //Toast.makeText(myView.context, "Break Time must be a Number of Minutes", Toast.LENGTH_LONG).show()

                        binding.breakEditTxt.setText("0")
                        return false
                    }





                    if (actionId == EditorInfo.IME_ACTION_DONE) {



                        println("currentPayroll.startTime = ${currentPayroll.startTime}")
                        //validate break time
                        if (currentPayroll.startTime == null)
                        {
                            // Toast.makeText(myView.context,"Add Start Time First",Toast.LENGTH_LONG).show()

                            globalVars.simpleAlert(myView.context,"Break Time Error","Add start time before break time.")


                            binding.breakEditTxt.setText("0")
                            return false
                        }


                        println("currentPayroll.total = ${currentPayroll.total}")
                        // make sure there is a stop time
                        if (currentPayroll.total  != null){

                            val totalMins = currentPayroll.total!!.toFloat() * 60

                            if (binding.breakEditTxt.text.toString().toFloat() >= totalMins){

                                globalVars.simpleAlert(myView.context,"Break Time Error","Break time can not be equal or greater then total shift time.")

                                // Toast.makeText(myView.context,"Break time can not be equal or greater then total shift time.",Toast.LENGTH_LONG).show()
                                binding.breakEditTxt.setText("0")
                                return false
                            }

                        }

                        binding.breakEditTxt.hideKeyboard()

                        currentPayroll.lunch = binding.breakEditTxt.text.toString()
                        submitPayroll()
                        return true
                    }
                    return false
                }
            })
        }
        else {
            binding.breakEditTxt.isEnabled = false
            binding.breakEditTxt.isFocusable = false
        }





        (activity as AppCompatActivity).supportActionBar?.title = "Payroll Entry"

        val adapter: ArrayAdapter<Employee> = ArrayAdapter<Employee>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, employeeList!!

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        binding.empSpinner.adapter = adapter


        var i = 0
        for (emp in employeeList!!) {


            println(emp.name) // or your logic to catch the "B"
            if (emp.ID == employee!!.ID){
                println("set selected with ${emp.name} position $i")
                binding.empSpinner.setSelection(i, false)
            }
            i += 1

        }

        binding.empSpinner.onItemSelectedListener = this@PayrollFragment

        getPayroll()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.payroll_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.payroll_summary_item) {
            val directions = PayrollFragmentDirections.navigateToPayrollSummary(employee)
            myView.findNavController().navigate(directions)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("payroll")
    }

    private fun getPayroll(){
        println("getPayroll")

        if (!firstLoad){
            showProgressView()
        }


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/payroll.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val payrollJSON: JSONArray = parentObject.getJSONArray("payroll")
                    println("payroll = $payrollJSON")
                    println("payroll count = ${payrollJSON.length()}")



                    val gson = GsonBuilder().create()
                    val payrollList = gson.fromJson(payrollJSON.toString() , Array<Payroll>::class.java).toMutableList()

                     currentPayroll = payrollList[payrollList.count() - 1]

                    if (currentPayroll.startTimeShort != null && currentPayroll.startTimeShort != "No Time" && currentPayroll.startTimeShort != ""){
                        println("currentPayroll.startTimeShort = ${currentPayroll.startTimeShort!!}")


                        binding.startEditTxt.setText(currentPayroll.startTimeShort!!)
                    }else{
                        binding.startEditTxt.setText(getString(R.string.no_time))
                    }

                    if (currentPayroll.stopTimeShort != null && currentPayroll.stopTimeShort != "No Time" && currentPayroll.stopTimeShort != "") {
                        binding.stopEditTxt.setText(currentPayroll.stopTimeShort!!)
                    }else{
                        binding.stopEditTxt.setText(getString(R.string.no_time))
                    }
                    if (currentPayroll.lunch != null) {
                        binding.breakEditTxt.setText(currentPayroll.lunch!!)
                    }else{
                        binding.breakEditTxt.setText("0")
                    }

                    if (currentPayroll.total != null) {
                        binding.totalValTv.text = currentPayroll.total!!
                    }else{
                        binding.totalValTv.text = getString(R.string.zero_hours)
                    }
                    val combinedTotal: String? = parentObject.getString("combinedTotal")
                    println("payroll = $combinedTotal")
                    if (combinedTotal != null) {
                        binding.combinedTotalValTv.text = combinedTotal
                    }else{
                        binding.combinedTotalValTv.text = getString(R.string.zero_hours)
                    }

                    //var pendingString: String =
                   // println("pendingString = ${parentObject.getString("verified")}")
                    if (currentPayroll.verified !="0") {
                        //pendingTxt.text = "Pending Shift"
                        binding.totalPendingTv.visibility = View.GONE

                        //combinedPendingTxt.text = "Excludes Pending"
                        binding.combinedTotalPendingTv.visibility = View.GONE
                    }else{
                        binding.totalPendingTv.visibility = View.VISIBLE
                        binding.combinedTotalPendingTv.visibility = View.VISIBLE
                    }



                    firstLoad = true

                    println("currentPayroll.appCreatedBy = ${currentPayroll.appCreatedBy}")

                    if (currentPayroll.appCreatedBy == GlobalVars.loggedInEmployee!!.ID || currentPayroll.appCreatedBy == "0" && currentPayroll.createdBy == "0" || currentPayroll.startTime == "No Time"){
                        unlockInputs()
                    }else{
                        lockInputs()
                    }

                    hideProgressView()



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
                params["empID"] = employee!!.ID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "payroll"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun start(){
        println("start")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = sdf.format(Date())
        println(currentDate)

        currentPayroll.startTime = currentDate
        submitPayroll()

    }
    private fun stop(){
        println("stop")


        println("currentPayroll.startTime = ${currentPayroll.startTime}")
        //validate break time
        if (currentPayroll.startTime == null)
        {
            //layoutVars.simpleAlert(_vc: self, _title: "Add Start Time First", _message: "")
           // Toast.makeText(myView.context,"Add Start Time First",Toast.LENGTH_LONG).show()

            globalVars.simpleAlert(myView.context,"Stop Time Error","Add start time before adding stop time.")


            binding.stopEditTxt.setText(getString(R.string.no_time))

        }else{
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDate = sdf.format(Date())
            println(currentDate)

            currentPayroll.stopTime = currentDate
            submitPayroll()
        }




    }
    private fun reset(){
        println("reset")


        if (currentPayroll.ID != "0"){
            currentPayroll.startTime = ""
            currentPayroll.stopTime = ""
            currentPayroll.lunch = "0"
            binding.startEditTxt.setText("")
            binding.stopEditTxt.setText("")
            binding.breakEditTxt.setText("0")

            submitPayroll("1")
        }else{
            Toast.makeText(myView.context,"Nothing to Reset",Toast.LENGTH_LONG).show()



        }

    }

    private fun editStart(){
        println("editStart")


        val cal:Calendar
        val h:Int
        val m:Int
        if (currentPayroll.startTime == null || currentPayroll.startTime == "No Time"){
            println("currentPayroll.startTime == null || currentPayroll.startTime == No Time")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }else{


            println("cal.time = formatter.parse(currentPayroll!!.startTime!!) as Date")

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)


            cal = Calendar.getInstance()

            cal.time = sdf.parse(currentPayroll.startTime!!) as Date
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }


        timePicker = TimePickerHelper(myView.context, false, true)
        timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
            override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                println("hourOfDay = $hourOfDay")
                println("minute = $minute")

               // val sdf = SimpleDateFormat("yyyy-MM-DD")
                //val currentDate = sdf.format(Date())
                // println(" C DATE is  "+currentDate)

                val current = LocalDate.now()

               // val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
              //  val formatted = current.format(formatter)

                println("Current Date and Time is: $current")


                val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
                val minuteStr = if (minute < 10) "0${minute}" else "$minute"

                val startString = "$current $hourStr:$minuteStr:00"
                println("startString is  $startString")

                currentPayroll.startTime = startString

                submitPayroll()

            }
        })
    }

    private fun editStop(){
        println("editStop")

        if (currentPayroll.startTime == null)
        {
            //layoutVars.simpleAlert(_vc: self, _title: "Add Start Time First", _message: "")
           // Toast.makeText(myView.context,"Add Start Time First",Toast.LENGTH_LONG).show()

            globalVars.simpleAlert(myView.context,"Stop Time Error","Add start time before adding stop time.")

            binding.stopEditTxt.setText(getString(R.string.no_time))

        }else {

            val cal: Calendar
            val h: Int
            val m: Int
            if (currentPayroll.stopTime == null || currentPayroll.stopTime == "No Time") {
                println("currentPayroll.stopTime == null || currentPayroll.stopTime == No Time")
                cal = Calendar.getInstance()
                h = cal.get(Calendar.HOUR_OF_DAY)
                m = cal.get(Calendar.MINUTE)
            } else {

                println("cal.time = formatter.parse(currentPayroll!!.stopTime!!) as Date")
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                cal = Calendar.getInstance()

                cal.time = sdf.parse(currentPayroll.stopTime!!) as Date
                h = cal.get(Calendar.HOUR_OF_DAY)
                m = cal.get(Calendar.MINUTE)
            }

            println("h = $h")
            println("m = $m")
            timePicker = TimePickerHelper(myView.context, false, true)
            timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
                override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                    println("hourOfDay = $hourOfDay")
                    println("minute = $minute")


                   // val sdf = SimpleDateFormat("yyyy-MM-DD")
                   // val currentDate = sdf.format(Date())
                   // println(" C DATE is  " + currentDate)

                    val current = LocalDate.now()

                    // val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    //  val formatted = current.format(formatter)

                   // println("Current Date and Time is: $current")



                    val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
                    val minuteStr = if (minute < 10) "0${minute}" else "$minute"


                    val stopString = "$current $hourStr:$minuteStr:00"

                    println("stopString is  $stopString")

                    currentPayroll.stopTime = stopString

                    submitPayroll()

                }
            })
        }
    }




    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")

        employee = employeeList!![position]


        getPayroll()

    }


    private fun submitPayroll(del:String = "0"){
        println("submitPayroll")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = sdf.format(Date())


        var start = ""
        if (currentPayroll.startTime != null && currentPayroll.startTime != "No Time"){
            start  = currentPayroll.startTime.toString()
        }

        var stop = ""
        if (currentPayroll.stopTime != null && currentPayroll.stopTime != "No Time"){
            stop  = currentPayroll.stopTime.toString()
        }

        var lunch = "0"
        if (currentPayroll.lunch != null){
            lunch  = currentPayroll.lunch!!
        }

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/payroll.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                //hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)
                    val payrollJSON: JSONArray = parentObject.getJSONArray("payroll")
                    println("payroll = $payrollJSON")
                    println("payroll count = ${payrollJSON.length()}")


                    getPayroll()


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
                params["empID"] = employee!!.ID
                params["ID"] = currentPayroll.ID!!
                params["appCreatedBy"] = GlobalVars.loggedInEmployee!!.ID
                params["startTime"] = start
                params["stopTime"] = stop
                params["lunch"] = lunch
                params["date"] = currentDate
                params["del"] = del

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "payroll"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun lockInputs(){


        binding.startBtn.isEnabled = false
        binding.startBtn.isClickable = false
        binding.startEditTxt.isEnabled = false
        binding.startEditTxt.isClickable = false

        binding.stopBtn.isEnabled = false
        binding.stopBtn.isClickable = false
        binding.stopEditTxt.isEnabled = false
        binding.stopEditTxt.isClickable = false

        binding.breakEditTxt.isEnabled = false
        binding.breakEditTxt.isClickable = false

        binding.resetBtn.isEnabled = false
        binding.resetBtn.isClickable = false



        binding.startLockCl.visibility = View.VISIBLE
        binding.stopLockCl.visibility = View.VISIBLE
        binding.breakLockCl.visibility = View.VISIBLE
    }

    fun unlockInputs(){


        binding.startBtn.isEnabled = true
        binding.startBtn.isClickable = true
        binding.startEditTxt.isEnabled = true
        binding.startEditTxt.isClickable = true

        binding.stopBtn.isEnabled = true
        binding.stopBtn.isClickable = true
        binding.stopEditTxt.isEnabled = true
        binding.stopEditTxt.isClickable = true

        binding.breakEditTxt.isEnabled = true
        binding.breakEditTxt.isClickable = true

        binding.resetBtn.isEnabled = true
        binding.resetBtn.isClickable = true


        binding.startLockCl.visibility = View.GONE
        binding.stopLockCl.visibility = View.GONE
        binding.breakLockCl.visibility = View.GONE
    }


    fun showProgressView() {
        println("showProgressView")
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}