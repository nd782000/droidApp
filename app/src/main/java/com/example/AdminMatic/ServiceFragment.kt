package com.example.AdminMatic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentServiceBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterPHP
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterShort
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class ServiceFragment : Fragment() {

    private var service: EquipmentService? = null
    private var historyMode = false
    private var equipmentUsage = 0

    private var equipment:Equipment? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var adapter:EquipmentDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable("service")
            historyMode = it.getBoolean("historyMode")
            equipment = it.getParcelable("equipment")
        }
    }

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        myView = binding.root
        setHasOptionsMenu(true)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.service)

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
                    val directions = ServiceFragmentDirections.navigateToNewEditService(equipment, service)
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

        println("onViewCreated")

        binding.serviceInstructionsTxt.text = service!!.instructions


        binding.serviceNotesEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                service!!.completionNotes = s.toString()
            }
        })

        binding.updateServiceBtn.setOnClickListener{
            showUpdateMenu()
        }

        //Date stuff

        println(service!!.createDate)
        val createDate = LocalDate.parse(service!!.createDate, dateFormatterPHP)
        val currentDate = LocalDate.now()
        //val nextDate = createDate.plusDays(service!!.nextValue!!.toLong())
        var nextDate = currentDate
        if (service!!.nextDate != null) {
            println("Attempting to parse ${service!!.nextDate}")
            nextDate = LocalDate.parse(service!!.nextDate, dateFormatterShort)
        }


        binding.serviceNameTxt.text = service!!.name
        binding.serviceTypeTxt.text = requireActivity().getString(R.string.service_type, service!!.typeName)
        if(service!!.addedBy != null){
            binding.serviceAddedByTxt.text = requireActivity().getString(R.string.service_added_by, service!!.addedByName, createDate.format(dateFormatterShort))
        }
        if(service!!.instructions != null){
            binding.serviceInstructionsTxt.text = service!!.instructions
        }
        if(service!!.completionNotes != null){
            binding.serviceNotesEditTxt.setText(service!!.completionNotes)
        }


        when (service!!.type) {
            "0" -> { //one time
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_one_time))
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_mileage_hours)
                binding.serviceDueTxt.text = requireActivity().getString(R.string.service_due, nextDate.format(dateFormatterShort), "")
                if (currentDate >= nextDate && !historyMode) {
                    binding.serviceDueTxt.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                }
                binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, getString(R.string.na))
            }
            "1" -> { //repeating
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_date_based))

                binding.serviceDueTxt.text = requireActivity().getString(R.string.service_due, nextDate.format(dateFormatterShort), "")
                if (service!!.frequency != null) {
                    binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, getString(R.string.service_every_x_days, service!!.frequency))
                }

                if (currentDate >= nextDate && !historyMode) {
                    binding.serviceDueTxt.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                }

            }
            "2" -> { //usage based

                when (equipment!!.usageType) {
                    "km" -> {
                        binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_km_based))
                        binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, getString(R.string.service_every_x_km, service!!.frequency))
                        binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.kilometers))
                        binding.serviceDueTxt.text = getString(R.string.service_due, service!!.nextValue, getString(R.string.kilometers))
                    }
                    "hours" -> {
                        binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_engine_hour_based))
                        binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, getString(R.string.service_every_x_engine_hours, service!!.frequency))
                        binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.engine_hours))
                        binding.serviceDueTxt.text = getString(R.string.service_due, service!!.nextValue, getString(R.string.engine_hours))
                    }
                    else -> { // miles
                        binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_mile_based))
                        binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, getString(R.string.service_every_x_miles, service!!.frequency))
                        binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.miles))
                        binding.serviceDueTxt.text = getString(R.string.service_due, service!!.nextValue, getString(R.string.miles))
                    }
                }




                if (service!!.nextValue!!.toInt() <= equipmentUsage && !historyMode) {
                    binding.serviceDueTxt.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                }


                //binding.nextEditTxt.setText(nextValue.toString())

            }
        }

        // Set current label
        when (equipment!!.usageType) {
            "km" -> {
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.kilometers))
            }
            "hours" -> {
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.engine_hours))
            }
            else -> { // miles
                binding.serviceCurrentTitleTxt.text = getString(R.string.completion_value_x, getString(R.string.miles))
            }
        }

        setStatus(service!!.status.toString())


        if (historyMode) {
            binding.currentEditTxt.visibility = View.GONE
            binding.serviceCurrentTitleTxt.visibility = View.GONE
            binding.serviceNotesEditTxt.isFocusable = false
            binding.serviceFrequencyTxt.text = requireActivity().getString(R.string.service_added_by, service!!.addedByName, createDate.format(dateFormatterShort))
            binding.serviceAddedByTxt.text = requireActivity().getString(R.string.service_completed_by, service!!.completedByName, createDate.format(dateFormatterShort))
        }



        hideProgressView()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("service")
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

        val popUp = PopupMenu(myView.context, binding.updateServiceBtn)
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
                    if (equipment!!.usage!!.toInt() > currentValue) {
                        val builder = AlertDialog.Builder(myView.context)
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
                    if (equipment!!.usage!!.toInt() > currentValue) {
                        val builder = AlertDialog.Builder(myView.context)
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

                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.cancel_service_title))
                    builder.setMessage(getString(R.string.cancel_service_body, service!!.name))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (equipment!!.usage!!.toInt() > currentValue) {
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
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.skip_service_title))
                    builder.setMessage(getString(R.string.skip_service_body, service!!.name))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (equipment!!.usage!!.toInt() > currentValue) {
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

    private fun validateFields():Boolean {
        if (binding.currentEditTxt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.provide_a_completion_value))
            return false
        }
        return true
    }

    private fun updateService(newStatus:String) {

        if (!validateFields()) {
            return
        }

        service!!.status = newStatus

        setStatus(service!!.status.toString())

        showProgressView()


        var newNextValue = ""
        var newNextDateValue = ""
        var newNextDate = LocalDate.now()

        /*
        if (service!!.targetDate != null && service!!.targetDate != "" && service!!.targetDate != "0000-00-00") {
            newNextDate = LocalDate.parse(service!!.targetDate, GlobalVars.dateFormatterYYYYMMDD)
            println("found an existing target date to calculate next from")
        }
         */

        if (newStatus == "2" || newStatus == "4") {
            if (service!!.type == "2") {
                newNextValue = (service!!.currentValue!!.toInt() + service!!.frequency!!.toInt()).toString()
            }
            else if (service!!.type != "0") {
                newNextDate = newNextDate.plusDays(service!!.frequency!!.toLong())
                /*
                while (newNextDate < LocalDate.now()) {
                    newNextDate = newNextDate.plusDays(service!!.frequency!!.toLong())
                }
                 */
                newNextDateValue = newNextDate.format(GlobalVars.dateFormatterYYYYMMDD)
            }
        }


        var urlString =
            "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentServiceComplete.php"

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

                    setStatus(newStatus)

                    hideProgressView()


                    if (newStatus == "2" || newStatus == "4") { // If completed or canceled, show dialogue for followup service
                        when (service!!.type) {
                            "1" -> {
                                globalVars.simpleAlert(myView.context,getString(R.string.new_service_added_title),getString(R.string.new_service_added_body_date, getString(R.string.service_type_date_based), newNextDate.format(dateFormatterShort)))
                                println("New next date: $newNextDate")
                            }
                            "2" -> {
                                when (equipment!!.usageType) {
                                    "km" -> {
                                        globalVars.simpleAlert(myView.context,getString(R.string.new_service_added_title),getString(R.string.new_service_added_body_usage, getString(R.string.service_type_km_based), newNextValue, getString(R.string.kilometers)))
                                    }
                                    "hours" -> {
                                        globalVars.simpleAlert(myView.context,getString(R.string.new_service_added_title),getString(R.string.new_service_added_body_usage, getString(R.string.service_type_engine_hour_based), newNextValue, getString(R.string.hours)))
                                    }
                                    else -> { // miles
                                        globalVars.simpleAlert(myView.context,getString(R.string.new_service_added_title),getString(R.string.new_service_added_body_usage, getString(R.string.service_type_mile_based), newNextValue, getString(R.string.miles)))
                                    }
                                }
                            }
                            "4" -> {
                                if (service!!.frequency != "0") {
                                    globalVars.simpleAlert(myView.context,getString(R.string.new_service_added_title),getString(R.string.new_service_added_body_date, getString(R.string.service_type_inspection), newNextDate.format(dateFormatterShort)))
                                }
                            }
                            else -> {
                                println("no alert necessary")
                            }
                        }
                    }



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
                params["equipmentID"] = equipment!!.ID
                params["ID"] = service!!.ID
                params["completeValue"] = binding.currentEditTxt.text.toString()
                params["completionNotes"] = binding.serviceNotesEditTxt.text.toString()
                params["nextValue"] = newNextValue
                params["targetDate"] = newNextDateValue
                params["status"] = service!!.status.toString()
                params["type"] = service!!.type
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "service"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                binding.serviceStatusIv.setBackgroundResource(R.drawable.ic_not_started)
            }
            "1" -> {
                println("1")
                binding.serviceStatusIv.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "2" -> {
                println("2")
                binding.serviceStatusIv.setBackgroundResource(R.drawable.ic_done)
            }
            "3" -> {
                println("3")
                binding.serviceStatusIv.setBackgroundResource(R.drawable.ic_canceled)
            }
            "4" -> {
                println("4")
                binding.serviceStatusIv.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }

}