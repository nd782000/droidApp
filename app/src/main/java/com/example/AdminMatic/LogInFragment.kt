package com.example.AdminMatic

//import com.android.volley.RequestQueue

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class LogInFragment : Fragment() {

    private var instance: Fragment? = null

    var  sessionKey: String? = null
    var  loggedInEmpID:String? = null
    var  companyUnique:String? = null

    var rememberMe:String = "0"

    val logIns = arrayOf<String>()

    lateinit  var globalVars:GlobalVars
    



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
        inflater.inflate(R.menu.login_menu, menu)
        menu.findItem(R.id.version_item).title = getString(R.string.version, BuildConfig.VERSION_NAME, GlobalVars.phpVersion)
        //menu.removeItem(R.id.reload_company_data_item)
        //menu.removeItem(R.id.departments_item)
        //menu.removeItem(R.id.crews_item)
        //menu.removeItem(R.id.change_password_item)
        super.onCreateOptionsMenu(menu, inflater)
        ((activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("login onOptionsItemSelected")
        when (item.itemId) {
            R.id.force_logout_item -> {
                listener!!.logOut(myView)
                stopLoading()
            }

            R.id.privacy_policy_item -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.adminmatic.com/app/privacy"))
                startActivity(intent)
            }
            R.id.support_item -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.adminmatic.com/support"))
                startActivity(intent)
            }
        }
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

        val preferences =
            this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)

        sessionKey = preferences.getString("sessionKey","")
        println("stored sessionKey = $sessionKey")
        loggedInEmpID = preferences.getString("loggedInEmpID","")
        println("stored loggedInEmpID = $loggedInEmpID")
        companyUnique = preferences.getString("companyUnique","")
        println("stored companyUnique = $companyUnique")

        //binding.progressBar.visibility = View.GONE

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginRememberMeSwitch.setOnClickListener{
            rememberMe = if (binding.loginRememberMeSwitch.isChecked){
                "1"
            }
            else {
                "2"
            }
        }

        binding.loginButton.setOnClickListener {
            println("submit")

            if (validateFields()) {
                println("all good")

                attemptLogIn()

            } else {
                println("no good")

            }
        }

        binding.loginVersionTv.text = getString(R.string.version, BuildConfig.VERSION_NAME, GlobalVars.phpVersion)

        binding.loginEnterDemoTv.setOnClickListener {
            binding.loginCompanyUniqueEt.setText("demo")
            binding.loginUsernameEt.setText("demo")
            binding.loginPasswordEt.setText("adminmatic")
            attemptLogIn()
        }

        loginOrGetSessionUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun loginOrGetSessionUser(){
        println("loginOrGetSessionUser")

        if (sessionKey != ""){
            //skip ahead to getSessionUser info
            //binding.loginCl.visibility = View.VISIBLE
            getSessionUser()
        }
        else {
            stopLoading()
        }

    }


    private fun getSessionUser(){
        println("getSessionEmp")

        loading()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/sessionUser.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Get session user response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (!globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        listener!!.logOut(myView)
                        stopLoading()
                    }

                    //println("parentObject.getJSONObject(\"employee\").toString() = ${parentObject.getJSONObject("employee")}")

                    val employee:Employee = Gson().fromJson(parentObject.getJSONObject("employee").toString(), Employee::class.java)

                    loggedInEmployee = employee

                    if (loggedInEmployee!!.lang == "EN") {
                        val locale = Locale("es-*","mexico")
                        Locale.setDefault(locale)

                        val resources = myView.context.resources

                        val configuration = resources.configuration
                        configuration.locale = locale
                        configuration.setLayoutDirection(locale)

                        resources.updateConfiguration(configuration, resources.displayMetrics)
                    }

                    println("loggedInEmployee.fname = ${loggedInEmployee!!.fname}")

                    deviceID = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

                    binding.loginCompanyUniqueEt.text.clear()
                    binding.loginUsernameEt.text.clear()
                    binding.loginPasswordEt.text.clear()
                    binding.loginRememberMeSwitch.isChecked = false
                    binding.loginRememberMeSwitch.jumpDrawablesToCurrentState()

                    getFields()


                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                    listener!!.logOut(myView)
                    stopLoading()
                }
            },
            Response.ErrorListener { error ->
                println("GET SESSION USER ERROR: $error")
                globalVars.simpleAlert(myView.context,"Get Session User Error:", error.toString())
                listener!!.logOut(myView)
                stopLoading()
                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = companyUnique!!
                params["empID"] = loggedInEmpID!!
                params["sessionKey"] = sessionKey!!

                println("Get session user params = $params")
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
                    if (!globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        listener!!.logOut(myView)
                        stopLoading()
                    }

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
                    globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                    listener!!.logOut(myView)
                    stopLoading()
                }



            },
            Response.ErrorListener { // error
                globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                listener!!.logOut(myView)
                stopLoading()
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

        val company: String = binding.loginCompanyUniqueEt.text.toString()
        //check if the EditText have values or not
        println("company = $company")
        if(company.trim().isNotEmpty() && company != "") {
            //Toast.makeText(activity,"Message : $company",Toast.LENGTH_SHORT).show()
           // allGood = true
            val user: String = binding.loginUsernameEt.text.toString()
            //check if the EditText have values or not
            println("user = $user")
            if(user.trim().isNotEmpty() && user != "") {
                //Toast.makeText(getActivity(),"Message : $user",Toast.LENGTH_SHORT).show()
               // allGood = true
                val pass: String = binding.loginPasswordEt.text.toString()
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
                                binding.loginCompanyUniqueEt.setText("")
                            }
                            'I' -> { //Incorrect login
                                binding.loginUsernameEt.setText("")
                                binding.loginPasswordEt.setText("")
                            }
                            'P' -> { //Password incorrect
                                binding.loginPasswordEt.setText("")
                            }
                            else -> {
                                binding.loginCompanyUniqueEt.setText("")
                                binding.loginUsernameEt.setText("")
                                binding.loginPasswordEt.setText("")
                            }
                        }

                    }
                    else {

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

                        GlobalVars.shouldLogOut = false

                        binding.loginCompanyUniqueEt.text.clear()
                        binding.loginUsernameEt.text.clear()
                        binding.loginPasswordEt.text.clear()
                        binding.loginRememberMeSwitch.isChecked = false
                        binding.loginRememberMeSwitch.jumpDrawablesToCurrentState()

                        getFields()
                    }



                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                    globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                    listener!!.logOut(myView)
                    stopLoading()
                }



               // myView.findNavController().navigate(R.id.navigateToMainMenu)
            },
            Response.ErrorListener { error ->
                println("LOGIN USER ERROR: $error")
                globalVars.simpleAlert(myView.context,"Login Error:", error.toString())
                listener!!.logOut(myView)
                stopLoading()
                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = "${binding.loginCompanyUniqueEt.text}"
                params["username"] = "${binding.loginUsernameEt.text}"
                params["password"] = "${binding.loginPasswordEt.text}"
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
                        listener!!.logOut(myView)
                        stopLoading()
                    }


                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                    globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                    listener!!.logOut(myView)
                    stopLoading()
                }



               // myView.findNavController().navigate(R.id.navigateToMainMenu)
            },
            Response.ErrorListener { // error
                listener!!.logOut(myView)
                stopLoading()
                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["salesTax"] = "1"
                params["tax"] = "1"
                params["crews"] = "1"
                params["departments"] = "1"
                params["zones"] = "1"
                params["emails"] = "1"
                params["contactTypes"] = "1"
                params["hearTypes"] = "1"
                params["vendorCategories"] = "1"
                params["albums"] = "1"
                params["terms"] = "1"
                params["templates"] = "1"
                params["depositTypes"] = "1"
                params["defaults"] = "1"
                params["units"] = "1"

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
                    if (!globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                        listener!!.logOut(myView)
                        stopLoading()
                    }
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
                    globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                    listener!!.logOut(myView)
                    stopLoading()
                }

            },
            Response.ErrorListener { // error
                globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                listener!!.logOut(myView)
                stopLoading()

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
                    if (!globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)){
                        listener!!.logOut(myView)
                        stopLoading()
                    }

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
                    globalVars.simpleAlert(myView.context,"Login Error:", "JSONException")
                    listener!!.logOut(myView)
                    stopLoading()
                }
            },
            Response.ErrorListener { // error
                //Log.e("VOLLEY", error.toString())
                listener!!.logOut(myView)
                stopLoading()
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

    fun loading() {
        binding.loginCl.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun stopLoading() {
        binding.loginCl.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
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


