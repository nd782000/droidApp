package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEmployeeListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface EmployeeCellClickListener {
    fun onEmployeeCellClickListener(data:Employee)
}

/*
class EmployeeListViewModel : ViewModel() {

    val showInactive: Boolean = false

    fun updateSettings(_showInactive:Boolean) {
}

 */


class EmployeeListFragment : Fragment(), EmployeeCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var employeesList: MutableList<Employee>

    lateinit var adapter:EmployeesAdapter

    private var dataLoaded = false
    private var showInactive = false

    private var _binding: FragmentEmployeeListBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("_showInactive") { _, bundle ->
            println("Fragment result listener went off")
            val result = bundle.getBoolean("_showInactive")

            if (result != showInactive) {
                showInactive = result
                if (showInactive) {
                    println("setting button color yellow")
                    ImageViewCompat.setImageTintList(binding.settingsIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, R.color.settingsActive)))
                }
                else {
                    println("setting button color default")
                    ImageViewCompat.setImageTintList(binding.settingsIv, null)
                }
                getEmployees()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("ON CREATE VIEW")
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
            getString(R.string.employee_list)
        if (!dataLoaded) {
            globalVars = GlobalVars()
            _binding = FragmentEmployeeListBinding.inflate(inflater, container, false)
            myView = binding.root

            val emptyList: MutableList<Employee> = mutableListOf()
            adapter = EmployeesAdapter(emptyList, true, myView.context, this)


        }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")

        if (!dataLoaded) {
            println("Data not loaded")
            dataLoaded = true

            //crewsBtn = view.findViewById(R.id.crews_btn)
            binding.crewsBtn.setOnClickListener {
                val directions = EmployeeListFragmentDirections.navigateToCrews(null)
                myView.findNavController().navigate(directions)
            }

            //groupTextBtn = view.findViewById(R.id.group_text_btn)
            binding.groupTextBtn.setOnClickListener {
                val directions = EmployeeListFragmentDirections.navigateToGroupText(employeesList.toTypedArray())
                myView.findNavController().navigate(directions)
            }

            binding.newEmployeeBtn.setOnClickListener {
                if (GlobalVars.permissions!!.employeesEdit == "1") {
                    val directions = EmployeeListFragmentDirections.navigateToNewEditEmployee(null)
                    myView.findNavController().navigate(directions)
                } else {
                    globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_employees_edit))
                }
            }

            binding.newEmployeeBtn.setOnClickListener {
                if (GlobalVars.permissions!!.employeesEdit == "1") {
                    val directions = EmployeeListFragmentDirections.navigateToNewEditEmployee(null)
                    myView.findNavController().navigate(directions)
                } else {
                    globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_employees_edit))
                }
            }

            binding.settingsBtn.setOnClickListener {
                val directions = EmployeeListFragmentDirections.navigateToEmployeeListSettings(showInactive)
                myView.findNavController().navigate(directions)
            }

            getEmployees()
        }



    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("employeeList")
    }

    private fun getEmployees(){
        println("getEmployees")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        //val preferences =
        //this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
        // val session = preferences.getString("sessionKey","")
        //val companyUnique = preferences.getString("companyUnique","")


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = ${parentObject.toString()}")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val employees: JSONArray = parentObject.getJSONArray("employees")
                        // println("employees = ${employees.toString()}")
                        // println("employees count = ${employees.length()}")


                        val gson = GsonBuilder().create()
                        employeesList =
                            gson.fromJson(employees.toString(), Array<Employee>::class.java)
                                .toMutableList()

                        if (showInactive) {
                            binding.employeeCountTextview.text = getString(R.string.x_inactive_employees, employeesList.size)
                        }
                        else {
                            binding.employeeCountTextview.text = getString(R.string.x_active_employees, employeesList.size)
                        }

                        binding.employeesRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                EmployeesAdapter(
                                    employeesList, true, it, this@EmployeeListFragment
                                )
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(
                                    myView.context,
                                    DividerItemDecoration.VERTICAL
                                )
                            binding.employeesRecyclerView.addItemDecoration(itemDecoration)

                            //for item animations
                            // recyclerView.itemAnimator = SlideInUpAnimator()


                            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                            // Setup refresh listener which triggers new data loading
                            // Setup refresh listener which triggers new data loading
                            binding.employeeSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                                // Make sure you call swipeContainer.setRefreshing(false)
                                // once the network request has completed successfully.
                                //fetchTimelineAsync(0)
                                binding.employeesSearch.setQuery("", false)
                                binding.employeesSearch.clearFocus()
                                getEmployees()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            binding.employeeSwipeContainer.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )


                            //(adapter as EmployeesAdapter).notifyDataSetChanged()

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            binding.employeeSwipeContainer.isRefreshing = false

                            // Toast.makeText(activity,"${employeesList.count()} Employees Loaded",Toast.LENGTH_SHORT).show()


                            //search listener
                            binding.employeesSearch.setOnQueryTextListener(object :
                                SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    myView.hideKeyboard()
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as EmployeesAdapter).filter.filter(newText)
                                    return false
                                }

                            })


                        }
                    }




                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }


                // var intent:Intent = Intent(applicationContext,MainActivity2::class.java)
                // startActivity(intent)
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
                if (showInactive) {
                    params["active"] = "0"
                }
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "employeeList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onEmployeeCellClickListener(data:Employee) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()
        data.let {
            val directions = EmployeeListFragmentDirections.navigateToEmployee(it)
            myView.findNavController().navigate(directions)
        }
        println("Cell clicked with employee: ${data.fname}")
    }



    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE

    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    fun updateSettings(_showInactive:Boolean) {
        showInactive = _showInactive
        dataLoaded = false
    }

}