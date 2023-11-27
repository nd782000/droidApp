package com.example.AdminMatic

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditEquipmentBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.set
import kotlin.concurrent.schedule

class NewEditEquipmentFragment : Fragment(), AdapterView.OnItemSelectedListener, VendorCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private lateinit var equipment: Equipment

    private var dataLoaded = false

    private lateinit var typeAdapter: ArrayAdapter<String>
    private lateinit var crewAdapter: ArrayAdapter<String>
    private lateinit var fuelAdapter: ArrayAdapter<String>
    private lateinit var engineAdapter: ArrayAdapter<String>
    private lateinit var usageTypeAdapter: ArrayAdapter<String>

    private var typeNameArray = mutableListOf<String>()
    private var typeIDArray = mutableListOf<String>()
    private var crewNameArray = mutableListOf<String>()
    private var crewIDArray = mutableListOf<String>()
    private var fuelNameArray = mutableListOf<String>()
    private var fuelIDArray = mutableListOf<String>()
    private var engineNameArray = mutableListOf<String>()
    private var engineIDArray = mutableListOf<String>()
    private var usageTypeArray = mutableListOf<String>()
    private var usageTypeIDArray = mutableListOf<String>()

    private var selectedDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

    private var vendorList = mutableListOf<Vendor>()

    private var shouldRefreshEquipment = false

    lateinit var globalVars:GlobalVars
    lateinit var myView:View
    private var editMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val equipmentTemp:Equipment? = it.getParcelable("equipment")
            if (equipmentTemp != null) {
                equipment = equipmentTemp
                editMode = true
            }
            else {
                equipment = Equipment("0","","0", "", "1")
            }
        }

        setFragmentResultListener("_refreshEquipment") { _, bundle ->
            shouldRefreshEquipment = bundle.getBoolean("_refreshEquipment")
            println("Got fragmentresult of $shouldRefreshEquipment")

            showProgressView()

            if (shouldRefreshEquipment) {
                getEquipment()
            }
            else {
                getEquipmentFields()
            }

        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("equipmentNewEdit")
    }

    private var _binding: FragmentNewEditEquipmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditEquipmentBinding.inflate(inflater, container, false)
        myView = binding.root

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_equipment_num, equipment.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_equipment)
        }
        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flag edits made false after all the views have time to set their states
        Timer("EquipmentEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        binding.picIv.setOnClickListener {
            if (equipment.ID == "0") {
                globalVars.simpleAlert(myView.context,getString(R.string.save_equipment_first_title),getString(R.string.save_equipment_first_body))
            }
            else {
                //if (equipment.pic == "0") {
                    val directions = NewEditEquipmentFragmentDirections.navigateToImageUpload(
                        "EQUIPMENT",
                        arrayOf(), "", "", "", "", "", "", "0", "", "", "", equipment.ID, ""
                    )
                    myView.findNavController().navigate(directions)
               // }
               // else {

               // }
            }
        }

        binding.nameEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.nameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.makeEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.makeEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.serialEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.serialEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Purchase Date
        binding.purchaseDateEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(selectedDate.year, selectedDate.monthValue-1, selectedDate.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    selectedDate = LocalDate.of(year, month+1, dayOfMonth)
                    binding.purchaseDateEt.setText(selectedDate.format(GlobalVars.dateFormatterShort))
                    equipment.purchaseDate = selectedDate.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        binding.showInPlannersSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                equipment.plannerShow = "1"
            }
            else {
                equipment.plannerShow = "0"
            }
        }

        binding.descriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.descriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.typeSpinner.onItemSelectedListener = this
        binding.crewSpinner.onItemSelectedListener = this
        binding.fuelSpinner.onItemSelectedListener = this
        binding.engineSpinner.onItemSelectedListener = this
        binding.usageTypeSpinner.onItemSelectedListener = this

        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateEquipment()
            }
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true

        if (dataLoaded) {

            when (parent!!.id) {
                R.id.type_spinner -> {
                    equipment.type = typeIDArray[position]
                    equipment.typeName = typeNameArray[position]
                    println("setting type to ${typeIDArray[position]} ${typeNameArray[position]}")
                }
                R.id.crew_spinner -> {
                    equipment.crew = crewIDArray[position]
                    equipment.crewName = crewNameArray[position]
                    println("setting crew to ${crewIDArray[position]} ${crewNameArray[position]}")
                }
                R.id.fuel_spinner -> {
                    equipment.fuelType = fuelIDArray[position]
                    equipment.fuelTypeName = fuelNameArray[position]
                    println("setting fuelType to ${fuelIDArray[position]} ${fuelNameArray[position]}")
                }
                R.id.engine_spinner -> {
                    equipment.engineType = engineIDArray[position]
                    equipment.engineTypeName = engineNameArray[position]
                    println("setting engineType to ${engineIDArray[position]} ${engineNameArray[position]}")
                }
                R.id.usage_type_spinner -> {
                    equipment.usageType = usageTypeIDArray[position]
                    //equipment.engineTypeName = engineNameArray[position]
                    println("setting usage type to ${usageTypeIDArray[position]}")
                }
            }
        }
    }

    private fun getEquipment(){
        println("getEquipment")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipment.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Get equipemnt response $response")
                try {
                    val gson = GsonBuilder().create()

                    val parentObject = JSONObject(response)
                    println("Get equipment parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val equipmentJSON: JSONObject = parentObject.getJSONObject("equipment")

                        //val equipmentNew = gson.fromJson(equipmentJSON.toString(), Equipment::class.java)
                        //equipment!!.status = equipmentNew.status
                        equipment = gson.fromJson(equipmentJSON.toString(), Equipment::class.java)

                        getEquipmentFields()

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
                params["equipmentID"] = equipment.ID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getEquipmentFields(){
        println("getEquipmentFields")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipmentFields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Get equipment fields response $response")
                try {
                    val gson = GsonBuilder().create()

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val types: JSONArray = parentObject.getJSONArray("types")
                        val fuelTypes: JSONArray = parentObject.getJSONArray("fuelTypes")
                        val engineTypes: JSONArray = parentObject.getJSONArray("engineTypes")

                        val typesList = gson.fromJson(types.toString(), Array<EquipmentField>::class.java).toMutableList()
                        val fuelTypesList = gson.fromJson(fuelTypes.toString(), Array<EquipmentField>::class.java).toMutableList()
                        val engineTypesList = gson.fromJson(engineTypes.toString(), Array<EquipmentField>::class.java).toMutableList()

                        println("Types size: ${typesList.size}")
                        println("Fuel size: ${fuelTypesList.size}")
                        println("Engine size: ${engineTypesList.size}")

                        typeNameArray.clear()
                        typeIDArray.clear()
                        fuelNameArray.clear()
                        fuelIDArray.clear()
                        engineNameArray.clear()
                        engineIDArray.clear()

                        typesList.forEach {
                            typeNameArray.add(it.name)
                            typeIDArray.add(it.ID)
                        }

                        typeAdapter = ArrayAdapter<String>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item,
                            typeNameArray
                        )
                        binding.typeSpinner.adapter = typeAdapter
                        binding.typeSpinner.setBackgroundResource(R.drawable.text_view_layout)

                        fuelTypesList.forEach {
                            fuelNameArray.add(it.name)
                            fuelIDArray.add(it.ID)
                        }

                        fuelAdapter = ArrayAdapter<String>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item,
                            fuelNameArray
                        )
                        binding.fuelSpinner.adapter = fuelAdapter
                        binding.fuelSpinner.setBackgroundResource(R.drawable.text_view_layout)

                        engineTypesList.forEach {
                            engineNameArray.add(it.name)
                            engineIDArray.add(it.ID)
                        }

                        engineAdapter = ArrayAdapter<String>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item,
                            engineNameArray
                        )
                        binding.engineSpinner.adapter = engineAdapter
                        binding.engineSpinner.setBackgroundResource(R.drawable.text_view_layout)

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
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getCrews(){
        println("getCrews")

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

                        crewNameArray.clear()
                        crewIDArray.clear()

                        crewNameArray.add(getString(R.string.no_crew))
                        crewIDArray.add("0")

                        crewArrayTemp.forEach {
                            crewNameArray.add(it.name)
                            crewIDArray.add(it.ID)
                        }

                        crewAdapter = ArrayAdapter<String>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item,
                            crewNameArray
                        )
                        binding.crewSpinner.adapter = crewAdapter
                        binding.crewSpinner.setBackgroundResource(R.drawable.text_view_layout)

                        getVendors()
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
                //params["department"] =
                params["active"] = "1"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getVendors(){
        println("getVendors")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/vendors.php"

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

                        val vendors: JSONArray = parentObject.getJSONArray("vendors")
                        println("vendors = $vendors")
                        println("vendors count = ${vendors.length()}")

                        val gson = GsonBuilder().create()
                        vendorList = gson.fromJson(vendors.toString(), Array<Vendor>::class.java).toMutableList()

                        vendorList.forEach {
                            if (it.itemString == null) {
                                it.itemString = ""
                            }
                        }

                        populateFields()

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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun populateFields() {

        usageTypeArray.add(getString(R.string.select_logging_type))
        usageTypeArray.add(getString(R.string.miles))
        usageTypeArray.add(getString(R.string.kilometers))
        usageTypeArray.add(getString(R.string.hours))
        usageTypeIDArray.add("")
        usageTypeIDArray.add("miles")
        usageTypeIDArray.add("km")
        usageTypeIDArray.add("hours")

        usageTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            usageTypeArray
        )
        binding.usageTypeSpinner.adapter = usageTypeAdapter
        binding.usageTypeSpinner.setBackgroundResource(R.drawable.text_view_layout)

        // Fill in default equipment data
        if (!editMode) {
            equipment.engineType = engineIDArray[0]
            equipment.type = typeIDArray[0]
            equipment.crew = crewIDArray[0]
            equipment.fuelType = fuelIDArray[0]
            equipment.purchaseDate = selectedDate.format(GlobalVars.dateFormatterYYYYMMDD)
            equipment.dealer = ""
        }

        println("populateFields")
        dataLoaded = true

        if (equipment.image != null) {
            Picasso.with(context)
                .load(GlobalVars.thumbBase + equipment.image!!.fileName)
                .placeholder(R.drawable.ic_images)
                .into(binding.picIv)
        }

        println("equipment type: ${equipment.type}")
        println("selection: ${typeIDArray.indexOf(equipment.type)}")
        typeIDArray.forEach {
            println(it)
        }
        binding.typeSpinner.setSelection(typeIDArray.indexOf(equipment.type))
        binding.nameEt.setText(equipment.name)
        binding.makeEt.setText(equipment.make)
        binding.modelEt.setText(equipment.model)
        binding.crewSpinner.setSelection(crewIDArray.indexOf(equipment.crew))
        binding.serialEt.setText(equipment.serial)
        binding.fuelSpinner.setSelection(fuelIDArray.indexOf(equipment.fuelType))
        binding.engineSpinner.setSelection(engineIDArray.indexOf(equipment.engineType))
        binding.dealerSearch.setQuery(equipment.dealerName, false)
        binding.usageTypeSpinner.setSelection(usageTypeIDArray.indexOf(equipment.usageType))
        binding.purchasePriceEt.setText(equipment.purchasePrice)
        binding.weightEt.setText(equipment.weight)

        if (!equipment.purchaseDate.isNullOrBlank()) {
            selectedDate = LocalDate.parse(equipment.purchaseDate, GlobalVars.dateFormatterYYYYMMDD)
        }

        binding.purchaseDateEt.setText(selectedDate.format(GlobalVars.dateFormatterShort))

        if (equipment.plannerShow == "1") {
            binding.showInPlannersSwitch.isChecked = true
            binding.showInPlannersSwitch.jumpDrawablesToCurrentState()
        }
        else {
            binding.showInPlannersSwitch.isChecked = false
            binding.showInPlannersSwitch.jumpDrawablesToCurrentState()
        }

        binding.descriptionEt.setText(equipment.description)

        binding.dealerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)
            layoutManager = LinearLayoutManager(myView.context).apply {
                stackFromEnd = true
                reverseLayout = false
            }

            adapter = activity?.let {
                VendorsAdapter(
                    vendorList,
                    this@NewEditEquipmentFragment
                )
            }

            val itemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(getDrawable(context, R.drawable.opaque_divider)!!)
            addItemDecoration(itemDecoration)

            /*
            var itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.dealerSearchRv.addItemDecoration(itemDecoration)

             */

            binding.dealerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.dealerSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.dealerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.dealerSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as VendorsAdapter).filter.filter(newText)
                    return false
                }

            })


            val closeButton: View? = binding.dealerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.dealerSearch.setQuery("", false)
                equipment.dealer = "0"
                equipment.dealerName = ""
                myView.hideKeyboard()
                binding.dealerSearch.clearFocus()
                binding.dealerSearchRv.visibility = View.INVISIBLE
            }

            binding.dealerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.dealerSearch.setQuery(equipment.dealerName, false)
                    binding.dealerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        hideProgressView()

        editsMade = false
    }

    private fun updateEquipment() {
        // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipment.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Update equipment response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val gson = GsonBuilder().create()
                        val newEquipmentID: String = gson.fromJson(parentObject["equipmentID"].toString(), String::class.java)

                        println("newEquipmentID: $newEquipmentID")

                        globalVars.playSaveSound(myView.context)
                        editsMade = false

                        if (editMode) {
                            editsMade = false
                            myView.findNavController().navigateUp()

                        } else {
                            val directions = NewEditEquipmentFragmentDirections.navigateToEquipment(null)
                            directions.equipmentID = newEquipmentID
                            myView.findNavController().navigate(directions)
                        }
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

                if (equipment.purchaseDate == null) {equipment.purchaseDate = ""}
                if (equipment.usage == null) {equipment.usage = "0"}

                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["equipmentID"] = equipment.ID
                params["addedBy"] = GlobalVars.loggedInEmployee!!.ID
                params["name"] = binding.nameEt.text.trim().toString()
                params["make"] = binding.makeEt.text.trim().toString()
                params["model"] = binding.modelEt.text.trim().toString()
                params["crew"] = equipment.crew!!
                params["vendorID"] = equipment.dealer!!
                params["fuelType"] = equipment.fuelType!!
                params["engineType"] = equipment.engineType!!
                params["serial"] = binding.serialEt.text.trim().toString()
                params["status"] = equipment.status
                params["type"] = equipment.type
                params["active"] = equipment.active
                params["usageType"] = equipment.usageType!!
                params["plannerShow"] = equipment.plannerShow!!
                params["description"] = binding.descriptionEt.text.trim().toString()
                params["purchaseDate"] = equipment.purchaseDate!!
                params["purchasePrice"] = binding.purchasePriceEt.text.trim().toString()
                params["weight"] = binding.weightEt.text.trim().toString()
                params["usage"] = equipment.usage!!




                println("Update equipment params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun validateFields(): Boolean {

        if (binding.nameEt.text.trim().isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_equipment),getString(R.string.incomplete_equipment_name))
            return false
        }
        return true
    }


    override fun onVendorCellClickListener(data: Vendor) {
        editsMade = true
        equipment.dealer = data.ID
        equipment.dealerName = data.name
        binding.dealerSearch.setQuery(equipment.dealerName, false)
        binding.dealerSearch.clearFocus()
        myView.hideKeyboard()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }


    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}