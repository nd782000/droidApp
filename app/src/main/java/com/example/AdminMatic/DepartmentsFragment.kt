package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class DepartmentsFragment : Fragment(), EmployeeCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    lateinit var recyclerView: RecyclerView
    lateinit var footerText:TextView

    lateinit var departmentsAdapter:DepartmentAdapter
    lateinit var crewsAdapter:DepartmentAdapter
    lateinit var equipmentAdapter:DepartmentAdapter

    private lateinit var tabLayout: TabLayout
    lateinit var tableMode:String

    private var departmentCount:Int = 0
    private var crewCount:Int = 0
    private var equipmentCount:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        arguments?.let {
            equipment = it.getParcelable("equipment")
            param2 = it.getString(ARG_PARAM2)
        }

         */
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        myView = inflater.inflate(R.layout.fragment_departments, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment)



        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pgsBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.departments_recycler_view)
        tabLayout = myView.findViewById(R.id.departments_table_tl)
        footerText = myView.findViewById(R.id.department_footer_tv)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = "DEPARTMENTS"
                        recyclerView.adapter = departmentsAdapter
                        footerText.text = getString(R.string.x_departments, departmentCount)
                    }
                    1 -> {
                        //recyclerView.adapter = crewsAdapter
                        tableMode = "CREWS"
                        footerText.text = getString(R.string.x_crews, crewCount)
                    }
                    2 -> {
                        //recyclerView.adapter = equipmentAdapter
                        tableMode = "EQUIPMENT"
                        footerText.text = getString(R.string.x_equipment, equipmentCount)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        tableMode = "DEPARTMENTS"
        getDepartments()

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

    private fun getDepartments(){
        println("getDepartments")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {

                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")



                    val departments:JSONArray = parentObject.getJSONArray("departments")
                    println("departments = $departments")
                    println("departments count = ${departments.length()}")

                    val departmentsList = gson.fromJson(departments.toString() , Array<Department>::class.java).toMutableList()
                    println("DepartmentsCount = ${departmentsList.count()}")

                    // Clear empty crews from the view
                    val depIterator = departmentsList.iterator()
                    while (depIterator.hasNext()) {
                        val dep = depIterator.next()
                        if (dep.emps!!.isEmpty()) {
                            depIterator.remove()
                        }
                    }

                    departmentsAdapter = DepartmentAdapter(departmentsList, this.myView.context, this@DepartmentsFragment)
                    println(departmentsAdapter.itemCount)
                    recyclerView.adapter = departmentsAdapter
                    recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)

                    departmentCount = departmentsList.size
                    footerText.text = getString(R.string.x_departments, departmentCount)



                    getCrews()


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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["crewView"] = "0"
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }


    private fun getCrews(){
        println("getCrews")


        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {

                    val gson = GsonBuilder().create()

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")

                    val crews:JSONArray = parentObject.getJSONArray("crews")
                    println("crews = $crews")
                    println("crews count = ${crews.length()}")

                    val crewsList = gson.fromJson(crews.toString() , Array<Crew>::class.java).toMutableList()


                    // Clear empty crews from the view
                    val crewIterator = crewsList.iterator()
                    while (crewIterator.hasNext()) {
                        val crew = crewIterator.next()
                        if (crew.emps!!.isEmpty()) {
                            crewIterator.remove()
                        }
                    }

                    println("CrewsCount = ${crewsList.count()}")
                    crewCount = crewsList.size

                    /*
                    departmentsAdapter = DepartmentAdapter(crewsList, this.myView.context, this@DepartmentsFragment)
                    println(departmentsAdapter.itemCount)
                    recyclerView.adapter = departmentsAdapter
                    recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                    */

                    //getEquipmentCrews()


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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["crewView"] = "1"
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        //currentRecyclerView.visibility = View.INVISIBLE
        // historyRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
        // currentRecyclerView.visibility = View.VISIBLE
        // historyRecyclerView.visibility = View.VISIBLE
    }

}
