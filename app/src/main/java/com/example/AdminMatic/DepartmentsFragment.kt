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
import com.AdminMatic.databinding.FragmentDepartmentsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DepartmentsFragment : Fragment(), EmployeeCellClickListener, EquipmentCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var departmentAdapter:DepartmentAdapter
    lateinit var crewAdapter:CrewAdapter
    lateinit var equipmentCrewAdapter:EquipmentCrewAdapter

    lateinit var tableMode:String

    private var departmentCount:Int = 0
    private var crewCount:Int = 0
    private var equipmentCount:Int = 0
    private var employee: Employee? = null
    private var empString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    private var _binding: FragmentDepartmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        _binding = FragmentDepartmentsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        if (employee != null) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews, employee!!.fname)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val tab = binding.departmentsTableTl.getTabAt(1)
        tab!!.select()

        binding.departmentsTableTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = "DEPARTMENTS"
                        binding.recyclerView.adapter = departmentAdapter
                        binding.departmentFooterTv.text = getString(R.string.x_departments, departmentCount)
                        if (employee != null) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_departments, employee!!.fname)
                        }
                        else {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.departments)
                        }

                    }
                    1 -> {
                        binding.recyclerView.adapter = crewAdapter
                        tableMode = "CREWS"
                        binding.departmentFooterTv.text = getString(R.string.x_crews, crewCount)
                        if (employee != null) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews, employee!!.fname)
                        }
                        else {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)
                        }
                    }
                    2 -> {
                        binding.recyclerView.adapter = equipmentCrewAdapter
                        tableMode = "EQUIPMENT"
                        binding.departmentFooterTv.text = getString(R.string.x_equipment, equipmentCount)
                        if (employee != null) {
                            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_equipment, employee!!.fname)
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



        if (employee != null) {
            empString = employee!!.ID
        }

        //tableMode = "DEPARTMENTS"
        getDepartments()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("departments")
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

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val departments: JSONArray = parentObject.getJSONArray("departments")
                        println("departments = $departments")
                        println("departments count = ${departments.length()}")

                        val departmentsList =
                            gson.fromJson(departments.toString(), Array<Department>::class.java)
                                .toMutableList()
                        println("DepartmentsCount = ${departmentsList.count()}")

                        // Clear empty crews from the view
                        val depIterator = departmentsList.iterator()
                        while (depIterator.hasNext()) {
                            val dep = depIterator.next()
                            if (dep.emps!!.isEmpty()) {
                                depIterator.remove()
                            }
                        }

                        departmentAdapter = DepartmentAdapter(
                            departmentsList,
                            this.myView.context,
                            this@DepartmentsFragment
                        )
                        println(departmentAdapter.itemCount)
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)

                        departmentCount = departmentsList.size
                    }

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
                params["empID"] = empString
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "departments"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun getCrews(){
        println("getCrews")



        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")

                try {
                    val gson = GsonBuilder().create()

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val crews: JSONArray = parentObject.getJSONArray("crews")
                        println("crews = $crews")
                        println("crews count = ${crews.length()}")

                        val crewsList =
                            gson.fromJson(crews.toString(), Array<Crew>::class.java).toMutableList()


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


                        crewAdapter =
                            CrewAdapter(crewsList, this.myView.context, this@DepartmentsFragment)
                        println(departmentAdapter.itemCount)
                        binding.recyclerView.adapter = crewAdapter
                    }

                    getEquipmentCrews()


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
        postRequest1.tag = "departments"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getEquipmentCrews(){
        println("getCrews")



        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipmentList.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")

                try {
                    val gson = GsonBuilder().create()

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

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
                        binding.departmentFooterTv.text = getString(R.string.x_crews, crewCount)
                    }
                    hideProgressView()



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
        postRequest1.tag = "departments"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.INVISIBLE
        binding.departmentsTableTl.visibility = View.INVISIBLE
        //currentRecyclerView.visibility = View.INVISIBLE
        // historyRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.departmentsTableTl.visibility = View.VISIBLE
        // currentRecyclerView.visibility = View.VISIBLE
        // historyRecyclerView.visibility = View.VISIBLE
    }

}
