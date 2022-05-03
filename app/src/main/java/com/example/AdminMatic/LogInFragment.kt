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
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.generateViewId
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */





class LogInFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    private var instance: Fragment? = null

    var  sessionKey: String? = null
    var  loggedInEmpID:String? = null
    var  companyUnique:String? = null

    private lateinit var constraintLayout:ConstraintLayout
    private lateinit var companyEditText: EditText
    private lateinit  var userEditText: EditText
    private lateinit  var passEditText: EditText
    private lateinit  var rememberSwitch: Switch
    private lateinit  var submitBtn: Button
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



        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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




    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {




        // Inflate the layout for this fragment
         myView = inflater.inflate(R.layout.fragment_log_in, container, false)

        //set app bar
        val colorDrawable =  ColorDrawable(Color.parseColor(resources.getString(R.color.button)))
        //(activity as AppCompatActivity).supportActionBar?.title = "Log In to AdminMatic"
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(colorDrawable)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.login_to_adminmatic)

        //set up layout container
        constraintLayout = myView.findViewById(R.id.loginLayout)
        constraintLayout.setBackgroundColor(Color.parseColor(resources.getString(R.color.background)))


        val preferences =
            this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)

        sessionKey = preferences.getString("sessionKey","")
        println("stored sessionKey = $sessionKey")
        loggedInEmpID = preferences.getString("loggedInEmpID","")
        println("stored loggedInEmpID = $loggedInEmpID")
        companyUnique = preferences.getString("companyUnique","")
        println("stored companyUnique = $companyUnique")


        loginOrGetSessionUser()

        return myView
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


        pgsBar = myView.findViewById(R.id.progressBar) as ProgressBar
        pgsBar.isVisible = false


        companyEditText = EditText(myView.context)
        companyEditText.hint = "Company Unique"
        companyEditText.setSingleLine()
        companyEditText.setPadding(10,0,10,0)


        companyEditText.setBackgroundResource(R.drawable.text_view_layout)

        companyEditText.id = generateViewId() // Views must have IDs in order to add them to chain later.
        myView.findViewById<ConstraintLayout>(R.id.loginLayout).addView(companyEditText)


        userEditText = EditText(myView.context)
        userEditText.hint = "User Name"
        userEditText.setSingleLine()
        userEditText.setPadding(10,0,10,0)
        userEditText.setBackgroundResource(R.drawable.text_view_layout)
        userEditText.id = generateViewId()
        myView.findViewById<ConstraintLayout>(R.id.loginLayout).addView(userEditText)

        passEditText = EditText(myView.context)

        passEditText.hint = "Password"
        passEditText.setSingleLine()
        passEditText.setPadding(10,0,10,0)
        passEditText.setBackgroundResource(R.drawable.text_view_layout)
        passEditText.id = generateViewId()
        passEditText.transformationMethod = PasswordTransformationMethod()
        myView.findViewById<ConstraintLayout>(R.id.loginLayout).addView(passEditText)

        rememberSwitch = Switch(myView.context)
        rememberSwitch.text = getString(R.string.remember_me)
        rememberSwitch.id = generateViewId()

        rememberSwitch.setOnClickListener{
            rememberMe = if (rememberSwitch.isChecked){
                "1"
            }else{
                "2"
            }

        }
        myView.findViewById<ConstraintLayout>(R.id.loginLayout).addView(rememberSwitch)



        submitBtn = Button(myView.context)
        submitBtn.text = getString(R.string.login)
        submitBtn.id = generateViewId()
        submitBtn.setBackgroundResource(R.drawable.button_layout)
        submitBtn.setTextColor(Color.WHITE)
        submitBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
        myView.findViewById<ConstraintLayout>(R.id.loginLayout).addView(submitBtn)



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

        constraintSet.applyTo(constraintLayout)
    }

    private fun getSessionUser(){
        println("getSessionEmp")


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/sessionUser.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(activity)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        println("parentObject.getJSONObject(\"employee\").toString() = ${parentObject.getJSONObject("employee")}")

                        val employee:Employee = Gson().fromJson(parentObject.getJSONObject("employee").toString(), Employee::class.java)

                        loggedInEmployee = employee
                        println("loggedInEmployee.fname = ${loggedInEmployee!!.fName}")

                        deviceID = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

                        getFields()
                    }


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
                params["empID"] = loggedInEmpID!!
                params["sessionKey"] = sessionKey!!

                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    private fun getPermissions(){
        println("getPermissions")


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/permissions.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(activity)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        val permissions: Permissions = Gson().fromJson(parentObject.toString(), Permissions::class.java)

                        GlobalVars.permissions = permissions
                        println("accounting permissions = ${GlobalVars.permissions!!.accounting}")


                        myView.findNavController().navigate(R.id.navigateToMainMenu)
                    }


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
        queue.add(postRequest1)


    }






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
                    Toast.makeText(activity,"Please enter a Password.",Toast.LENGTH_SHORT).show()
                    false
                }
            }else{

                Toast.makeText(activity,"Please enter a User Name.",Toast.LENGTH_SHORT).show()

                allGood =false
            }
        }else{
            Toast.makeText(activity,"Please enter Company Identifier.",Toast.LENGTH_SHORT).show()
            allGood = false
        }






        return allGood

    }




    //using StringRequest
    private fun attemptLogIn(){
        println("attemptLogIn")


        loading()


        var urlString = "https://www.adminmatic.com/cp/app/functions/other/login.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(activity)




        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")



                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        val errorArray:JSONArray = parentObject.getJSONArray("errorArray")
                        if (errorArray.length() > 0){
                            println("log in error ${errorArray[0]}")
                            Toast.makeText(activity,errorArray[0].toString(),Toast.LENGTH_LONG).show()
                            stopLoading()
                            companyEditText.setText("")
                            userEditText.setText("")
                            passEditText.setText("")
                        }else{

                            println("parentObject.getJSONObject(\"employee\").toString() = ${parentObject.getJSONObject("employee")}")

                            val employee:Employee = Gson().fromJson(parentObject.getJSONObject("employee").toString(), Employee::class.java)

                            loggedInEmployee = employee

                            //textView.text = "Device ID: $id"

                            deviceID = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                            println("loggedInEmployee.fname = ${loggedInEmployee!!.fName}")

                            //save stored vars
                            val sharedPref = this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
                            with (sharedPref.edit()) {

                                putString("loggedInEmpID",loggedInEmployee!!.ID)
                                putString("sessionKey",loggedInEmployee!!.sessionKey)
                                putString("companyUnique",loggedInEmployee!!.companyUnique)
                                putString("deviceID",deviceID)
                                apply()
                            }
                            getFields()
                        }
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

                params["logins"] = logIns.toString()

                val id: String = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                params["deviceID"] = id

                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }


    fun getFields(){
        //"https://www.adminmatic.com/cp/app/functions/get/fields.php"
        println("getFields")

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/fields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(activity)



        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")



                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")


                        thumbBase = parentObject.getString("thumbBase")
                        mediumBase = parentObject.getString("mediumBase")
                        rawBase = parentObject.getString("rawBase")
                        println("thumbBase= $thumbBase")
                    }

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }


                getEmployees()
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
        queue.add(postRequest1)

    }


  fun getEmployees(){
        println("getEmployees")


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
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
                    }

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
        queue.add(postRequest1)
    }





    private fun getCustomers(){
        println("getCustomers")

        //showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/customers.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
               // hideProgressView()
                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        //var customers: JSONObject = parentObject.getJSONObject("customers")
                        val customers: JSONArray = parentObject.getJSONArray("customers")
                        // println("customers = ${customers.toString()}")
                        // println("customers count = ${customers.length()}")

                        val gson = GsonBuilder().create()
                        GlobalVars.customerList = gson.fromJson(customers.toString() , Array<Customer>::class.java).toMutableList()

                        getPermissions()

                        /*
                        list_recycler_view.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = activity?.let {
                                CustomersAdapter(customersList,
                                    it, this@CustomerListFragment)
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            recyclerView.addItemDecoration(itemDecoration)


                            // Setup refresh listener which triggers new data loading
                            swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                                // Make sure you call swipeContainer.setRefreshing(false)
                                // once the network request has completed successfully.
                                searchView.setQuery("", false);
                                searchView.clearFocus();
                                getCustomers()
                            }
                            // Configure the refreshing colors
                            swipeRefresh.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )
                            (adapter as CustomersAdapter).notifyDataSetChanged();

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // Now we call setRefreshing(false) to signal refresh has finished
                            customerSwipeContainer.isRefreshing = false;

                            Toast.makeText(activity,"${customersList.count()} Customers Loaded", Toast.LENGTH_SHORT).show()

                            //search listener
                            customers_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as CustomersAdapter).filter.filter(newText)
                                    return false
                                }

                            })
                        }
                        */
                    }


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
        queue.add(postRequest1)
    }












    fun loading(){
        for (index in 0 until (myView.findViewById<ConstraintLayout>(R.id.loginLayout) as ViewGroup).childCount) {
            val nextChild = (myView.findViewById<ConstraintLayout>(R.id.loginLayout) as ViewGroup).getChildAt(index)
            nextChild.isVisible = false
        }
        pgsBar.isVisible = true
    }

    fun stopLoading(){
        for (index in 0 until (myView.findViewById<ConstraintLayout>(R.id.loginLayout) as ViewGroup).childCount) {
            val nextChild = (myView.findViewById<ConstraintLayout>(R.id.loginLayout) as ViewGroup).getChildAt(index)
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LogInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LogInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


