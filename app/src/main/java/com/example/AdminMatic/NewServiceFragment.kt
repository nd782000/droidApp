package com.example.AdminMatic

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewServiceBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class NewServiceFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var equipment: Equipment? = null
    private lateinit var service: EquipmentService //? = null

    //private var editModeInitialDataFilled = false

    private var currentDate = LocalDate.now()
    private var nextDate = currentDate

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    //private var existingType = "0"

    private lateinit var typeAdapter: ArrayAdapter<String>
    private lateinit var typeArray:Array<String>
    private lateinit var typeIDArray:Array<String>


    private var typeValue = "0"
    private var typeNameValue = ""
    private var frequencyValue = "0"
    private var nextValue = "0"
    private var warningValue = "0"

    private var editMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
            val tempService:EquipmentService? = it.getParcelable("service")
            if (tempService != null) {
                service = tempService
                editMode = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("newService")
    }

    private var _binding: FragmentNewServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentNewServiceBinding.inflate(inflater, container, false)
        myView = binding.root

        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_service_x, service.ID)

        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_service)
        }
        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")

        binding.newServiceSubmitBtn.setOnClickListener{
            println("new service submit btn clicked")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = Date()

        if (editMode) {
            typeValue = service.type
            binding.nameEditTxt.setText(service.name)
            binding.instructionsEditTxt.setText(service.instructions)
            binding.typeTitleTxt.visibility = View.GONE
            binding.typeSpinner.visibility = View.GONE

            if (service.frequency != null) {frequencyValue = service.frequency!!}
            if (typeValue == "2") { // usage based
                if (service.nextValue != null) {
                    nextValue = service.nextValue!!
                }
            }
            else {
                if (service.nextDate != null) {
                    //nextValue = service.nextDate!!
                    println("SETTING INITIAL NEXTDATE")
                    nextDate = LocalDate.parse(service.nextDate, GlobalVars.dateFormatterShort)
                }
            }
            warningValue = service.warningOffset!!
        }
        else {
            //fill in default for new
            service = EquipmentService(
                "0", //temp
                "",
                "0",
                getString(R.string.service_type_one_time), //typeName
                loggedInEmployee!!.ID, //added by
                "",
                "0", // 0 = not started
                equipment!!.ID, //equipment ID
                "",
                "0",
                "",
                sdf.format(currentTime),
                null,
                null,
                null,
                null,
                "",
                "0",
                "0",
                "0",
                "0",
                "",
                false
            )
        }

        binding.startingEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //if (!editModeInitialDataFilled) {
                //    return
                //}
                if (typeValue == "2") {
                    nextValue = s.toString().trim()
                }
            }
        })

        binding.frequencyEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (typeValue != "0") {
                    frequencyValue = s.toString().trim()
                }

            }
        })

        binding.warningEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                warningValue = s.toString().trim()

            }
        })

        val typeArrayMutable = mutableListOf<String>()
        val typeIDArrayMutable = mutableListOf<String>()
        typeArrayMutable.add(getString(R.string.service_type_one_time))
        typeIDArrayMutable.add("0")
        typeArrayMutable.add(getString(R.string.service_type_date_based))
        typeIDArrayMutable.add("1")
        when (equipment!!.usageType) {
            "km" -> {
                typeArrayMutable.add(getString(R.string.service_type_km_based))
                typeIDArrayMutable.add("2")
            }
            "miles" -> {
                typeArrayMutable.add(getString(R.string.service_type_mile_based))
                typeIDArrayMutable.add("2")
            }
            "hours" -> {
                typeArrayMutable.add(getString(R.string.service_type_engine_hour_based))
                typeIDArrayMutable.add("2")
            }
        }
        typeArrayMutable.add(getString(R.string.service_type_inspection))
        typeIDArrayMutable.add("4")


        typeArray = typeArrayMutable.toTypedArray()
        typeIDArray = typeIDArrayMutable.toTypedArray()


        binding.typeSpinner.setBackgroundResource(R.drawable.text_view_layout)
        typeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            typeArray
        )
        binding.typeSpinner.adapter = typeAdapter
        binding.typeSpinner.onItemSelectedListener = this@NewServiceFragment


        val dateSetListenerNext =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                println("SETTING NEXTDATE FROM DATE SET LISTENER")
                nextDate = LocalDate.of(year, monthOfYear+1, dayOfMonth)
                binding.startingEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                //nextValue = GlobalVars.dateFormatterShort.format(nextDate)
            }


        binding.startingEditTxt.setOnClickListener {
            context?.let { it1 ->
                if(typeValue == "0" || typeValue == "1" || typeValue == "4") {
                    DatePickerDialog(
                        it1,
                        dateSetListenerNext,
                        // set DatePickerDialog to point to today's date when it loads up
                        nextDate.year,
                        nextDate.monthValue-1,
                        nextDate.dayOfMonth
                    ).show()

                    //newService.nextValue = GlobalVars.dateFormatterShort.format(nextDate)

                }
            }
        }

        binding.newServiceSubmitBtn.setOnClickListener {
            submit(false)
        }

        println("setting selection to ${service.type}")
        //binding.typeSpinner.setSelection(typeIDArray.indexOf(service.type))
        updateFieldsForType()
    }

    fun validateFields():Boolean {


        if (binding.nameEditTxt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_service),getString(R.string.new_service_no_name))
            return false
        }

        if (typeValue != "0" && typeValue != "4" && frequencyValue.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_service),getString(R.string.new_service_no_frequency))
            return false
        }

        if (nextValue.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_service),getString(R.string.new_service_no_next))
            return false
        }

        /*
        if (newService.currentValue!! != "0" && newService.type != "0" || newService.type != "1" || newService.type != "4") {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_service),getString(R.string.new_service_no_current))
            return false
        }

         */

        if (typeValue != "0" && typeValue != "4" && typeValue != "1" && nextValue == "0") {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_service),getString(R.string.new_service_no_next))
            return false
        }

        if (typeValue != "4" && binding.instructionsEditTxt.text.isNullOrBlank()) {

            val builder = AlertDialog.Builder(myView.context)
            builder.setTitle(getString(R.string.new_service_no_instructions_title))
            builder.setMessage(getString(R.string.new_service_no_instructions_body))

            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                submit(true)
            }

            builder.setNegativeButton(android.R.string.cancel) { _, _ ->

            }

            builder.show()

            return false
        }

        return true

    }

    fun submit(skipValidation:Boolean) {
        println("new/edit service submit")

        if (!skipValidation) {
            if (!validateFields()) {
                return
            }
        }

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentService.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Update Equipment Service Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("Update Equipment Service parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        service.name = binding.nameEditTxt.text.toString().trim()
                        service.type = typeValue
                        service.typeName = typeNameValue
                        service.frequency = frequencyValue
                        service.nextValue = nextValue
                        if (typeValue == "1") {
                            println("new date: nextDate.format(GlobalVars.dateFormatterShort)")
                            service.nextDate = nextDate.format(GlobalVars.dateFormatterShort)
                        }
                        service.instructions = binding.instructionsEditTxt.text.toString().trim()

                        globalVars.playSaveSound(myView.context)
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

                params["ID"] = service.ID
                params["name"] = binding.nameEditTxt.text.toString().trim()
                params["type"] = typeValue
                params["frequency"] = frequencyValue
                params["instructions"] = binding.instructionsEditTxt.text.toString().trim()
                params["equipmentID"] = service.equipmentID!!
                params["status"] = service.status!!
                params["warningOffset"] = warningValue
                if (typeValue == "2") { // usage based
                    params["nextValue"] = nextValue
                }
                else {
                    params["targetDate"] = nextDate.format(GlobalVars.dateFormatterYYYYMMDD)
                }
                params["addedBy"] = loggedInEmployee!!.ID
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["companyUnique"] = loggedInEmployee!!.companyUnique

                println("Update Equipment Service params = $params")

                return params

            }
        }
        postRequest1.tag = "newService"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //editsMade = true
        typeValue = typeIDArray[position]
        updateFieldsForType()

    }

    private fun updateFieldsForType() {

        when (typeValue) {
            "0" -> {
                println("selected item 0 (one time)")
                typeValue = "0"
                typeNameValue = getString(R.string.service_type_one_time)

                binding.startingTitleTxt.setText(R.string.due_date_label)
                binding.startingUnitTxt.text = ""
                binding.frequencyUnitTxt.text = ""

                binding.startingEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                binding.frequencyEditTxt.setText("N/A")

                binding.startingEditTxt.isFocusable = false
                binding.startingEditTxt.isFocusableInTouchMode = false
                binding.frequencyEditTxt.isFocusableInTouchMode = false
                binding.frequencyEditTxt.isFocusableInTouchMode = false

                binding.warningUnitTxt.setText(R.string.service_reminder_warning_days)

            }
            "1" -> {
                println("selected item 1 (date based)")
                typeValue = "1"
                typeNameValue = getString(R.string.service_type_date_based)

                binding.startingTitleTxt.setText(R.string.due_date_label)
                binding.startingUnitTxt.text = ""
                binding.frequencyUnitTxt.setText(R.string.days)

                binding.startingEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                binding.frequencyEditTxt.setText(frequencyValue)

                binding.startingEditTxt.isFocusable = false
                binding.startingEditTxt.isFocusableInTouchMode = false
                binding.frequencyEditTxt.isFocusable = true
                binding.frequencyEditTxt.isFocusableInTouchMode = true

                binding.warningUnitTxt.setText(R.string.service_reminder_warning_days)

            }
            "2" -> {
                println("selected item 2 (usage based)")
                typeValue = "2"

                binding.startingTitleTxt.setText(R.string.starting_at_label)

                when (equipment!!.usageType) {
                    "km" -> {
                        typeNameValue = getString(R.string.service_type_km_based)
                        binding.startingUnitTxt.setText(R.string.kilometers)
                        binding.frequencyUnitTxt.setText(R.string.kilometers)
                        binding.warningUnitTxt.setText(R.string.service_reminder_warning_km)
                    }
                    "hours" -> {
                        typeNameValue = getString(R.string.service_type_engine_hour_based)
                        binding.startingUnitTxt.setText(R.string.engine_hours)
                        binding.frequencyUnitTxt.setText(R.string.engine_hours)
                        binding.warningUnitTxt.setText(R.string.service_reminder_warning_hours)
                    }
                    else -> {
                        typeNameValue = getString(R.string.service_type_mile_based)
                        binding.startingUnitTxt.setText(R.string.miles)
                        binding.frequencyUnitTxt.setText(R.string.miles)
                        binding.warningUnitTxt.setText(R.string.service_reminder_warning_miles)
                    }
                }

                binding.startingEditTxt.setText(nextValue)
                binding.frequencyEditTxt.setText(frequencyValue)

                binding.startingEditTxt.isFocusable = true
                binding.startingEditTxt.isFocusableInTouchMode = true
                binding.startingEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                binding.frequencyEditTxt.isFocusable = true
                binding.frequencyEditTxt.isFocusableInTouchMode = true

            }
            "4" -> {
                typeValue = "4"
                typeNameValue = getString(R.string.service_type_inspection)

                binding.startingTitleTxt.setText(R.string.due_date_label)
                binding.startingUnitTxt.text = ""
                binding.frequencyUnitTxt.setText(R.string.days)

                binding.startingEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                binding.frequencyEditTxt.setText(frequencyValue)

                binding.startingEditTxt.isFocusable = false
                binding.startingEditTxt.isFocusableInTouchMode = false
                binding.frequencyEditTxt.isFocusable = true
                binding.frequencyEditTxt.isFocusableInTouchMode = true

                binding.warningUnitTxt.setText(R.string.service_reminder_warning_days)

            }
        }

        /*
        if (!editModeInitialDataFilled) {
            if (editMode) { //populate fields
                println("populating fields")
                binding.nameEditTxt.setText(service.name)
                binding.frequencyEditTxt.setText(service.frequency)
                println("")
                binding.startingEditTxt.setText(service.nextValue)
                binding.instructionsEditTxt.setText(service.instructions)
                if (typeValue == "1") { // date based
                    println("filling in ${service.nextDate}")
                    binding.startingEditTxt.setText(service.nextDate)
                }
            }
            editModeInitialDataFilled = true
        }


        existingType = service.type

         */
        hideProgressView()
    }


    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
    }


}