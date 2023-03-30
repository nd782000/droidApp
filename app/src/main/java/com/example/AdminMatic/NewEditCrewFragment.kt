package com.example.AdminMatic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditCrewBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class NewEditCrewFragment : Fragment(), AdapterView.OnItemSelectedListener, CrewEntryDelegate, CrewEntryCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private lateinit var employeesList: MutableList<Employee>
    private lateinit var departmentsList: MutableList<Department>
    private lateinit var equipmentList: MutableList<Equipment>

    private lateinit var departmentAdapter: ArrayAdapter<String>

    private var crewEntriesList: MutableList<EmployeeOrEquipment> = mutableListOf()
    private lateinit var crewArrayList:ArrayList<Crew>
    private lateinit var crewList:MutableList<Crew>

    private lateinit var crewID: String
    private var crew: Crew? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var dataLoaded = false
    private var refreshCrews = false

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            crewID = it.getString("crewID")!!
            val crewArray = it.getParcelableArray("crewList") as Array<Crew>
            crewList = crewArray.toMutableList()
        }
        if (crewID != "0") {
            editMode = true
        }
        //crewList = crewArrayList.toMutableList()
        println("crew list size: ${crewList.size}")
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("crewNewEdit")
    }

    private var _binding: FragmentNewEditCrewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditCrewBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event

                println("resetCrewsDataLoaded to send: $refreshCrews")
                setFragmentResult("newEditCrew", bundleOf("refreshCrews" to refreshCrews))


                println("handleOnBackPressed")
                if(editsMade && editsMadeDelayPassed){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_crew_bar, crewID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_crew_bar)
        }

        return myView
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgressView()

        if (dataLoaded) {
            layoutViews()
        }
        else {
            if (editMode) {
                getCrew()
            } else {
                getEmployees()
            }
        }

    }

    private fun getCrew(){
        println("getEmployees")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/crew.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("get crew response: $response")
                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val crewJSON = parentObject.getJSONObject("crew")

                        val gson = GsonBuilder().create()
                        crew = gson.fromJson(crewJSON.toString(), Crew::class.java)

                        getEmployees()
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("error in get/crew")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["crewID"] = crewID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getEmployees(){
        println("getEmployees")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("get employees response: $response")
                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val employees: JSONArray = parentObject.getJSONArray("employees")

                        val gson = GsonBuilder().create()
                        employeesList =
                            gson.fromJson(employees.toString(), Array<Employee>::class.java)
                                .toMutableList()

                        getDepartments()
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("error in get/departments")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getDepartments(){
        println("getDepartments")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("get departments response: $response")
                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val departments: JSONArray = parentObject.getJSONArray("departments")

                        val gson = GsonBuilder().create()
                        departmentsList =
                            gson.fromJson(departments.toString(), Array<Department>::class.java)
                                .toMutableList()

                        getEquipment()
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("error in get/departments")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getEquipment(){
        println("getDepartments")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipmentList.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("get departments response: $response")
                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val equipment: JSONArray = parentObject.getJSONArray("equipment")

                        val gson = GsonBuilder().create()
                        equipmentList =
                            gson.fromJson(equipment.toString(), Array<Equipment>::class.java)
                                .toMutableList()

                        layoutViews()
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("error in get/equipment")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {
        if (!editMode) {
            crew = Crew("0", "")
            crew!!.color = GlobalVars.colorArray[0]
            crew!!.status = "1"
        }

        // Flag edits made false after all the views have time to set their states
        Timer("CrewEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }


        // Title
        binding.nameEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.nameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Department Spinner
        val departmentStringsList = mutableListOf<String>()
        departmentsList.forEach {
            departmentStringsList.add(it.name)
        }
        departmentAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentStringsList
        )
        binding.departmentSpinner.adapter = departmentAdapter
        binding.departmentSpinner.onItemSelectedListener = this@NewEditCrewFragment

        if (editMode) {
            for (i in 0 until departmentsList.size) {
                if (departmentsList[i].ID == crew!!.dep) {
                    binding.departmentSpinner.setSelection(i, false)
                }
            }
        }


        // Color view
        binding.colorView.setOnClickListener {
            showColorMenu()
        }


        // Add Employee Button
        binding.addEmployeeBtn.setOnClickListener {
            val popUp = PopupMenu(myView.context, binding.addEmployeeBtn)
            popUp.inflate(R.menu.task_status_menu)

            for (i in 0 until employeesList.size) {
                popUp.menu.add(0, i, 1, employeesList[i].name)
            }

            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                val empToAdd = employeesList[item!!.itemId]
                println("${empToAdd.name} clicked")

                crew!!.emps!!.forEach {
                    if (it.ID == empToAdd.ID) {
                        globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.entry_already_in_crew, empToAdd.name))
                        return@setOnMenuItemClickListener true
                    }
                }

                val existingCrewNames = mutableListOf<String>()
                var existingCrewName = ""

                crewList.forEach {
                    if (it.ID != "0") { // skip unassigned crew
                        it.emps!!.forEach { emp ->
                            if (emp.ID == empToAdd.ID) {
                                existingCrewNames.add(it.name)
                            }
                        }
                    }
                }

                if (existingCrewNames.isNotEmpty()) {
                    existingCrewNames.forEach {
                        if (existingCrewName == "") {
                            existingCrewName = it
                        }
                        else {
                            existingCrewName += ", $it"
                        }
                    }


                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.employee_already_in_crew_title))
                    builder.setMessage(getString(R.string.employee_already_in_crew_body, empToAdd.name, existingCrewName))
                    builder.setPositiveButton(getString(R.string.move)) { _, _ ->
                        crewMove("0", crew!!.ID, EmployeeOrEquipment(empToAdd), false)
                    }
                    builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->

                    }
                    builder.setNegativeButton(getString(R.string.copy)) { _, _ ->
                        crewMove("0", crew!!.ID, EmployeeOrEquipment(empToAdd), true)
                    }
                    builder.show()
                }
                else { // not found in any other crews
                    crewMove("0", crew!!.ID, EmployeeOrEquipment(empToAdd), false)
                }





                true
            }

            popUp.show()
        }

        // Add Equipment Button
        binding.addEquipmentBtn.setOnClickListener {
            val popUp = PopupMenu(myView.context, binding.addEquipmentBtn)
            popUp.inflate(R.menu.task_status_menu)

            for (i in 0 until equipmentList.size) {
                popUp.menu.add(0, i, 1, equipmentList[i].name)
            }

            popUp.setOnMenuItemClickListener { item: MenuItem? ->
                val equipToAdd = equipmentList[item!!.itemId]
                println("${equipmentList[item!!.itemId].name} clicked")

                crew!!.equipment!!.forEach {
                    if (it.ID == equipToAdd.ID) {
                        globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.entry_already_in_crew, equipToAdd.name))
                        return@setOnMenuItemClickListener true
                    }
                }

                var existingCrewName = ""

                crewList.forEach {
                    if (it.ID != "0") { //skip unassigned crew
                        it.equipment!!.forEach { equip ->
                            if (equip.ID == equipToAdd.ID) {
                                existingCrewName = it.name
                            }
                        }
                    }
                }

                if (existingCrewName.isNotEmpty()) {

                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.equipment_already_in_crew_title))
                    builder.setMessage(getString(R.string.equipment_already_in_crew_body, equipToAdd.name, existingCrewName))
                    builder.setPositiveButton(getString(R.string.move)) { _, _ ->
                        crewMove("0", crew!!.ID, EmployeeOrEquipment(equipToAdd), false)
                    }
                    builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->

                    }
                    builder.show()
                }
                else { // not found in any other crews
                    crewMove("0", crew!!.ID, EmployeeOrEquipment(equipToAdd), false)
                }

                true
            }



            popUp.show()
        }


        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateCrew()
            }
        }



        // ===== POPULATE FIELDS =====
        if (editMode) {
            binding.nameEt.setText(crew!!.name)
        }

        print("crew color: ${crew!!.color}")
        print("crew color: ${crew!!.subcolor}")

        if (!editMode) {
            crew!!.subcolor = GlobalVars.colorArray[0]
            binding.recyclerView.visibility = View.INVISIBLE
            binding.buttonsLayout.visibility = View.INVISIBLE

            binding.departmentSpinner.setSelection(0)
        }

        binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(crew!!.subcolor!!))

        editsMade = false

        if (editMode) {
            buildCrewEntriesList()
        }
        else {
            binding.recyclerContainer.visibility = View.GONE
            binding.buttonsLayout.visibility = View.GONE
        }
        dataLoaded = true
        hideProgressView()
    }

    private fun buildCrewEntriesList() {
        crewEntriesList.clear()

        crew!!.emps!!.forEach {
            println("added an emp")
            crewEntriesList.add(EmployeeOrEquipment(it))
        }
        crew!!.equipment!!.forEach {
            println("added an equip")
            crewEntriesList.add(EmployeeOrEquipment(it))
        }

        if (dataLoaded) {
            binding.recyclerView.adapter!!.notifyDataSetChanged()
            //val adapter = CrewEntriesAdapter(crewEntriesList, -1, crew!!.ID, this, this)
            //binding.recyclerView.layoutManager = LinearLayoutManager(com.example.AdminMatic.myView.context, LinearLayoutManager.VERTICAL, false)
            //binding.recyclerView.adapter = adapter
        }
        else {
            val adapter = CrewEntriesAdapter(crewEntriesList, -1, crew!!.ID, this, this)

            //holder.mRecycler!!.setHasFixedSize(true)
            binding.recyclerView.layoutManager = LinearLayoutManager(com.example.AdminMatic.myView.context, LinearLayoutManager.VERTICAL, false)
            binding.recyclerView.adapter = adapter
            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(com.example.AdminMatic.myView.context, DividerItemDecoration.VERTICAL)
            binding.recyclerView.addItemDecoration(itemDecoration)
        }
    }

    private fun validateFields(): Boolean {


        /*
        if (contract!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        if (contract!!.title.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_wo_set_title))
            return false
        }

        //Skipping charge type and payment terms checking here since droid spinners can't be empty so it defaults to the first option

        if (contract!!.salesRep.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_contract_select_sales_rep))
            return false
        }

         */

        return true
    }

    private fun updateCrew() {
        println("updateCrew")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/crew.php"

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
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        /*
                        val gson = GsonBuilder().create()
                        val newID: String = gson.fromJson(parentObject["contractID"].toString(), String::class.java)
                        contract!!.ID = newContractID
                         */
                        globalVars.playSaveSound(myView.context)
                        refreshCrews = true
                        editsMade = false

                        setFragmentResult("newEditCrew", bundleOf("refreshCrews" to refreshCrews))
                        myView.findNavController().navigateUp()

                    }
                    hideProgressView()


                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["ID"] = crew!!.ID
                params["name"] = binding.nameEt.text.toString()
                params["status"] = crew!!.status.toString()
                params["dep"] = crew!!.dep.toString()
                params["depName"] = crew!!.depName.toString()
                params["subcolor"] = crew!!.subcolor.toString()
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    @SuppressLint("Range")
    private fun showColorMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.colorView)
        popUp.inflate(R.menu.task_status_menu)

        for (i in 0 until GlobalVars.colorArray.size) {
            popUp.menu.add(0, i, 1,globalVars.menuColor(GlobalVars.colorArray[i]))
            popUp.setOnMenuItemClickListener { item: MenuItem? ->
                binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(GlobalVars.colorArray[item!!.itemId]))
                crew!!.subcolor = GlobalVars.colorArray[item.itemId]

                editsMade = true

                true
            }
        }

        popUp.gravity = Gravity.START
        popUp.show()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true

        crew!!.dep = departmentsList[position].ID
        crew!!.depName = departmentsList[position].name

        /*
        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                val positionPlus1 = position+1
                contract!!.chargeType = positionPlus1.toString()
            }
        }
         */

    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }


    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun onMoveCrewEntry(entry: EmployeeOrEquipment, crewIndex: Int, viewAnchor: View) {
        println("move crew entry")
    }

    override fun onUnassignCrewEntry(entry: EmployeeOrEquipment, crewIndex: Int) {
        println("unassign crew entry")

        crewMove(crew!!.ID, "0", entry, false)



    }

    override fun onCrewEntryCellClickListener(data: EmployeeOrEquipment) {
        if (data.isEquipment) {
            println("${data.equipment!!.name} tapped")
            val directions = NewEditCrewFragmentDirections.navigateToEquipment(data.equipment!!)
            myView.findNavController().navigate(directions)
        }
        else {
            println("${data.employee!!.name} tapped")
            val directions = NewEditCrewFragmentDirections.navigateToEmployee(data.employee)
            myView.findNavController().navigate(directions)
        }
    }

    private fun crewMove(oldCrew:String, newCrew:String, entry:EmployeeOrEquipment, copy:Boolean) {
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/crewMove.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("get crew response: $response")
                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        if (newCrew == "0") { //unassign
                            if (entry.isEquipment) {
                                for (i in 0 until crew!!.equipment!!.size) {
                                    if (crew!!.equipment!![i].ID == entry.equipment!!.ID)  {
                                        print ("found it")
                                        val tempML = crew!!.equipment!!.toMutableList()
                                        tempML.removeAt(i)
                                        crew!!.equipment = tempML.toTypedArray()
                                        break
                                    }
                                }
                            }
                            else {
                                for (i in 0 until crew!!.emps!!.size) {
                                    if (crew!!.emps!![i].ID == entry.employee!!.ID)  {
                                        print ("found it")
                                        val tempML = crew!!.emps!!.toMutableList()
                                        tempML.removeAt(i)
                                        crew!!.emps = tempML.toTypedArray()
                                        break
                                    }
                                }
                            }
                            refreshCrews = true
                            buildCrewEntriesList()
                        }
                        else { // copy or move
                            if (entry.isEquipment) {
                                val tempML = crew!!.equipment!!.toMutableList()
                                tempML.add(entry.equipment!!)
                                crew!!.equipment = tempML.toTypedArray()
                            }
                            else { // is employee
                                val tempML = crew!!.emps!!.toMutableList()
                                tempML.add(entry.employee!!)
                                crew!!.emps = tempML.toTypedArray()
                            }
                            refreshCrews = true
                            buildCrewEntriesList()
                        }

                        globalVars.playSaveSound(myView.context)

                        println("crew move successful")
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("error in get/crew")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["oldCrew"] = oldCrew
                params["newCrew"] = newCrew
                if (copy) {
                    params["copy"] = "1"
                }
                if (entry.isEquipment) {
                    params["equipmentID"] = entry.equipment!!.ID
                }
                else {
                    params["employeeID"] = entry.employee!!.ID
                }
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

}