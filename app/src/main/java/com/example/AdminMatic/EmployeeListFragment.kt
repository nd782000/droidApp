package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_employee_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text


interface EmployeeCellClickListener {
    fun onEmployeeCellClickListener(data:Employee)
}



class EmployeeListFragment : Fragment(), EmployeeCellClickListener {


    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView: androidx.appcompat.widget.SearchView
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var employeeCountTv: TextView

    //lateinit var  groupTextBtn: Button
    private lateinit var  crewsBtn: Button

    lateinit var adapter:EmployeesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_employee_list, container, false)


        val emptyList:MutableList<Employee> = mutableListOf()

        adapter = EmployeesAdapter(emptyList,myView.context, this)





        //(activity as AppCompatActivity).supportActionBar?.title = "Employee List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.employee_list)

        // Inflate the layout for this fragment
      //  return inflater.inflate(R.layout.fragment_employee_list, container, false)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.employees_search)
        swipeRefresh = view.findViewById(R.id.customerSwipeContainer)
        employeeCountTv = view.findViewById(R.id.employee_count_textview)
        crewsBtn = view.findViewById(R.id.crews_btn)


        crewsBtn.setOnClickListener{
            val directions = EmployeeListFragmentDirections.navigateToDepartments(false)
            myView.findNavController().navigate(directions)
        }

        getEmployees()

    }


    private fun getEmployees(){
        println("getEmployees")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)


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
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        //println("parentObject = ${parentObject.toString()}")
                        val employees:JSONArray = parentObject.getJSONArray("employees")
                       // println("employees = ${employees.toString()}")
                       // println("employees count = ${employees.length()}")



                        val gson = GsonBuilder().create()
                        val employeesList = gson.fromJson(employees.toString() , Array<Employee>::class.java).toMutableList()

                        employeeCountTv.text = getString(R.string.x_active_employees, employeesList.size)

                        list_recycler_view.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                EmployeesAdapter(employeesList,
                                    it, this@EmployeeListFragment)
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            recyclerView.addItemDecoration(itemDecoration)

                            //for item animations
                            // recyclerView.itemAnimator = SlideInUpAnimator()



                            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                            // Setup refresh listener which triggers new data loading
                            // Setup refresh listener which triggers new data loading
                            swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                                // Make sure you call swipeContainer.setRefreshing(false)
                                // once the network request has completed successfully.
                                //fetchTimelineAsync(0)
                                searchView.setQuery("", false)
                                searchView.clearFocus()
                                getEmployees()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            swipeRefresh.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )



                            (adapter as EmployeesAdapter).notifyDataSetChanged()

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            customerSwipeContainer.isRefreshing = false

                            // Toast.makeText(activity,"${employeesList.count()} Employees Loaded",Toast.LENGTH_SHORT).show()



                            //search listener
                            employees_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
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
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    override fun onEmployeeCellClickListener(data:Employee) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()
        data.let {
            val directions = EmployeeListFragmentDirections.navigateToEmployee(it)
            myView.findNavController().navigate(directions)
        }
        println("Cell clicked with employee: ${data.name}")
    }



    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        searchView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        searchView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
    }

}