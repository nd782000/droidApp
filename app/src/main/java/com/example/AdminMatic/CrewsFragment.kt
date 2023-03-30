package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCrewsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

interface CrewCellClickListener {
    fun onCrewCellClickListener(data:CrewSection)
}

interface CrewEntryCellClickListener {
    fun onCrewEntryCellClickListener(data:EmployeeOrEquipment)
}

interface CrewEntryDelegate {
    fun onMoveCrewEntry(entry:EmployeeOrEquipment, crewIndex:Int, viewAnchor:View)
    fun onUnassignCrewEntry(entry:EmployeeOrEquipment, crewIndex:Int)
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

class CrewsFragment : Fragment(), CrewCellClickListener, CrewEntryCellClickListener, CrewEntryDelegate, AdapterView.OnItemSelectedListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var crewsAdapter:CrewsAdapter
    private lateinit var departmentAdapter: ArrayAdapter<String>

    private var crewList:MutableList<Crew> = mutableListOf()
    private var crewSections:MutableList<CrewSection> = mutableListOf()

    private var departmentValue = "0"
    private var dateValueDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var dateValue = ""

    private var dataLoaded = false
    private var viewsLaidOut = false

    private var _binding: FragmentCrewsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        _binding = FragmentCrewsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.crews)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("Data loaded in onViewCreate: $dataLoaded")

        if (!dataLoaded) {
            getCrews()
        }
        else {
            layoutViews()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("newEditCrew") { _, bundle ->
            println("FRAGMENTRESULTLISTENER")
            val refreshCrews = bundle.getBoolean("refreshCrews")
            println("Data loaded in onCreate FragmentResult Listener: $dataLoaded")
            if (refreshCrews) {
                println("REFRESH CREWS")
                getCrews()
            }
        }
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

                        val crewlessEmpsJSON: JSONArray = parentObject.getJSONArray("employees")
                        val crewlessEmps = gson.fromJson(crewlessEmpsJSON.toString(), Array<Employee>::class.java)
                        val unassignedEmps = Crew("0", "Unassigned")
                        unassignedEmps.emps = crewlessEmps
                        unassignedEmps.color = "#000000"
                        unassignedEmps.subcolor = unassignedEmps.color

                        val crewlessEquipmentJSON: JSONArray = parentObject.getJSONArray("equipment")
                        val crewlessEquipment = gson.fromJson(crewlessEquipmentJSON.toString(), Array<Equipment>::class.java)
                        unassignedEmps.equipment = crewlessEquipment

                        crewList.add(unassignedEmps)

                        println("NUMBER OF CREWS IN CREWLIST: ${crewList.size}")

                        dataLoaded = true

                        createSections()

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
                params["department"] = departmentValue
                params["active"] = "1"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crews"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun createSections() {
        crewSections.clear()

        crewList.forEach{

            if (it.subcolor == null) {
                it.subcolor = it.color
            }

            val crewSection = CrewSection(it.name, it.ID, it.subcolor!!)

            it.emps!!.forEach { emp ->
                crewSection.entries.add(EmployeeOrEquipment(emp))
            }

            it.equipment!!.forEach { equip ->
                crewSection.entries.add(EmployeeOrEquipment(equip))
            }

            crewSections.add(crewSection)
        }

        if (!viewsLaidOut) {
            layoutViews()
        }
        else {
            binding.recyclerView.adapter!!.notifyDataSetChanged()
        }

    }

    private fun layoutViews() {

        // Date Picker
        binding.dateEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
            datePicker.showDialog(dateValueDate.year, dateValueDate.monthValue-1, dateValueDate.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    val selectedDate = LocalDate.of(year, month+1, dayOfMonth)

                    if (selectedDate < LocalDate.now(ZoneOffset.UTC)) {
                        globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.dialogue_date_in_past))
                        return
                    }

                    if (selectedDate != dateValueDate) {
                        binding.dateEt.setText(selectedDate.format(GlobalVars.dateFormatterShort))
                        dateValue = if (selectedDate == LocalDate.now(ZoneOffset.UTC)) {
                            ""
                        } else {
                            selectedDate.format(GlobalVars.dateFormatterYYYYMMDD)
                        }

                        dateValueDate = selectedDate
                        getCrews()
                    }
                }
            })
        }

        binding.dateEt.setText(dateValueDate.format(GlobalVars.dateFormatterShort))




        // Department Picker
        val departmentNameList = mutableListOf<String>()
        GlobalVars.departments!!.forEach {
            if (it.ID == "0") {
                departmentNameList.add(getString(R.string.all_departments))
            }
            else {
                departmentNameList.add(it.name)
            }
        }

        departmentAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentNameList
        )
        binding.departmentSpinner.adapter = departmentAdapter
        binding.departmentSpinner.onItemSelectedListener = this@CrewsFragment

        crewsAdapter = CrewsAdapter(
            crewSections,
            this@CrewsFragment,
            this@CrewsFragment,
            this@CrewsFragment
        )



        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(
                myView.context,
                DividerItemDecoration.VERTICAL
            )
        binding.recyclerView.addItemDecoration(itemDecoration)



        println(crewSections.count()-1)
        binding.recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = crewsAdapter



        binding.departmentFooterTv.text = getString(R.string.x_active_crews, crewSections.count() - 1)

        binding.newCrewBtn.setOnClickListener {
            val directions = CrewsFragmentDirections.navigateToNewEditCrew("0", crewList.toTypedArray())
            myView.findNavController().navigate(directions)
        }

        viewsLaidOut = true

        hideProgressView()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")

        when (parent!!.id) {
            R.id.department_spinner -> {
                if (departmentValue != GlobalVars.departments?.get(position)?.ID.toString()) {
                    departmentValue = GlobalVars.departments?.get(position)?.ID.toString()
                    println("departmentValue $departmentValue")
                    getCrews()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onCrewCellClickListener(data: CrewSection) {
        println("crew ${data.name} tapped")

        val directions = CrewsFragmentDirections.navigateToNewEditCrew(data.ID, crewList.toTypedArray())
        myView.findNavController().navigate(directions)

    }

    override fun onCrewEntryCellClickListener(data: EmployeeOrEquipment) {
        if (data.isEquipment) {
            println("${data.equipment!!.name} tapped")
            val directions = CrewsFragmentDirections.navigateToEquipment(data.equipment!!)
            myView.findNavController().navigate(directions)
        }
        else {
            println("${data.employee!!.name} tapped")
            val directions = CrewsFragmentDirections.navigateToEmployee(data.employee)
            myView.findNavController().navigate(directions)
        }

    }

    override fun onMoveCrewEntry(entry: EmployeeOrEquipment, crewIndex: Int, viewAnchor:View) {
        val popUp = PopupMenu(myView.context, viewAnchor)
        popUp.inflate(R.menu.task_status_menu)

        val existingID = crewList[crewIndex].ID

        crewList.forEach {
            if (existingID != it.ID && it.ID != "0") { // Skip adding the crew we're already in and the unassigned crew to the list
                popUp.menu.add(0, it.ID.toInt(), 1, it.name)
            }
        }

        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/crewMove.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response
                    println("move crew response $response")
                    try {
                        //val gson = GsonBuilder().create()


                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                            if (entry.isEquipment) {
                                for (i in 0 until crewList[crewIndex].equipment!!.size) {

                                    val checkedElement = crewList[crewIndex].equipment!![i]

                                    if (entry.equipment!!.ID == checkedElement.ID) {
                                        println("Found the source element")

                                        // Find the target crew
                                        crewList.forEach {
                                            if (it.ID == item!!.itemId.toString()) {
                                                println("Found the target crew: ${it.name}")
                                                // Add the equipment to the target crew
                                                val unassignedEquipMutable = it.equipment!!.toMutableList()
                                                unassignedEquipMutable.add(checkedElement)
                                                it.equipment = unassignedEquipMutable.toTypedArray()
                                            }
                                        }


                                        // Remove the equipment from the current crew
                                        val equipMutable = crewList[crewIndex].equipment!!.toMutableList()
                                        equipMutable.removeAt(i)
                                        crewList[crewIndex].equipment = equipMutable.toTypedArray()

                                        createSections()
                                        break
                                    }
                                }
                            }
                            else { // is employee
                                for (i in 0 until crewList[crewIndex].emps!!.size) {

                                    val checkedElement = crewList[crewIndex].emps!![i]

                                    if (entry.employee!!.ID == checkedElement.ID) {
                                        println("Found the source element")

                                        // Find the target crew
                                        crewList.forEach {
                                            if (it.ID == item!!.itemId.toString()) {
                                                println("Found the target crew: ${it.name}")
                                                // Add the equipment to the target crew
                                                val unassignedEmpsMutable = it.emps!!.toMutableList()
                                                unassignedEmpsMutable.add(checkedElement)
                                                it.emps = unassignedEmpsMutable.toTypedArray()
                                            }
                                        }


                                        // Remove the equipment from the current crew
                                        val empsMutable = crewList[crewIndex].emps!!.toMutableList()
                                        empsMutable.removeAt(i)
                                        crewList[crewIndex].emps = empsMutable.toTypedArray()

                                        createSections()
                                        break
                                    }
                                }
                            }

                            globalVars.playSaveSound(myView.context)

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
                    if (entry.isEquipment) {
                        params["equipmentID"] = entry.equipment!!.ID
                    }
                    else {
                        params["employeeID"] = entry.employee!!.ID
                    }
                    if (dateValue != "") {
                        params["day"] = dateValue
                    }
                    params["oldCrew"] = existingID
                    params["newCrew"] = item!!.itemId.toString()
                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "crews"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

            true
        }



        popUp.show()
    }

    override fun onUnassignCrewEntry(entry: EmployeeOrEquipment, crewIndex: Int) {

        println(crewIndex)
        println(crewList.size)

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/crewMove.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Unassign crew response $response")
                try {
                    //val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        if (entry.isEquipment) {
                            for (i in 0 until crewList[crewIndex].equipment!!.size) {
                                val checkedElement = crewList[crewIndex].equipment!![i]
                                if (entry.equipment!!.ID == checkedElement.ID) {
                                    // Add the equipment to the unassigned crew
                                    val unassignedEquipMutable = crewList.last().equipment!!.toMutableList()
                                    unassignedEquipMutable.add(checkedElement)
                                    crewList.last().equipment = unassignedEquipMutable.toTypedArray()

                                    // Remove the equipment from the current crew
                                    val equipMutable = crewList[crewIndex].equipment!!.toMutableList()
                                    equipMutable.removeAt(i)
                                    crewList[crewIndex].equipment = equipMutable.toTypedArray()

                                    createSections()
                                    break
                                }
                            }
                        }
                        else { // is employee
                            for (i in 0 until crewList[crewIndex].emps!!.size) {
                                val checkedElement = crewList[crewIndex].emps!![i]
                                if (entry.employee!!.ID == checkedElement.ID) {
                                    // Add the equipment to the unassigned crew
                                    val unassignedEmpsMutable = crewList.last().emps!!.toMutableList()
                                    unassignedEmpsMutable.add(checkedElement)
                                    crewList.last().emps = unassignedEmpsMutable.toTypedArray()

                                    // Remove the equipment from the current crew
                                    val empMutable = crewList[crewIndex].emps!!.toMutableList()
                                    empMutable.removeAt(i)
                                    crewList[crewIndex].emps = empMutable.toTypedArray()

                                    createSections()
                                    break
                                }
                            }
                        }

                        globalVars.playSaveSound(myView.context)

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
                if (entry.isEquipment) {
                    params["equipmentID"] = entry.equipment!!.ID
                }
                else {
                    params["employeeID"] = entry.employee!!.ID
                }
                if (dateValue != "") {
                    params["day"] = dateValue
                }
                params["oldCrew"] = crewSections[crewIndex].ID
                params["newCrew"] = "0"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crews"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
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
