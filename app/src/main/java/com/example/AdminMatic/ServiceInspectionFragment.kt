package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentServiceInspectionBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class ServiceInspectionFragment : Fragment() {

    private var service: EquipmentService? = null
    private var serviceID = ""
    private var equipment: Equipment? = null
    private var historyMode = false
    private var fromMySchedule = false

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var adapter:ServiceInspectionAdapter

    // The list is declared here so the recycler can be passed a reference to it and the radio buttons can edit it
    var questionsList:MutableList<InspectionQuestion> = emptyList<InspectionQuestion>().toMutableList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable("service")
            equipment = it.getParcelable("equipment")
            historyMode = it.getBoolean("historyMode")
            fromMySchedule = it.getBoolean("fromMySchedule")
            serviceID = it.getString("serviceID")!!
        }
    }

    private var _binding: FragmentServiceInspectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentServiceInspectionBinding.inflate(inflater, container, false)
        myView = binding.root
        setHasOptionsMenu(true)

        val emptyList:MutableList<InspectionQuestion> = mutableListOf()

        adapter = ServiceInspectionAdapter(emptyList, historyMode)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.inspection_page_title, equipment!!.name)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.service_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.

        when (item.itemId) {
            R.id.edit_service_item -> {
                if (GlobalVars.permissions!!.equipmentEdit == "1") {
                    val directions = ServiceFragmentDirections.navigateToNewEditService(null, service)
                    myView.findNavController().navigate(directions)
                }
                else {
                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment_edit))
                }
            }
            R.id.planned_dates_item -> {
                if (GlobalVars.permissions!!.equipmentEdit == "1") {
                    val directions = ServiceInspectionFragmentDirections.navigateToPlannedDates(null, null, service)
                    myView.findNavController().navigate(directions)
                }
                else {
                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment_edit))
                }
            }
        }

        return super.onOptionsItemSelected(item)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showProgressView()

        if (serviceID != "") {
            getService()
        }
        else {
            getInspectionItems()
        }



    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("serviceInspection")
    }

    private fun getService(){
        println("getService")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipmentService.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("getLead parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        //var leadJSONObject:JSONObject
                        //leadJSONObject = gson.fromJson(parentObject["leads"].toString() , JSONObject::class.java)

                        service = gson.fromJson(parentObject["service"].toString(), EquipmentService::class.java)

                        getInspectionItems()

                    }

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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["serviceID"] = serviceID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "serviceInspection"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    private fun getInspectionItems() {

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/inspection.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val questions: JSONArray = parentObject.getJSONArray("questions")
                        println("questions = $questions")
                        //println("questions count = ${questions.length()}")

                        val gson = GsonBuilder().create()
                        questionsList = gson.fromJson(questions.toString(), Array<InspectionQuestion>::class.java).toMutableList()

                        binding.serviceInspectionRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                ServiceInspectionAdapter(questionsList, historyMode)
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.serviceInspectionRecyclerView.addItemDecoration(itemDecoration)

                            //(adapter as ServiceInspectionAdapter).notifyDataSetChanged()
                            println(adapter!!.itemCount)

                            setStatus(service!!.status!!)

                            layoutViews()

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
                params["ID"] = service!!.ID
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "serviceInspection"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {
        binding.equipmentBtn.text = equipment!!.name
        binding.equipmentBtn.setOnClickListener {
            if (fromMySchedule) {
                val directions = ServiceFragmentDirections.navigateToEquipment(null)
                directions.equipmentID = equipment!!.ID
                myView.findNavController().navigate(directions)
            }
            else {
                myView.findNavController().navigateUp()
            }
        }

        binding.inspectionNotesEditTxt.setText(service!!.completionNotes)

        binding.statusIv.setOnClickListener{
            showUpdateMenu()
        }

        binding.serviceInspectionSubmitBtn.setOnClickListener{
            showUpdateMenu()
        }

        if (historyMode) {
            binding.serviceInspectionSubmitBtn.visibility = View.GONE
            binding.inspectionNotesEditTxt.isEnabled = false
        }

        when (equipment!!.usageType) {
            "km" -> {
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.kilometers))
            }
            "hours" -> {
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.engine_hours))
            }
            "miles" -> { // miles
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.miles))
            }
            else -> {
                binding.serviceCurrentTitleTxt.visibility = View.GONE
                binding.currentEditTxt.visibility = View.GONE
            }
        }

        if (historyMode) {
            binding.currentEditTxt.visibility = View.GONE
            binding.serviceCurrentTitleTxt.visibility = View.GONE
            binding.inspectionNotesEditTxt.isFocusable = false
        }

        hideProgressView()
    }

    private fun validateFields(newStatus:String):Boolean {

        if (newStatus == "2") {
            var unansweredQuestions = false
            questionsList.forEach {
                if (it.answer == "0") {
                    globalVars.simpleAlert(myView.context,getString(R.string.dialogue_fields_missing_title),getString(R.string.dialogue_fields_missing_body))
                    return false
                }
            }
        }

        if (binding.currentEditTxt.text.isBlank() && binding.currentEditTxt.visibility == View.VISIBLE) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.provide_a_completion_value))
            return false
        }
        return true
    }

    private fun updateService(newStatus:String) {

        if (!validateFields(newStatus)) {
            return
        }

        //Todo: check for status here, line 395 at https://github.com/nd782000/adminMatic.ios/blob/master/AdminMatic/Equipment/EquipmentInspectionViewController.swift
        //Todo: add
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentServiceComplete.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        var newNextDateValue = ""
        var newNextDate = LocalDate.now()

        if (newStatus == "2" || newStatus == "4") { // Only calculate next date if inspection is done or skipped
            if (service!!.frequency != "0") { // inspections with 0 frequency don't recur
                newNextDate = newNextDate.plusDays(service!!.frequency!!.toLong())
                while (newNextDate < LocalDate.now()) {
                    newNextDate = newNextDate.plusDays(service!!.frequency!!.toLong())
                }
                newNextDateValue = newNextDate.format(GlobalVars.dateFormatterYYYYMMDD)
            }
        }


        if (equipment!!.status != "2") {
            var shouldUpdateEquipmentStatus = false

            questionsList.forEach {
                if (it.answer == "2") {
                    shouldUpdateEquipmentStatus = true
                }
            }

            if (shouldUpdateEquipmentStatus) {
                val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                builder.setTitle(R.string.dialogue_inspection_bad_checked_title)
                builder.setMessage(R.string.dialogue_inspection_bad_checked_body)

                builder.setPositiveButton(R.string.equipment_status_broken) { _, _ ->
                    setEquipmentStatus("2")
                }

                builder.setNegativeButton(R.string.equipment_status_needs_repair) { _, _ ->

                    setEquipmentStatus("1")
                }

                builder.setNeutralButton(android.R.string.cancel) { _, _ ->

                }

                /*
                builder.setNeutralButton("Maybe") { dialog, which ->
                    Toast.makeText(myView.context,
                        "Maybe", Toast.LENGTH_SHORT).show()
                }
                */


                builder.show()
                return
            }

        }

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

                    //setFragmentResult("refresh", bundleOf("refresh" to true))
                    globalVars.updateGlobalMySchedule(service!!.ID, MyScheduleEntryType.service, newStatus)

                    hideProgressView()


                    if (newStatus == "2" || newStatus == "4") {
                        if (service!!.frequency != "0") {
                            globalVars.simpleAlert(myView.context, getString(R.string.new_service_added_title), getString(
                                    R.string.new_service_added_body_date, getString(R.string.service_type_inspection), newNextDate.format(
                                        GlobalVars.dateFormatterShort
                                    )
                                )
                            )
                        }
                    }

                    //myView.findNavController().navigateUp()
                    setStatus(newStatus)

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

                val gson = GsonBuilder().disableHtmlEscaping().create()
                println("Questions List: ${gson.toJson(questionsList)}")

                val params: MutableMap<String, String> = java.util.HashMap()

                params["equipmentID"] = equipment!!.ID
                params["ID"] = service!!.ID
                params["completeValue"] = binding.currentEditTxt.text.toString()
                params["completionNotes"] = binding.inspectionNotesEditTxt.text.toString()
                params["questions"] = gson.toJson(questionsList)
                params["targetDate"] = newNextDateValue
                params["nextValue"] = ""
                params["status"] = newStatus
                params["type"] = "4"
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["companyUnique"] = loggedInEmployee!!.companyUnique


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    private fun setEquipmentStatus(newStatus: String) {
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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    hideProgressView()
                    myView.findNavController().navigateUp()

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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["status"] = newStatus
                params["equipmentID"] = equipment!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun showUpdateMenu() {

        println("showUpdateMenu")

        /*
        if (!validateFields) {
            return
        }
        */

        // Return if already complete
        if (service!!.status!!.toInt() > 1) {
            println("status > 1, returning")
            return
        }

        var currentValue = 0
        if (binding.currentEditTxt.text.isNotBlank()) {
            currentValue = binding.currentEditTxt.text.toString().toInt()
        }

        val popUp = PopupMenu(myView.context, binding.serviceInspectionSubmitBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1, getString(R.string.mark_completed))
        popUp.menu.add(0, 1, 1, getString(R.string.mark_in_progress))
        popUp.menu.add(0, 2, 1, getString(R.string.cancel_service))
        // don't show skip on service types that don't repeat
        if (service!!.type != "0" && service!!.type != "4") {
            popUp.menu.add(0, 3, 1, getString(R.string.skip_service))
        }

        popUp.setOnMenuItemClickListener { item: MenuItem ->

            when (item.itemId) {
                0 -> { // mark completed
                    if (equipment!!.usageType != "" && equipment!!.usage!!.toInt() > currentValue) {
                        val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                        builder.setTitle(getString(R.string.service_check_less_than_usage_title))
                        builder.setMessage(getString(R.string.service_check_less_than_usage_body, equipment!!.name))

                        builder.setPositiveButton(android.R.string.ok) { _, _ ->
                            updateService("2")
                        }

                        builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                        }

                        builder.show()
                    }
                    else {
                        updateService("2")
                    }
                }
                1 -> { // mark in progress
                    if (equipment!!.usageType != "" && equipment!!.usage!!.toInt() > currentValue) {
                        val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                        builder.setTitle(getString(R.string.service_check_less_than_usage_title))
                        builder.setMessage(getString(R.string.service_check_less_than_usage_body, equipment!!.name))

                        builder.setPositiveButton(android.R.string.ok) { _, _ ->
                            updateService("1")
                        }

                        builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                        }

                        builder.show()
                    }
                    else {
                        updateService("1")
                    }
                }
                2 -> { // cancel

                    val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.cancel_service_title))
                    builder.setMessage(getString(R.string.cancel_service_body, service!!.name))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (equipment!!.usageType != "" && equipment!!.usage!!.toInt() > currentValue) {
                            builder.setTitle(getString(R.string.service_check_less_than_usage_title))
                            builder.setMessage(getString(R.string.service_check_less_than_usage_body, equipment!!.name))

                            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                                updateService("3")
                            }

                            builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                            }

                            builder.show()
                        }
                        else {
                            updateService("3")
                        }
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }

                    builder.show()

                }
                3 -> { // skip
                    val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.skip_service_title))
                    builder.setMessage(getString(R.string.skip_service_body, service!!.name))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (equipment!!.usageType != "" && equipment!!.usage!!.toInt() > currentValue) {
                            builder.setTitle(getString(R.string.service_check_less_than_usage_title))
                            builder.setMessage(getString(R.string.service_check_less_than_usage_body, equipment!!.name))

                            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                                updateService("4")
                            }

                            builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                            }

                            builder.show()
                        }
                        else {
                            updateService("4")
                        }
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }

                    builder.show()
                }
            }

            true
        }
        popUp.show()
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                binding.statusIv.setBackgroundResource(R.drawable.ic_not_started)
            }
            "1" -> {
                println("1")
                binding.statusIv.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "2" -> {
                println("2")
                binding.statusIv.setBackgroundResource(R.drawable.ic_done)
            }
            "3" -> {
                println("3")
                binding.statusIv.setBackgroundResource(R.drawable.ic_canceled)
            }
            "4" -> {
                println("4")
                binding.statusIv.setBackgroundResource(R.drawable.ic_skipped)
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