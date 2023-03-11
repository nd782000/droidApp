package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditEmployeeBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.concurrent.schedule


class NewEditEmployeeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false


    private var employeeID: String? = null
    private var employee: Employee? = null
    private lateinit var stateAdapter: ArrayAdapter<String>
    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var payTypeAdapter: ArrayAdapter<String>
    private lateinit var taxStatusAdapter: ArrayAdapter<String>

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private var prefixValue = ""
    private var fNameValue = ""
    private var mNameValue = ""
    private var lNameValue = ""
    private var sysnameValue = ""

    private var phoneValue = ""
    private var emailValue = ""

    private var usernameValue = ""
    private var passwordValue = ""
    private var departmentValue = ""
    private var activeValue = "1"

    private var street1Value = ""
    private var street2Value = ""
    private var street3Value = ""
    private var street4Value = ""
    private var cityValue = ""
    private var stateValue = ""
    private var zipValue = ""

    private var dobValue = ""
    private var dobValueDate = LocalDate.now()
    private var payTypeValue = ""
    private var payRateValue = ""
    private var taxStatusValue = ""
    private var dependentsValue = ""

    private var originalUsername = ""
    private var originalSysname = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
        if (employee != null) {
            employeeID = employee!!.ID
            editMode = true
        }
        else {
            employeeID = "0"
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("employeeNewEdit")
    }

    private var _binding: FragmentNewEditEmployeeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditEmployeeBinding.inflate(inflater, container, false)
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_employee_num, employeeID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_employee)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flag edits made false after all the views have time to set their states
        Timer("CustomerEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }



        // Name
        binding.newEmployeeNamePrefixEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeNamePrefixEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                prefixValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeNameFirstEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeNameFirstEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                fNameValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeNameFirstEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.newEmployeeNameSystemEt.text.isBlank() && binding.newEmployeeNameFirstEt.text.isNotBlank() && binding.newEmployeeNameLastEt.text.isNotBlank()) {
                    binding.newEmployeeNameSystemEt.setText(getString(R.string.new_customer_autofill_sysname, binding.newEmployeeNameLastEt.text, binding.newEmployeeNameFirstEt.text))
                }
            }
        }

        binding.newEmployeeNameMiddleEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeNameMiddleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mNameValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeNameLastEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeNameLastEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                lNameValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeNameLastEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.newEmployeeNameSystemEt.text.isBlank() && binding.newEmployeeNameFirstEt.text.isNotBlank() && binding.newEmployeeNameLastEt.text.isNotBlank()) {
                    binding.newEmployeeNameSystemEt.setText(getString(R.string.new_customer_autofill_sysname, binding.newEmployeeNameLastEt.text, binding.newEmployeeNameFirstEt.text))
                }
            }
        }
        binding.newEmployeeNameSystemEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeNameSystemEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                sysnameValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeNameSystemEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkUniqueSysname(sysnameValue, false)
            }
        }
        /*
        binding.newEmployeeNameSystemEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.newEmployeeNameSystemEt.text.isBlank() && binding.newEmployeeNameFirstEt.text.isNotBlank() && binding.newEmployeeNameLastEt.text.isNotBlank()) {
                    binding.newEmployeeNameSystemEt.setText(getString(R.string.new_customer_autofill_sysname, binding.newEmployeeNameLastEt.text, binding.newEmployeeNameFirstEt.text))
                }
            }
        }

         */

        // Contact
        binding.newEmployeeContactPhoneEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeContactPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                phoneValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeContactEmailEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeContactEmailEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                emailValue = s.toString()
                editsMade = true
            }
        })

        // Account Info
        binding.newEmployeeUsernameEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeUsernameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                usernameValue = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeUsernameEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkUniqueUsername(usernameValue, false)
            }
        }
        binding.newEmployeePasswordEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeePasswordEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                passwordValue = s.toString()
                editsMade = true
            }
        })
        val departmentNameList = mutableListOf<String>()
        GlobalVars.departments!!.forEach {
            departmentNameList.add(it.name)
        }
        departmentAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentNameList
        )
        binding.newEmployeeDepartmentSpinner.adapter = departmentAdapter
        binding.newEmployeeDepartmentSpinner.onItemSelectedListener = this@NewEditEmployeeFragment

        binding.newEmployeeActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            activeValue = if (isChecked) {
                "1"
            } else {
                "0"
            }
        }

        // Address
        binding.newEmployeeAddressStreet1Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressStreet1Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                street1Value = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeAddressStreet2Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressStreet2Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                street2Value = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeAddressStreet3Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressStreet3Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                street3Value = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeAddressStreet4Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressStreet4Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                street4Value = s.toString()
                editsMade = true
            }
        })
        binding.newEmployeeAddressCityEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressCityEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                cityValue = s.toString()
                editsMade = true
            }
        })

        binding.newEmployeeAddressStateSpinner.setBackgroundResource(R.drawable.text_view_layout)
        stateAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.states
        )
        stateAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newEmployeeAddressStateSpinner.adapter = stateAdapter
        binding.newEmployeeAddressStateSpinner.onItemSelectedListener = this@NewEditEmployeeFragment

        binding.newEmployeeAddressZipEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeAddressZipEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                zipValue = s.toString()
                editsMade = true
            }
        })

        // Financial Info
        binding.newEmployeeFinancialDobEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
            datePicker.showDialog(dobValueDate.year, dobValueDate.monthValue-1, dobValueDate.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    dobValueDate = LocalDate.of(year, month+1, dayOfMonth)
                    binding.newEmployeeFinancialDobEt.setText(dobValueDate.format(GlobalVars.dateFormatterShort))
                    dobValue = dobValueDate.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }
        val payTypeArray = arrayOf(getString(R.string.pay_type_hourly), getString(R.string.pay_type_salary))
        payTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            payTypeArray
        )
        binding.newEmployeeFinancialPayTypeSpinner.adapter = payTypeAdapter
        binding.newEmployeeFinancialPayTypeSpinner.onItemSelectedListener = this@NewEditEmployeeFragment

        binding.newEmployeeFinancialPayRateEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeFinancialPayRateEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                payRateValue = s.toString()
                editsMade = true
            }
        })
        val taxStatusArray = arrayOf(getString(R.string.tax_status_single), getString(R.string.tax_status_married))
        taxStatusAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            taxStatusArray
        )
        binding.newEmployeeFinancialTaxStatusSpinner.adapter = taxStatusAdapter
        binding.newEmployeeFinancialTaxStatusSpinner.onItemSelectedListener = this@NewEditEmployeeFragment

        binding.newEmployeeFinancialDependentsEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEmployeeFinancialDependentsEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                dependentsValue = s.toString()
                editsMade = true
            }
        })

        // Submit
        binding.newEmployeeSubmitBtn.setOnClickListener {
            if (validateFields()) {
                // Calls unique checks in sequence, then submits employee
                checkUniqueSysname(sysnameValue, true)
            }
        }

        if (editMode) {
            println("edit mode")
            populateFields()
        }

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true


        when (parent!!.id) {
            R.id.new_employee_address_state_spinner -> {
                stateValue = GlobalVars.statesShort[position]
            }

            R.id.new_employee_financial_pay_type_spinner -> {
                if (position == 0) {
                    payTypeValue = "0"
                }
                else {
                    payTypeValue = "2"
                }
            }

            R.id.new_employee_financial_tax_status_spinner -> {
                taxStatusValue = position.toString()
            }
        }
    }


    private fun populateFields() {
        println("populateFields")



        // Name
        if (employee!!.salutation != null) {
            prefixValue = employee!!.salutation!!
            binding.newEmployeeNamePrefixEt.setText(prefixValue)
        }
        if (employee!!.fname != null) {
            fNameValue = employee!!.fname!!
            binding.newEmployeeNameFirstEt.setText(fNameValue)
        }
        if (employee!!.middleName != null) {
            mNameValue = employee!!.middleName!!
            binding.newEmployeeNameMiddleEt.setText(mNameValue)
        }
        if (employee!!.lname != null) {
            lNameValue = employee!!.lname!!
            binding.newEmployeeNameLastEt.setText(lNameValue)
        }
        sysnameValue = employee!!.name
        binding.newEmployeeNameSystemEt.setText(sysnameValue)

        // Contact Info
        if (employee!!.phone != null) {
            phoneValue = employee!!.phone!!
            binding.newEmployeeContactPhoneEt.setText(phoneValue)
        }
        if (employee!!.email != null) {
            emailValue = employee!!.email!!
            binding.newEmployeeContactEmailEt.setText(emailValue)
        }

        // Account Info
        usernameValue = employee!!.username
        binding.newEmployeeUsernameEt.setText(usernameValue)
        if (employee!!.dep != null) {
            departmentValue = employee!!.dep!!
            if (departmentValue != "") {
                println("Setting Department $departmentValue")
                for (i in 0 until GlobalVars.departments!!.size) {
                    if (GlobalVars.departments!![i].ID == departmentValue) {
                        println("Found the department!")
                        binding.newEmployeeDepartmentSpinner.setSelection(i)
                    }
                }
            }
        }
        if (employee!!.active == "1") {
            binding.newEmployeeActiveSwitch.isChecked = true
        }
        else {
            binding.newEmployeeActiveSwitch.isChecked = false
        }
        binding.newEmployeeActiveSwitch.jumpDrawablesToCurrentState()

        // Address
        if (employee!!.address != null) {
            street1Value = employee!!.address!!
            binding.newEmployeeAddressStreet1Et.setText(street1Value)
        }
        if (employee!!.address2 != null) {
            street2Value = employee!!.address2!!
            binding.newEmployeeAddressStreet2Et.setText(street2Value)
        }
        if (employee!!.address3 != null) {
            street3Value = employee!!.address3!!
            binding.newEmployeeAddressStreet3Et.setText(street3Value)
        }
        if (employee!!.address4 != null) {
            street4Value = employee!!.address4!!
            binding.newEmployeeAddressStreet4Et.setText(street4Value)
        }
        if (employee!!.city != null) {
            cityValue = employee!!.city!!
            binding.newEmployeeAddressCityEt.setText(cityValue)
        }
        if (employee!!.state != null) {
            stateValue = employee!!.state!!
            val addressStateIndex = GlobalVars.statesShort.indexOf(employee!!.state)
            if (addressStateIndex != -1) {
                binding.newEmployeeAddressStateSpinner.setSelection(addressStateIndex)
            }
        }
        if (employee!!.zip != null) {
            zipValue = employee!!.zip!!
            binding.newEmployeeAddressZipEt.setText(zipValue)
        }

        // Financial Info
        if (employee!!.dob != null) {
            dobValue = employee!!.dob!!
            dobValueDate = LocalDate.parse(dobValue, GlobalVars.dateFormatterYYYYMMDD)
            binding.newEmployeeFinancialDobEt.setText(GlobalVars.dateFormatterShort.format(dobValueDate))
        }
        if (employee!!.payType != null) {
            payTypeValue = employee!!.payType!!
            if (payTypeValue == "0") {
                binding.newEmployeeFinancialPayTypeSpinner.setSelection(0)
            } else { // "2"
                binding.newEmployeeFinancialPayTypeSpinner.setSelection(1)
            }
        }
        if (employee!!.payRate != null) {
            payRateValue = employee!!.payRate!!
            binding.newEmployeeFinancialPayRateEt.setText(payRateValue)
        }
        if (employee!!.taxStatus != null) {
            taxStatusValue = employee!!.taxStatus!!
            if (taxStatusValue == "0") {
                binding.newEmployeeFinancialPayTypeSpinner.setSelection(0)
            } else { // "1"
                binding.newEmployeeFinancialPayTypeSpinner.setSelection(1)
            }
        }
        if (employee!!.dependents != null) {
            dependentsValue = employee!!.dependents!!
            binding.newEmployeeFinancialDependentsEt.setText(dependentsValue)
        }

        originalUsername = usernameValue
        originalSysname = sysnameValue

        editsMade = false
        hideProgressView()
    }

    private fun checkUniqueSysname(_sysname:String, submitting:Boolean) {

        println("checkUniqueSysname()")

        if (submitting) {
            showProgressView()
        }

        val sysnameTrimmed = _sysname.trim()

        // Exit if the username is unchanged (for edit mode)
        if (sysnameTrimmed == originalSysname.trim()) {
            println("sys unchanged, returning")
            if (submitting) {
                checkUniqueUsername(usernameValue, true)
                return
            }
            else {
                return
            }
        }


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/other/unique.php"

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
                    //if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                    val gson = GsonBuilder().create()
                    val result: Boolean = gson.fromJson(parentObject["unique"].toString(), Boolean::class.java)

                    println("Unique sysname check result: $result")

                    if (result) {
                        println("Unique true")
                        sysnameValue = sysnameTrimmed
                        binding.newEmployeeNameSystemEt.setText(sysnameTrimmed)
                        if (submitting) {
                            checkUniqueUsername(usernameValue, true)
                        }

                    }
                    else {
                        globalVars.simpleAlert(myView.context,getString(R.string.duplicate_sysname_title),getString(R.string.duplicate_sysname_body, sysnameTrimmed))
                        println("Unique false")
                        binding.newEmployeeNameSystemEt.setText("")
                    }
                    //}





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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["val"] = sysnameTrimmed
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "employeeNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun checkUniqueUsername(_username:String, submitting: Boolean) {

        println("checkUniqueUsername()")

        val usernameTrimmed = _username.trim()

        // Exit if the username is unchanged (for edit mode)
        if (usernameTrimmed == originalUsername.trim()) {
            println("username unchanged, returning")
            if (submitting) {
                updateEmployee()
                return
            }
            else {
                return
            }

        }


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/other/usernameUnique.php"

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

                    val gson = GsonBuilder().create()
                    val result: Boolean = gson.fromJson(parentObject["unique"].toString(), Boolean::class.java)

                    println("Unique username check result: $result")

                    if (result) {
                        println("Unique true")
                        usernameValue = usernameTrimmed
                        binding.newEmployeeUsernameEt.setText(usernameTrimmed)

                        if (submitting) {
                            updateEmployee()
                        }
                    }
                    else {
                        globalVars.simpleAlert(myView.context,getString(R.string.duplicate_username_title),getString(R.string.duplicate_username_body, usernameTrimmed))
                        println("Unique false")
                        binding.newEmployeeUsernameEt.setText("")
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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["val"] = usernameTrimmed
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "employeeNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun updateEmployee() {
        // println("getCustomer = ${customer!!.ID}")
        //showProgressView()

        if (dobValue.isBlank()) {
            dobValue = LocalDate.now().format(GlobalVars.dateFormatterYYYYMMDD)
        }

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/employee.php"

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


                        val gson = GsonBuilder().create()
                        //val newEmpID: String = gson.fromJson(parentObject["empID"].toString(), String::class.java)


                        globalVars.playSaveSound(myView.context)
                        editsMade = false

                        myView.findNavController().navigateUp()

                        /*
                        if (editMode) {
                            editsMade = false
                            myView.findNavController().navigateUp()

                        } else {
                            val directions =
                                NewEditCustomerFragmentDirections.navigateToCustomer(newCustID)
                            myView.findNavController().navigate(directions)
                        }

                         */
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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["empID"] = employeeID!!
                params["name"] = sysnameValue

                params["salutation"] = prefixValue
                params["fname"] = fNameValue
                params["middleName"] = mNameValue
                params["lname"] = lNameValue

                params["username"] = usernameValue
                if (passwordValue.isNotBlank()) {
                    params["password"] = passwordValue
                    params["changePass"] = "1"
                }
                else {
                    params["changePass"] = "0"
                }

                params["depID"] = departmentValue
                params["active"] = activeValue
                params["phone"] = phoneValue
                params["email"] = emailValue

                if (!editMode) { // new mode
                    params["mobile"] = ""
                    params["salesRep"] = ""
                    params["lang"] = "EN"
                }
                else { // edit mode
                    if (employee!!.mobile != null) {
                        params["mobile"] = employee!!.mobile!!
                    }
                    else {
                        params["mobile"] = ""
                    }
                    if (employee!!.salesRep != null) {
                        params["salesRep"] = employee!!.salesRep!!
                    }
                    else {
                        params["salesRep"] = ""
                    }
                    if (employee!!.lang != null) {
                        params["lang"] = employee!!.lang!!
                    }
                    else {
                        params["lang"] = "EN"
                    }
                }

                params["payRate"] = payRateValue
                params["payType"] = payTypeValue

                params["address"] = street1Value
                params["address2"] = street2Value
                params["address3"] = street3Value
                params["address4"] = street4Value
                params["city"] = cityValue
                params["state"] = stateValue
                params["zip"] = binding.newEmployeeAddressZipEt.text.toString()

                params["dob"] = dobValue
                params["taxStatus"] = taxStatusValue
                params["dependants"] = dependentsValue

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }




    private fun validateFields(): Boolean {

        if (sysnameValue.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_employee),getString(R.string.empty_sysname_body))
            return false
        }

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+"

        if (emailValue.isNotBlank()) {
            if (emailValue.matches(emailPattern.toRegex())) {
                println("valid email address")
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.invalid_email),getString(R.string.invalid_email_body))
                return false
            }
        }

        if (usernameValue.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.incomplete_employee),getString(R.string.empty_username_body))
            return false
        }

        if (!editMode) {
            if (passwordValue.isBlank()) {
                globalVars.simpleAlert(myView.context,getString(R.string.incomplete_employee),getString(R.string.empty_password_body))
                return false
            }
        }

        if (passwordValue.isNotEmpty()) {
            if (passwordValue.length < 5) {
                globalVars.simpleAlert(myView.context,getString(R.string.password_too_short_title),getString(R.string.password_too_short_body))
                return false
            }
        }

        return true
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