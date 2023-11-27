package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEquipmentBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime

interface ServiceCellClickListener {
    fun onServiceCellClickListener(data:EquipmentService)
}


class EquipmentFragment : Fragment(), ServiceCellClickListener {

    private var equipment: Equipment? = null
    private var equipmentID = "0"

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    var servicesListCurrent = mutableListOf<EquipmentService>()
    var servicesListHistory = mutableListOf<EquipmentService>()

    lateinit var currentServicesAdapter:ServiceAdapter
    lateinit var historyServicesAdapter:ServiceAdapter

    lateinit var tableMode:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
            equipmentID = it.getString("equipmentID")!!
            if (equipment != null) {
                equipmentID = equipment!!.ID
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.equipment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.

        when (item.itemId) {
            R.id.edit_equipment_item -> {
                if (GlobalVars.permissions!!.equipmentEdit == "1") {
                    setFragmentResult("_refreshEquipment", bundleOf("_refreshEquipment" to false))
                    val directions = EquipmentFragmentDirections.navigateToNewEditEquipment(equipment!!)
                    myView.findNavController().navigate(directions)
                }
                else {
                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment_edit))
                }
            }
        }

        return super.onOptionsItemSelected(item)

    }

    private var _binding: FragmentEquipmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        myView = binding.root
        setHasOptionsMenu(true)
        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("On View Created")

        binding.equipmentStatusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        binding.addServiceBtn.setOnClickListener{
            println("status btn clicked")

            if (GlobalVars.permissions!!.equipmentEdit == "1") {
                val directions = EquipmentFragmentDirections.navigateToNewService(equipment, null)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment_edit))
            }
        }



        //setStatus(equipment!!.status)

        binding.serviceCheckBtn.setOnClickListener {

            // First, check if field is empty
            if (binding.serviceCheckEt.text.isNullOrBlank()) {
                globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.service_check_empty))
                return@setOnClickListener
            }

            // Fetch the field's value
            var newUsage = 0
            if (!binding.serviceCheckEt.text.isNullOrBlank()) {
                newUsage = binding.serviceCheckEt.text.toString().toInt()
            }

            // Check if new usage is less than existing, prompt confirmation before updating
            if (equipment!!.usage!!.toInt() > newUsage) {
                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.service_check_less_than_usage_title))
                builder.setMessage(getString(R.string.service_check_less_than_usage_body, equipment!!.name))

                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    updateUsage(newUsage.toString())
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }

                builder.show()
            }
            else {
                updateUsage(newUsage.toString())
            }
        }

        binding.equipmentTableTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = "CURRENT"

                        binding.serviceRecyclerView.adapter = currentServicesAdapter
                    }
                    1 -> {

                        binding.serviceRecyclerView.adapter = historyServicesAdapter
                        tableMode = "HISTORY"
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        tableMode = "CURRENT"

        println("before get service info")


        getServiceInfo(false)

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("equipment")
    }

    private fun updateUsage(newUsage:String) {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentUsage.php"

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

                        equipment!!.usage = newUsage
                        //binding.equipmentUsageTxt.text = getString(R.string.equipment_usage, equipment!!.usage!!)

                        when (equipment!!.usageType) {
                            "km" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_km, equipment!!.usage!!)
                            }
                            "hours" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_hours, equipment!!.usage!!)
                            }
                            "miles" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_miles, equipment!!.usage!!)
                            }
                            else -> {
                                binding.equipmentUsageTxt.visibility = View.INVISIBLE
                            }
                        }

                        binding.serviceCheckEt.text.clear()
                        getServiceInfo(true)

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
                params["equipmentID"] = equipmentID
                params["usage"] = newUsage
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getServiceInfo(checkDue:Boolean){
        println("getServiceInfo")
        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipment.php"

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

                        val equipmentJSON: JSONObject = parentObject.getJSONObject("equipment")
                        //val equipmentNew = gson.fromJson(equipmentJSON.toString(), Equipment::class.java)
                        //equipment!!.status = equipmentNew.status
                        equipment = gson.fromJson(equipmentJSON.toString(), Equipment::class.java)


                        //current adapter
                        val services: JSONArray = parentObject.getJSONArray("services")
                        println("services = $services")
                        println("services count = ${services.length()}")

                        servicesListCurrent =
                            gson.fromJson(services.toString(), Array<EquipmentService>::class.java)
                                .toMutableList()
                        println("ServiceCount = ${servicesListCurrent.count()}")

                        currentServicesAdapter =
                            ServiceAdapter(servicesListCurrent, this.myView.context, false, equipment!!.usageType!!, equipment!!.usage!!, this)

                        //history adapter
                        val servicesHistory: JSONArray = parentObject.getJSONArray("serviceHistory")
                        println("servicesHistory = $servicesHistory")
                        println("servicesHistory count = ${servicesHistory.length()}")

                        servicesListHistory = gson.fromJson(
                            servicesHistory.toString(),
                            Array<EquipmentService>::class.java
                        ).toMutableList()
                        println("ServiceHistoryCount = ${servicesListHistory.count()}")

                        historyServicesAdapter =
                            ServiceAdapter(servicesListHistory, this.myView.context, true, equipment!!.usageType!!, equipment!!.usage!!, this)


                        binding.serviceRecyclerView.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)


                        //status
                        setStatus(equipment!!.status)

                        binding.serviceRecyclerView.adapter = currentServicesAdapter


                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        binding.serviceRecyclerView.addItemDecoration(itemDecoration)

                        if (equipment!!.image != null) {
                            Picasso.with(context)
                                .load(GlobalVars.thumbBase + equipment!!.image!!.fileName)
                                .placeholder(R.drawable.ic_images) //optional
                                //.resize(imgWidth, imgHeight)         //optional
                                //.centerCrop()                        //optional
                                .into(binding.equipmentPicIv)                       //Your image view object.
                        }




                        binding.equipmentNameTxt.text = equipment!!.name

                        if (equipment!!.usage == null) {equipment!!.usage = "0"}

                        when (equipment!!.usageType) {
                            "km" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_km, equipment!!.usage!!)
                                binding.serviceCheckEt.hint = getString(R.string.km_hint)
                                binding.currentServiceCheckTxt.text = getString(R.string.current_x_label, getString(R.string.kilometers))
                            }
                            "hours" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_hours, equipment!!.usage!!)
                                binding.serviceCheckEt.hint = getString(R.string.hours_hint)
                                binding.currentServiceCheckTxt.text = getString(R.string.current_x_label, getString(R.string.hours))
                            }
                            "miles" -> {
                                binding.equipmentUsageTxt.text = getString(R.string.equipment_usage_miles, equipment!!.usage!!)
                                binding.serviceCheckEt.hint = getString(R.string.miles_hint)
                                binding.currentServiceCheckTxt.text = getString(R.string.current_x_label, getString(R.string.hours))
                            }
                            else -> {
                                binding.equipmentUsageTxt.visibility = View.INVISIBLE
                                binding.currentServiceCheckTxt.visibility = View.GONE
                                binding.serviceCheckEt.visibility = View.GONE
                                binding.serviceCheckBtn.visibility = View.GONE
                            }
                        }

                        if(equipment!!.typeName != null){
                            binding.equipmentTypeTxt.text = equipment!!.typeName
                        }

                        if(equipment!!.crewName != null){
                            binding.equipmentCrewTxt.text = getString(R.string.equipment_crew_display, equipment!!.crewName)
                        }

                        binding.equipmentDetailsBtn.setOnClickListener{
                            println("details btn clicked")
                            val directions = EquipmentFragmentDirections.navigateToEquipmentDetails(equipment)
                            myView.findNavController().navigate(directions)
                        }

                        if (checkDue) {
                            checkDueServices()
                        }

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
                params["equipmentID"] = equipmentID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun checkDueServices() {
        var numServicesDue = 0

        servicesListCurrent.forEach {
            if (it.serviceDue == true) {
                numServicesDue++
            }
        }

        if (numServicesDue == 0) {
            // If needs service or broken, prompt to set back to online
            if (equipment!!.status == "1" || equipment!!.status == "2") {
                val builder = AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.no_services_due_title))
                builder.setMessage(getString(R.string.no_services_due_prompt_change, equipment!!.name))

                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    equipment!!.status = "0"
                    updateEquipmentStatus()
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }
                builder.show()
            }
            else {
                globalVars.simpleAlert(myView.context, "", getString(R.string.no_services_due))
            }

        }
        else {

            if (equipment!!.status == "0") { // if status is online

                val builder = AlertDialog.Builder(myView.context)
                if (numServicesDue == 1) {
                    builder.setTitle(getString(R.string.one_service_due_title))
                } else {
                    builder.setTitle(getString(R.string.x_services_due_title, numServicesDue))
                }
                builder.setMessage(getString(R.string.services_due_body, equipment!!.name))

                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    equipment!!.status = "1"
                    updateEquipmentStatus()
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }
                builder.show()
            }
            else { //if status isn't online
                globalVars.simpleAlert(myView.context, getString(R.string.x_services_due_title, numServicesDue),getString(R.string.services_due_body_already_offline, equipment!!.name, numServicesDue))
            }


        }
    }

    override fun onServiceCellClickListener(data:EquipmentService){
        println("onServiceCellClickListener ${data.ID}")

        var historyMode = false
        if (tableMode == "HISTORY") {
            historyMode = true
        }

        if (data.type == "4") { // Go to the special inspection fragment if the service type is inspection
            data.equipmentName = equipment!!.name
            val directions = EquipmentFragmentDirections.navigateToServiceInspection(data, equipment, historyMode)
            myView.findNavController().navigate(directions)
        }
        else {
            if (equipment!!.usage.isNullOrBlank()) { equipment!!.usage = "0" }
            data.equipmentName = equipment!!.name
            val directions = EquipmentFragmentDirections.navigateToService(data, equipment, historyMode)
            myView.findNavController().navigate(directions)
        }




        /*
        if (tableMode == "CURRENT"){
            println("Show service in current mode")
            val directions = EquipmentFragmentDirections.navigateToService(data)
            myView.findNavController().navigate(directions)
        }else{
            println("Show service in history mode")

        }
        */
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.equipmentStatusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_online)!!,myView.context), myView.context.getString(R.string.equipment_status_online)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_needs_repair)!!,myView.context), myView.context.getString(R.string.equipment_status_needs_repair)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_broken)!!,myView.context), myView.context.getString(R.string.equipment_status_broken)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_winterized)!!,myView.context), myView.context.getString(R.string.equipment_status_winterized)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            equipment!!.status = item!!.itemId.toString()


            updateEquipmentStatus()


            true


        }

        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun updateEquipmentStatus() {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentStatus.php"

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
                        globalVars.playSaveSound(myView.context)
                    }

                    setStatus(equipment!!.status)
                    hideProgressView()

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["status"] = equipment!!.status
                params["equipmentID"] = equipment!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_online)
            }
            "1" -> {
                println("1")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_needs_repair)
            }
            "2" -> {
                println("2")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_broken)
            }
            "3" -> {
                println("3")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_winterized)
            }
        }
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

/*
when (it.type) {
                "0" -> {
                    println("${it.name}: because type is zero")
                    numServicesDue++
                }
                "1" -> {
                    var futureDate = LocalDateTime.parse(it.createDate, GlobalVars.dateFormatterPHP)

                    if (it.frequency != null) {
                        futureDate = futureDate.plusDays(it.frequency!!.toLong())

                        if (futureDate < LocalDateTime.now()) {
                            println("Future Date: ${GlobalVars.dateFormatterPHP.format(futureDate)}")
                            println("Today's Date: ${GlobalVars.dateFormatterPHP.format(LocalDateTime.now())}")
                            println("${it.name}: because future date is before now")
                            numServicesDue++
                        }
                    }



                }
                "4" -> {

                }
                else -> { // types 2 and 3
                    if (it.nextValue != null) {
                        if (newUsage.toInt() >= it.nextValue!!.toInt()) {
                            it.serviceDue = true
                            println("${it.name}: because $newUsage >= ${it.nextValue!!.toInt()}")
                            numServicesDue++
                        }
                        else {
                            it.serviceDue = false
                        }
                    }
                }
            }
 */