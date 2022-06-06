package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_main_menu.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DepartmentsFragment : Fragment(), EmployeeCellClickListener, EquipmentCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    lateinit var recyclerView: RecyclerView
    lateinit var footerText:TextView

    lateinit var departmentAdapter:DepartmentAdapter
    lateinit var crewAdapter:CrewAdapter
    lateinit var equipmentCrewAdapter:EquipmentCrewAdapter

    private lateinit var tabLayout: TabLayout
    lateinit var tableMode:String

    private var departmentCount:Int = 0
    private var crewCount:Int = 0
    private var equipmentCount:Int = 0
    private var personal : Boolean = false
    private var empString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            personal = it.getBoolean("personal")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        myView = inflater.inflate(R.layout.fragment_departments, container, false)

        globalVars = GlobalVars()
        if (personal) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews, GlobalVars.loggedInEmployee!!.fName)
            GlobalVars.loggedInEmployee!!.fName
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)
        }



        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pgsBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.departments_recycler_view)
        tabLayout = myView.findViewById(R.id.departments_table_tl)
        footerText = myView.findViewById(R.id.department_footer_tv)

        val tab = tabLayout.getTabAt(1)
        tab!!.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = "DEPARTMENTS"
                        recyclerView.adapter = departmentAdapter
                        footerText.text = getString(R.string.x_departments, departmentCount)
                        if (personal) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_departments, GlobalVars.loggedInEmployee!!.fName)
                            GlobalVars.loggedInEmployee!!.fName
                        }
                        else {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.departments)
                        }

                    }
                    1 -> {
                        recyclerView.adapter = crewAdapter
                        tableMode = "CREWS"
                        footerText.text = getString(R.string.x_crews, crewCount)
                        if (personal) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews, GlobalVars.loggedInEmployee!!.fName)
                            GlobalVars.loggedInEmployee!!.fName
                        }
                        else {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)
                        }
                    }
                    2 -> {
                        recyclerView.adapter = equipmentCrewAdapter
                        tableMode = "EQUIPMENT"
                        footerText.text = getString(R.string.x_equipment, equipmentCount)
                        if (personal) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_equipment, GlobalVars.loggedInEmployee!!.fName)
                            GlobalVars.loggedInEmployee!!.fName
                        }
                        else {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment)
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })



        if (personal) {
            empString = GlobalVars.loggedInEmployee!!.ID
        }

        //tableMode = "DEPARTMENTS"
        getDepartments()

    }

    override fun onEmployeeCellClickListener(data:Employee) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()
        data.let {
            val directions = DepartmentsFragmentDirections.navigateToEmployee(it)
            myView.findNavController().navigate(directions)
        }
        println("Cell clicked with employee: ${data.name}")
    }

    override fun onEquipmentCellClickListener(data:Equipment) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()
        data.let {
            val directions = DepartmentsFragmentDirections.navigateToEquipment(it)
            myView.findNavController().navigate(directions)
        }
        println("Cell clicked with equipment: ${data.name}")
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
                try {
                    if (isResumed) {
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

                        departmentAdapter = DepartmentAdapter(departmentsList, this.myView.context, this@DepartmentsFragment)
                        println(departmentAdapter.itemCount)
                        recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)

                        departmentCount = departmentsList.size

                        getCrews()
                    }


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
                params["empID"] = empString
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }


    private fun getCrews(){
        println("getCrews")



        var urlString = "https://www.adminmatic.com/cp/app/functions/get/departments.php"

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


                        crewAdapter = CrewAdapter(crewsList, this.myView.context, this@DepartmentsFragment)
                        println(departmentAdapter.itemCount)
                        recyclerView.adapter = crewAdapter

                        getEquipmentCrews()
                    }


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
                params["empID"] = empString
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    private fun getEquipmentCrews(){
        println("getCrews")



        var urlString = "https://www.adminmatic.com/cp/app/functions/get/equipmentList.php"

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
                        val gson = GsonBuilder().create()

                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        val equipment: JSONArray = parentObject.getJSONArray("equipment")
                        println("equipment = $equipment")
                        println("equipment count = ${equipment.length()}")


                        val equipmentList =
                            gson.fromJson(equipment.toString(), Array<Equipment>::class.java)
                                .toMutableList()

                        val equipmentCrewList = mutableListOf<EquipmentCrew>()

                        // Todo: alphabetize the list by crew name
                        // Pack the equipment into a list of equipment crew objects for the adapter
                        equipmentList.forEach { e ->
                            var found = false
                            equipmentCrewList.forEach { ec ->
                                // If the crew group already exists, add the equipment to its list
                                if (ec.name == e.crewName) {
                                    found = true
                                    ec.equips.add(e)
                                }
                            }
                            // If not, make and add it with the current equipment added to it
                            if (!found) {
                                val newEquipmentCrew =
                                    EquipmentCrew(e.crewName, e.crewColor)
                                newEquipmentCrew.equips.add(e)
                                equipmentCrewList.add(newEquipmentCrew)
                            }
                        }



                        println("EquipmentCount = ${equipmentList.count()}")
                        equipmentCount = equipmentList.size


                        equipmentCrewAdapter = EquipmentCrewAdapter(
                            equipmentCrewList,
                            this.myView.context,
                            this@DepartmentsFragment
                        )
                        println(departmentAdapter.itemCount)

                        //This is the final PHP get, so set the footer text now
                        footerText.text = getString(R.string.x_crews, crewCount)

                        hideProgressView()
                    }



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
                params["empID"] = empString
                params["plannerShow"] = "1"
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        tabLayout.visibility = View.INVISIBLE
        //currentRecyclerView.visibility = View.INVISIBLE
        // historyRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        // currentRecyclerView.visibility = View.VISIBLE
        // historyRecyclerView.visibility = View.VISIBLE
    }

}
