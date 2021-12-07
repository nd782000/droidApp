package com.example.AdminMatic

//import androidx.test.core.app.ApplicationProvider.getApplicationContext

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.opengl.Visibility
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.employeeList
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PayrollFragment.newInstance] factory method to
 * create an instance of this fragment.
 */





class PayrollFragment : Fragment(),AdapterView.OnItemSelectedListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit  var globalVars:GlobalVars
    private  var employee: Employee? = null
    lateinit var currentPayroll:Payroll

    lateinit var myView:View
    lateinit var  pgsBar: ProgressBar
    lateinit var  empPickerCL: ConstraintLayout
    lateinit var  startCL: ConstraintLayout
    lateinit var  stopCL: ConstraintLayout
    lateinit var  breakCL: ConstraintLayout
    lateinit var  resetCL: ConstraintLayout
    lateinit var  footerCL: ConstraintLayout

    lateinit var empSpinner:Spinner
    lateinit var  startBtn: Button
    lateinit var  stopBtn: Button
    lateinit var  resetBtn: Button

    lateinit var  startTxt: EditText
    lateinit var  stopTxt: EditText
    lateinit var  breakTxt: EditText

    lateinit var startLock:ConstraintLayout
    lateinit var stopLock:ConstraintLayout
    lateinit var breakLock:ConstraintLayout

    lateinit var  totalTxt: TextView
    lateinit var  combinedTotalTxt: TextView

    lateinit var  pendingTxt: TextView
    lateinit var  combinedPendingTxt: TextView

    lateinit var timePicker: TimePickerHelper

    var firstLoad = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable<Employee?>("employee")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_payroll, container, false)


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Payroll View")

        println("employee = ${employee!!.name}")


        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Enter Payroll"


        pgsBar = myView.findViewById(R.id.progressBar)
        empPickerCL = myView.findViewById(R.id.picker_cl)
        startCL = myView.findViewById(R.id.start_cl)
        stopCL = myView.findViewById(R.id.stop_cl)
        breakCL = myView.findViewById(R.id.break_cl)
        resetCL = myView.findViewById(R.id.reset_cl)
        footerCL = myView.findViewById(R.id.footer_cl)


        empSpinner = myView.findViewById(R.id.emp_spinner)
        empSpinner.setBackgroundResource(R.drawable.text_view_layout)

        startBtn = myView.findViewById(R.id.start_btn)
        startBtn.setOnClickListener({
            start()
        })
        stopBtn = myView.findViewById(R.id.stop_btn)
        stopBtn.setOnClickListener({
            stop()
        })
        resetBtn = myView.findViewById(R.id.reset_btn)
        resetBtn.setOnClickListener({
            reset()
        })


        startTxt = myView.findViewById(R.id.start_edit_txt)
        startTxt.setOnClickListener({
            editStart()
        })
        startTxt.setBackgroundResource(R.drawable.text_view_layout)
        stopTxt = myView.findViewById(R.id.stop_edit_txt)
        //stopTxt.ed
        stopTxt.setOnClickListener({
            editStop()
        })
        stopTxt.setBackgroundResource(R.drawable.text_view_layout)


        breakTxt = myView.findViewById(R.id.break_edit_txt)
        breakTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
        breakTxt.setSelectAllOnFocus(true);
        breakTxt.setBackgroundResource(R.drawable.text_view_layout)

        breakTxt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
          override  fun onEditorAction(
                v: TextView?,
                actionId: Int,
                event: KeyEvent?
            ): Boolean {

              //var numeric = true

              try {
                  val num = parseDouble(breakTxt.text.toString())
              } catch (e: NumberFormatException) {
                 // numeric = false

                  globalVars.simpleAlert(myView.context,"Break Time Error","Break Time must be a Number of Minutes")
                  //Toast.makeText(myView.context, "Break Time must be a Number of Minutes", Toast.LENGTH_LONG).show()

                  breakTxt.setText("0")
                  return false
              }





                if (actionId == EditorInfo.IME_ACTION_DONE) {



                    println("currentPayroll.startTime = ${currentPayroll.startTime}")
                    //validate break time
                    if (currentPayroll.startTime == null)
                    {
                        // Toast.makeText(myView.context,"Add Start Time First",Toast.LENGTH_LONG).show()

                        globalVars.simpleAlert(myView.context,"Break Time Error","Add start time before break time.")


                        breakTxt.setText("0")
                        return false
                    }


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
                }
                return false
            }
        })

        startLock = myView.findViewById(R.id.start_lock_cl)
        stopLock = myView.findViewById(R.id.stop_lock_cl)
        breakLock = myView.findViewById(R.id.break_lock_cl)

        totalTxt = myView.findViewById(R.id.total_val_tv)
        combinedTotalTxt = myView.findViewById(R.id.combined_total_val_tv)

        pendingTxt = myView.findViewById(R.id.total_pending_tv)
        combinedPendingTxt = myView.findViewById(R.id.combined_total_pending_tv)


        (activity as AppCompatActivity).supportActionBar?.title = "Payroll Entry"

        val adapter: ArrayAdapter<Employee>? = ArrayAdapter<Employee>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, employeeList!!

        )
        adapter!!.setDropDownViewResource(R.layout.spinner_right_aligned)

        empSpinner.adapter = adapter


        var i = 0
        for (emp in employeeList!!) {


            println(emp.name) // or your logic to catch the "B"
            if (emp.ID == employee!!.ID){

                println("set selected with ${emp.name} position $i")
                empSpinner.setSelection(i, false)
            }
            i += 1

        }

        empSpinner.onItemSelectedListener = this@PayrollFragment


        getPayroll()


    }

    private fun getPayroll(){
        println("getPayroll")

        if (firstLoad == false){
            showProgressView()
        }


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/payroll.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")




                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var payrollJSON: JSONArray = parentObject.getJSONArray("payroll")
                    println("payroll = ${payrollJSON.toString()}")
                    println("payroll count = ${payrollJSON.length()}")



                    val gson = GsonBuilder().create()
                    var payrollList = gson.fromJson(payrollJSON.toString() , Array<Payroll>::class.java).toMutableList()

                     currentPayroll = payrollList[payrollList.count() - 1]

                    if (currentPayroll.startTimeShort != null && currentPayroll.startTimeShort != "No Time" && currentPayroll.startTimeShort != ""){
                        println("currentPayroll.startTimeShort = ${currentPayroll.startTimeShort!!}")


                       startTxt.setText(currentPayroll!!.startTimeShort!!)
                    }else{
                        startTxt.setText("No Time")
                    }

                    if (currentPayroll.stopTimeShort != null && currentPayroll.stopTimeShort != "No Time" && currentPayroll.stopTimeShort != "") {
                        stopTxt.setText(currentPayroll.stopTimeShort!!)
                    }else{
                        stopTxt.setText("No Time")
                    }
                    if (currentPayroll.lunch != null) {
                        breakTxt.setText(currentPayroll.lunch!!)
                    }else{
                        breakTxt.setText("0")
                    }

                    if (currentPayroll.total != null) {
                        totalTxt.text = currentPayroll.total!!
                    }else{
                        totalTxt.text = "0.00"
                    }
                    var combinedTotal: String? = parentObject.getString("combinedTotal")
                    println("payroll = $combinedTotal")
                    if (combinedTotal != null) {
                        combinedTotalTxt.text = combinedTotal!!
                    }else{
                        combinedTotalTxt.text = "0.00"
                    }

                    //var pendingString: String =
                   // println("pendingString = ${parentObject.getString("verified")}")
                    if (currentPayroll.verified !="0") {
                        //pendingTxt.text = "Pending Shift"
                        pendingTxt.visibility = View.GONE

                        //combinedPendingTxt.text = "Excludes Pending"
                        combinedPendingTxt.visibility = View.GONE
                    }else{
                        pendingTxt.visibility = View.VISIBLE
                        combinedPendingTxt.visibility = View.VISIBLE
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

                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)
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


            stopTxt.setText("No Time")

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
            startTxt.setText("")
            stopTxt.setText("")
            breakTxt.setText("0")

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
        if (currentPayroll == null){
            println("currentPayroll == null")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }else if (currentPayroll.startTime == null || currentPayroll.startTime == "No Time"){
            println("currentPayroll.startTime == null || currentPayroll.startTime == No Time")
            cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
        }else{


            println("cal.time = formatter.parse(currentPayroll!!.startTime!!) as Date")

            val sdf:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)


            cal = Calendar.getInstance()

            cal.time = sdf.parse(currentPayroll!!.startTime!!)
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


                val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "${hourOfDay}"
                val minuteStr = if (minute < 10) "0${minute}" else "${minute}"

                val startString = "$current $hourStr:$minuteStr:00"
                println("startString is  "+startString)

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

            stopTxt.setText("No Time")

        }else {

            val cal: Calendar
            val h: Int
            val m: Int
            if (currentPayroll == null) {
                println("currentPayroll == null")
                cal = Calendar.getInstance()
                h = cal.get(Calendar.HOUR_OF_DAY)
                m = cal.get(Calendar.MINUTE)
            } else if (currentPayroll.stopTime == null || currentPayroll.stopTime == "No Time") {
                println("currentPayroll.stopTime == null || currentPayroll.stopTime == No Time")
                cal = Calendar.getInstance()
                h = cal.get(Calendar.HOUR_OF_DAY)
                m = cal.get(Calendar.MINUTE)
            } else {

                println("cal.time = formatter.parse(currentPayroll!!.stopTime!!) as Date")
                val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                cal = Calendar.getInstance()

                cal.time = sdf.parse(currentPayroll!!.stopTime!!)
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



                    val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "${hourOfDay}"
                    val minuteStr = if (minute < 10) "0${minute}" else "${minute}"


                    val stopString = "$current $hourStr:$minuteStr:00"

                    println("stopString is  " + stopString)

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


        var start:String = ""
        if (currentPayroll.startTime != null && currentPayroll.startTime != "No Time"){
            start  = currentPayroll.startTime.toString()
        }

        var stop:String = ""
        if (currentPayroll.stopTime != null && currentPayroll.stopTime != "No Time"){
            stop  = currentPayroll.stopTime.toString()
        }

        var lunch:String = "0"
        if (currentPayroll.lunch != null){
            lunch  = currentPayroll.lunch!!
        }

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/update/payroll.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                //hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var payrollJSON: JSONArray = parentObject.getJSONArray("payroll")
                    println("payroll = ${payrollJSON.toString()}")
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

                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)
    }

    fun lockInputs(){


        startBtn.isEnabled = false
        startBtn.isClickable = false
        startTxt.isEnabled = false
        startTxt.isClickable = false

        stopBtn.isEnabled = false
        stopBtn.isClickable = false
        stopTxt.isEnabled = false
        stopTxt.isClickable = false

        breakTxt.isEnabled = false
        breakTxt.isClickable = false

        resetBtn.isEnabled = false
        resetBtn.isClickable = false



        startLock.visibility = View.VISIBLE
        stopLock.setVisibility(View.VISIBLE)
        breakLock.setVisibility(View.VISIBLE)
    }

    fun unlockInputs(){


        startBtn.isEnabled = true
        startBtn.isClickable = true
        startTxt.isEnabled = true
        startTxt.isClickable = true

        stopBtn.isEnabled = true
        stopBtn.isClickable = true
        stopTxt.isEnabled = true
        stopTxt.isClickable = true

        breakTxt.isEnabled = true
        breakTxt.isClickable = true

        resetBtn.isEnabled = true
        resetBtn.isClickable = true


        startLock.setVisibility(View.GONE)
        stopLock.setVisibility(View.GONE)
        breakLock.setVisibility(View.GONE)
    }


    fun showProgressView() {

        println("showProgressView")

        pgsBar.visibility = View.VISIBLE
        empPickerCL.visibility = View.INVISIBLE
        startCL.visibility = View.INVISIBLE
        stopCL.visibility = View.INVISIBLE
        breakCL.visibility = View.INVISIBLE
        resetCL.visibility = View.INVISIBLE
        footerCL.visibility = View.INVISIBLE



    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        empPickerCL.visibility = View.VISIBLE
        startCL.visibility = View.VISIBLE
        stopCL.visibility = View.VISIBLE
        breakCL.visibility = View.VISIBLE
        resetCL.visibility = View.VISIBLE
        footerCL.visibility = View.VISIBLE
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PayrollFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PayrollFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}