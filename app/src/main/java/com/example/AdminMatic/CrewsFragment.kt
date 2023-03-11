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
import com.AdminMatic.databinding.FragmentDepartmentsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface CrewCellClickListener {
    fun onCrewCellClickListener(data:CrewSection)
}

interface CrewEntryCellClickListener {
    fun onCrewEntryCellClickListener(data:EmployeeOrEquipment)
}

class EmployeeOrEquipment {
    var isEquipment = false
    var employee: Employee? = null
    var equipment: Equipment? = null

    constructor(_employee:Employee) {
        employee = _employee
    }

    constructor(_equipment:Equipment) {
        equipment = _equipment
        isEquipment = true
    }
}

class CrewSection(_name: String, _ID: String, _color: String) {
    var name: String = _name
    var color: String = _color
    var ID: String = _ID
    var entries: MutableList<EmployeeOrEquipment> = mutableListOf()
}

class CrewsFragment : Fragment(), CrewCellClickListener, CrewEntryCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var crewsAdapter:CrewsAdapter

    private var crewList:MutableList<Crew> = mutableListOf()

    private var departmentValue = "0"
    private var dateValue = "0"

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

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newDepartmentBtn.setOnClickListener {
            val directions = DepartmentsFragmentDirections.navigateToNewEditDepartment(null)
            myView.findNavController().navigate(directions)
        }

        getCrews()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("crews")
    }

    private fun getCrews(){
        println("getCrews")

        showProgressView()

        crewList.clear()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/crews.php"

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

                        val crewArrayTemp = gson.fromJson(crews.toString(), Array<Crew>::class.java)
                        println("crewArray count = ${crewArrayTemp.count()}")


                        crewArrayTemp.forEach {
                            if (it.subcolor == "" || it.subcolor == null) {
                                it.subcolor = it.color
                            }

                            it.emps!!.forEach { emp ->
                                emp.crewName = it.name
                                emp.crewColor = it.color
                            }

                            it.equipment!!.forEach { equip ->
                                equip.crewName = it.name
                                equip.crewColor = it.color
                            }

                            crewList.add(it)
                        }

                        val crewlessEmpsJSON: JSONArray = parentObject.getJSONArray("emps")
                        val crewlessEmps = gson.fromJson(crewlessEmpsJSON.toString(), Array<Employee>::class.java)
                        val unassignedEmps = Crew("0", "Unassigned")
                        unassignedEmps.emps = crewlessEmps
                        unassignedEmps.color = "#000000"
                        unassignedEmps.subcolor = unassignedEmps.color

                        val crewlessEquipmentJSON: JSONArray = parentObject.getJSONArray("equipment")
                        val crewlessEquipment = gson.fromJson(crewlessEmpsJSON.toString(), Array<Equipment>::class.java)
                        unassignedEmps.equipment = crewlessEquipment

                        crewList.add(unassignedEmps)

                        createSections()

                        /* MOVE TO BUILD SECTIONS
                        crewsAdapter = CrewsAdapter(
                            departmentsList,
                            this.myView.context,
                            this@DepartmentsFragment
                        )



                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(
                                myView.context,
                                DividerItemDecoration.VERTICAL
                            )
                        binding.recyclerView.addItemDecoration(itemDecoration)



                        println(departmentAdapter.itemCount)
                        binding.recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.recyclerView.adapter = departmentAdapter




                        binding.departmentFooterTv.text = getString(R.string.x_active_departments, departmentCount)

                         */



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
                params["active"] = "1"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crews"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun createSections() {

    }

    override fun onCrewCellClickListener(data: CrewSection) {
        println("department ${data.name} tapped")


    }

    override fun onCrewEntryCellClickListener(data: EmployeeOrEquipment) {
        if (data.isEquipment) {
            println("${data.equipment!!.name} tapped")
        }
        else {
            println("${data.employee!!.name} tapped")
        }

        /*
        val directions = DepartmentsFragmentDirections.navigateToNewEditDepartment(data)
        myView.findNavController().navigate(directions)
         */
    }

    fun showProgressView() {
        binding.allCl.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.allCl.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

}
