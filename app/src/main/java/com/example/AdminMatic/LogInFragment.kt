package com.example.AdminMatic

//import com.android.volley.RequestQueue

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.*
import android.view.View.generateViewId
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.BuildConfig
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentLogInBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.deviceID
import com.example.AdminMatic.GlobalVars.Companion.employeeList
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.example.AdminMatic.GlobalVars.Companion.mediumBase
import com.example.AdminMatic.GlobalVars.Companion.rawBase
import com.example.AdminMatic.GlobalVars.Companion.thumbBase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class LogInFragment : Fragment() {

    private var instance: Fragment? = null

    var  sessionKey: String? = null
    var  loggedInEmpID:String? = null
    var  companyUnique:String? = null

    private lateinit var constraintLayout:ConstraintLayout
    private lateinit var companyEditText: EditText
    private lateinit  var userEditText: EditText
    private lateinit  var passEditText: EditText
    private lateinit  var rememberSwitch: SwitchCompat
    private lateinit  var submitBtn: Button
    private lateinit  var versionText: TextView
    //private lateinit  var tvDynamic: TextView

    var rememberMe:String = "0"

    val logIns = arrayOf<String>()

    lateinit  var globalVars:GlobalVars

    lateinit var myView:View
    lateinit var  pgsBar:ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("login onCreate")
        globalVars = GlobalVars()

        instance = this


        //used for async url calls
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        /*
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

         */
    }

    private var listener: LogOut? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is LogOut) {
            context
        } else {
            throw ClassCastException(
                "$context must implement LogOut"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("logIn")
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu_menu, menu)
        menu.findItem(R.id.version_item).title = getString(R.string.version, BuildConfig.VERSION_NAME, GlobalVars.phpVersion)
        menu.removeItem(R.id.reload_company_data_item)
        super.onCreateOptionsMenu(menu, inflater)
        ((activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("login onOptionsItemSelected")
        return super.onOptionsItemSelected(item)
    }


    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)


        // Inflate the layout for this fragment
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        println("Made binding")
        myView = binding.root

        //set app bar
        val colorDrawable =  ColorDrawable(Color.parseColor(resources.getString(R.color.button)))
        //(activity as AppCompatActivity).supportActionBar?.title = "Log In to AdminMatic"
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(colorDrawable)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.login_to_adminmatic)

        //set up layout container
        constraintLayout = binding.loginLayout
        constraintLayout.setBackgroundColor(Color.parseColor(resources.getString(R.color.background)))


        val preferences =
            this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)

        sessionKey = preferences.getString("sessionKey","")
        println("stored sessionKey = $sessionKey")
        loggedInEmpID = preferences.getString("loggedInEmpID","")
        println("stored loggedInEmpID = $loggedInEmpID")
        companyUnique = preferences.getString("companyUnique","")
        println("stored companyUnique = $companyUnique")

        pgsBar = binding.progressBar
        pgsBar.visibility = View.GONE

        loginOrGetSessionUser()

        return myView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun loginOrGetSessionUser(){
        println("loginOrGetSessionUser")



        if (sessionKey != ""){
            //skip ahead to getSessionUser info
            getSessionUser()

        }else{
            createLogInView()
        }




    }

    private fun createLogInView(){
        println("createLogInView")




        companyEditText = EditText(myView.context)
        companyEditText.hint = "Company Unique"
        //companyEditText.highlightColor = resources.getColor(R.color.colorTextSelected)
        companyEditText.setSingleLine()
        companyEditText.setPadding(10,0,10,0)
        companyEditText.height = 100
        companyEditText.setBackgroundResource(R.drawable.text_view_layout)



        companyEditText.id = generateViewId() // Views must have IDs in order to add them to chain later.
        binding.loginLayout.addView(companyEditText)


        userEditText = EditText(myView.context)
        userEditText.hint = "User Name"
        userEditText.setSingleLine()
        userEditText.setPadding(10,0,10,0)
        userEditText.height = 100
        userEditText.setBackgroundResource(R.drawable.text_view_layout)
        userEditText.id = generateViewId()
        binding.loginLayout.addView(userEditText)

        passEditText = EditText(myView.context)
        passEditText.hint = "Password"
        passEditText.setSingleLine()
        passEditText.setPadding(10,0,10,0)
        passEditText.height = 100
        passEditText.setBackgroundResource(R.drawable.text_view_layout)
        passEditText.id = generateViewId()
        passEditText.transformationMethod = PasswordTransformationMethod()
        passEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        binding.loginLayout.addView(passEditText)

        rememberSwitch = SwitchCompat(myView.context)
        rememberSwitch.text = getString(R.string.remember_me)
        rememberSwitch.id = generateViewId()



        /*
        val colorList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),  // Disabled
                intArrayOf(android.R.attr.state_checked)    // Enabled

            ),
            intArrayOf(
                resources.getColor(R.color.gray),     // The color for the Disabled state
                resources.getColor(R.color.colorPrimary)      // The color for the Enabled state
            )
        )

        rememberSwitch.trackTintList = colorList

         */

        rememberSwitch.setOnClickListener{
            rememberMe = if (rememberSwitch.isChecked){
                "1"
            }else{
                "2"
            }

        }
        binding.loginLayout.addView(rememberSwitch)



        submitBtn = Button(myView.context)
        submitBtn.text = getString(R.string.login)
        submitBtn.id = generateViewId()
        submitBtn.setBackgroundResource(R.drawable.button_layout)
        submitBtn.setTextColor(Color.WHITE)
        submitBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
        binding.loginLayout.addView(submitBtn)

        versionText = TextView(myView.context)
        versionText.text = getString(R.string.version, BuildConfig.VERSION_NAME, GlobalVars.phpVersion)
        versionText.gravity = Gravity.CENTER_HORIZONTAL
        versionText.id = generateViewId()
        binding.loginLayout.addView(versionText)



        submitBtn.setOnClickListener {
            // your code to perform when the user clicks on the TextView
            println("submit")

            //pgsBar.isVisible = true

            if (validateFields()) {
                println("all good")

                attemptLogIn()

            } else {
                println("no good")

            }
        }

        val listOfViews:ArrayList<View> = ArrayList()
        listOfViews.add(companyEditText)
        listOfViews.add(userEditText)
        listOfViews.add(passEditText)
        listOfViews.add(rememberSwitch)



        val listOfInts = IntArray(4)
        listOfInts[0] = companyEditText.id
        listOfInts[1] = userEditText.id
        listOfInts[2] = passEditText.id
        listOfInts[3] = rememberSwitch.id



        println("companyEditText.id = ${companyEditText.id}")
        println("submitBtn.id = ${submitBtn.id}")
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)


        var previousItem: View? =  null
        for (tv in listOfViews) {
            //val lastItem = listOfViews.indexOf(tv) === listOfViews.size() - 1
            //val lastItem = listOfViews.indexOf(tv) === listOfViews.size - 1
            if (previousItem == null) {


                constraintSet.connect(
                    tv.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,50
                )

                constraintSet.constrainWidth(tv.id,ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainMinWidth(tv.id,600)
                constraintSet.centerHorizontally(tv.id,ConstraintSet.PARENT_ID)


            } else {
                constraintSet.connect(
                    tv.id,
                    ConstraintSet.TOP,
                    previousItem.id,
                    ConstraintSet.BOTTOM,
                    50
                )

                constraintSet.constrainWidth(tv.id,ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainMinWidth(tv.id,600)
                constraintSet.centerHorizontally(tv.id,ConstraintSet.PARENT_ID)




            }
            previousItem = tv
        }

        constraintSet.connect(
            submitBtn.id,
            ConstraintSet.TOP,
            rememberSwitch.id,
            ConstraintSet.BOTTOM,
            50
        )

        constraintSet.constrainWidth(submitBtn.id,ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainMinWidth(submitBtn.id,900)
        constraintSet.centerHorizontally(submitBtn.id,ConstraintSet.PARENT_ID)

        constraintSet.connect(
            versionText.id,
            ConstraintSet.TOP,
            submitBtn.id,
            ConstraintSet.BOTTOM,
            50
        )

        constraintSet.constrainWidth(versionText.id,ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainMinWidth(versionText.id,900)
        constraintSet.centerHorizontally(versionText.id,ConstraintSet.PARENT_ID)

        constraintSet.applyTo(constraintLayout)

    }

    private fun getSessionUser(){
        println("getSessionEmp")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/sessionUser.php"

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

                    println("parentObject.getJSONObject(\"employee\").toString() = ${parentObject.getJSONObject("employee")}")

                    val employee:Employee = Gson().fromJson(parentObject.getJSONObject("employee").toString(), Employee::class.java)

                    loggedInEmployee = employee
                    println("loggedInEmployee.fname = ${loggedInEmployee!!.fname}")

                    deviceID = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

                    getFields()


                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                    listener!!.logOut(myView)
                    pgsBar.isVisible = false
                    createLogInView()


                }



            },
            Response.ErrorListener { // error

                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = companyUnique!!
                params["empID"] = loggedInEmpID!!
                params["sessionKey"] = sessionKey!!

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getPermissions(){
        println("getPermissions")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/permissions.php"

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

                    val permissions: Permissions = Gson().fromJson(parentObject.toString(), Permissions::class.java)

                    //permissions.scheduleEdit = "0"
                    GlobalVars.permissions = permissions

                    println("accounting permissions = ${GlobalVars.permissions!!.accounting}")


                    myView.findNavController().navigate(R.id.navigateToMainMenu)
                    //getStyles()





                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }



            },
            Response.ErrorListener { // error

                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = companyUnique!!
                //params["empID"] = loggedInEmpID!!
                params["sessionKey"] = sessionKey!!

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    /*
    private fun getStyles(){
        println("getStyles")

        var urlString = "https://www.adminmatic.com/cp/json/settings/$companyUnique/mainStyles.json"

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

                    myView.findNavController().navigate(R.id.navigateToMainMenu)




                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }



            },
            Response.ErrorListener { // error

                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = companyUnique!!
                //params["empID"] = loggedInEmpID!!
                params["sessionKey"] = sessionKey!!

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }
    */



    private fun validateFields():Boolean{

        hideSoftKeyboard(requireActivity())


        val allGood: Boolean

        val company: String = companyEditText.text.toString()
        //check if the EditText have values or not
        println("company = $company")
        if(company.trim().isNotEmpty() && company != "") {
            //Toast.makeText(activity,"Message : $company",Toast.LENGTH_SHORT).show()
           // allGood = true
            val user: String = userEditText.text.toString()
            //check if the EditText have values or not
            println("user = $user")
            if(user.trim().isNotEmpty() && user != "") {
                //Toast.makeText(getActivity(),"Message : $user",Toast.LENGTH_SHORT).show()
               // allGood = true
                val pass: String = passEditText.text.toString()
                //check if the EditText have values or not
                println("pass = $pass")
                allGood = if(pass.trim().isNotEmpty() && pass != "") {
                    //Toast.makeText(getActivity(),"Message : $pass",Toast.LENGTH_SHORT).show()
                    true
                }else{
                    context?.let { globalVars.simpleAlert(it,"Error","Please enter a password.") }
                    false
                }
            }else{
                context?.let { globalVars.simpleAlert(it,"Error","Please enter a username.") }
                allGood =false
            }
        }else{
            context?.let { globalVars.simpleAlert(it,"Error","Please enter a company identifier.") }
            allGood = false
        }






        return allGood

    }




    //using StringRequest
    private fun attemptLogIn(){
        println("attemptLogIn")


        loading()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/other/login.php"

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
                    //Todo: re-enable this once PHP is fixed
                    //globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val errorArray:JSONArray = parentObject.getJSONArray("errorArray")
                    if (errorArray.length() > 0){
                        println("log in error ${errorArray[0]}")
                        //Toast.makeText(activity,errorArray[0].toString(),Toast.LENGTH_LONG).show()
                        val errorString = errorArray[0].toString()
                        context?.let { globalVars.simpleAlert(it,"Error",errorArray[0].toString()) }
                        stopLoading()

                        // This is a messy way of doing it, especially if we want to have language options in the future
                        // Should probably send an error code, but this works for now
                        when (errorString[0]) {
                            'N' -> { //No company found with that identifier
                                companyEditText.setText("")
                            }
                            'I' -> { //Incorrect login
                                userEditText.setText("")
                                passEditText.setText("")
                            }
                            'P' -> { //Password incorrect
                                passEditText.setText("")
                            }
                            else -> {
                                companyEditText.setText("")
                                userEditText.setText("")
                                passEditText.setText("")
                            }
                        }

                    }else{

                        println("parentObject.getJSONObject(\"employee\").toString() = ${parentObject.getJSONObject("employee")}")

                        val employee:Employee = Gson().fromJson(parentObject.getJSONObject("employee").toString(), Employee::class.java)

                        loggedInEmployee = employee

                        //textView.text = "Device ID: $id"

                        deviceID = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                        println("loggedInEmployee.fname = ${loggedInEmployee!!.fname}")

                        sessionKey = loggedInEmployee!!.sessionKey
                        loggedInEmpID = loggedInEmployee!!.sessionKey
                        companyUnique = loggedInEmployee!!.companyUnique

                        //save stored vars
                        val sharedPref = this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {

                            putString("loggedInEmpID",loggedInEmployee!!.ID)
                            putString("sessionKey",loggedInEmployee!!.sessionKey)
                            putString("companyUnique",loggedInEmployee!!.companyUnique)
                            putString("deviceID",deviceID)
                            apply()
                        }
                        globalVars.playSaveSound(myView.context)
                        getFields()
                    }



                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }



               // myView.findNavController().navigate(R.id.navigateToMainMenu)
            },
            Response.ErrorListener { // error


                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = "${companyEditText.text}"
                params["username"] = "${userEditText.text}"
                params["password"] = "${passEditText.text}"
                params["remember"] = rememberMe
                params["device"] = globalVars.getDeviceName()
                params["deviceToken"] = "345"
                params["notificationType"] = "2" // 1 = iOS, 2 = Android
                params["phpVersion"] = GlobalVars.phpVersion
                params["appVersion"] = BuildConfig.VERSION_NAME
                params["deviceInfo"] = globalVars.getDeviceName()
                //params["autokill"] = "1"

                params["logins"] = logIns.toString()

                val id: String = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                params["deviceID"] = id

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    fun getFields(){
        //"https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/fields.php"
        println("getFields")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/fields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")



                try {
                    val parentObject = JSONObject(response)
                    println("parentObject get fields = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        globalVars.populateFields(context, parentObject)
                        getEmployees()
                    }
                    else {
                        createLogInView()
                    }


                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }



               // myView.findNavController().navigate(R.id.navigateToMainMenu)
            },
            Response.ErrorListener { // error


                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    fun getEmployees(){
        println("getEmployees")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)
                    //println("parentObject = ${parentObject.toString()}")
                    //var employees:JSONArray = parentObject.getJSONArray("employees")
                    // println("employees = ${employees.toString()}")
                    //println("employees count = ${employees.length()}")


                    val employeeJSONArray = parentObject.getJSONArray("employees")
                    // employeeList = parentObject.getJSONArray("employees")


                    val gson = GsonBuilder().create()
                    employeeList = gson.fromJson(employeeJSONArray.toString(), Array<Employee>::class.java)


                    // myView.findNavController().navigate(R.id.navigateToMainMenu)
                    getCustomers()

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error


                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
      postRequest1.tag = "logIn"
      VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }





    private fun getCustomers(){
        println("getCustomers")

        //showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customers.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
               // hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    //var customers: JSONObject = parentObject.getJSONObject("customers")
                    val customers: JSONArray = parentObject.getJSONArray("customers")
                    // println("customers = ${customers.toString()}")
                    // println("customers count = ${customers.length()}")

                    val gson = GsonBuilder().create()
                    GlobalVars.customerList = gson.fromJson(customers.toString() , Array<Customer>::class.java).toMutableList()

                    getPermissions()




                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
                //Log.e("VOLLEY", error.toString())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }












    fun loading(){
        for (index in 0 until (binding.loginLayout as ViewGroup).childCount) {
            val nextChild = (binding.loginLayout as ViewGroup).getChildAt(index)
            nextChild.isVisible = false
        }
        pgsBar.isVisible = true
    }

    fun stopLoading(){
        for (index in 0 until (binding.loginLayout as ViewGroup).childCount) {
            val nextChild = (binding.loginLayout as ViewGroup).getChildAt(index)
            nextChild.isVisible = true
        }
        pgsBar.isVisible = false
    }


    private fun hideSoftKeyboard(activity: Activity) {
        if (activity.currentFocus == null) {
            return
        }
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }

}


