package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditWorkOrderBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule



class NewEditWorkOrderFragment : Fragment(), AdapterView.OnItemSelectedListener, CustomerCellClickListener, EmployeeCellClickListener, TemplateCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var workOrder: WorkOrder? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private var templateID = ""
    private var templateName = ""

    private var deadlineValue: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private lateinit var rangeStartValue: LocalDate
    private lateinit var rangeEndValue: LocalDate


    private lateinit var chargeTypeAdapter: ArrayAdapter<String>
    private lateinit var paymentTermsAdapter: ArrayAdapter<String>
    private lateinit var departmentsAdapter: ArrayAdapter<String>
    private lateinit var crewsAdapter: ArrayAdapter<String>

    private lateinit var invoiceTypesArray: Array<String>
    private lateinit var invoiceTypesAdapter: ArrayAdapter<String>

    private lateinit var renewalTypesArray: Array<String>
    private lateinit var renewalTypesAdapter: ArrayAdapter<String>

    private lateinit var daysArray: Array<String>
    private lateinit var daysAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workOrder = it.getParcelable("workOrder")
        }
        if (workOrder != null) {
            editMode = true
            if (workOrder!!.recSettings == null) {
                workOrder!!.recSettings = ContractRecSettings(workOrder!!.woID, null, null, "", "", "0", "1")
            }

        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("workOrderNewEdit")
    }

    private var _binding: FragmentNewEditWorkOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditWorkOrderBinding.inflate(inflater, container, false)
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_work_order, workOrder!!.woID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_work_order)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("on view created")
        showProgressView()
        getCustomers()
    }

    private fun getCustomers() {
        println("getCustomers")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customers.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Get customers response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, com.example.AdminMatic.myView.context, com.example.AdminMatic.myView)) {

                        val customers: JSONArray = parentObject.getJSONArray("customers")

                        val gson = GsonBuilder().create()
                        GlobalVars.customerList =
                            gson.fromJson(customers.toString(), Array<Customer>::class.java)
                                .toMutableList()
                    }
                    getFields()

                    /* Here 'response' is a String containing the response you received from the website... */
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
        postRequest1.tag = "workOrderNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application)
            .addToRequestQueue(postRequest1)
    }

    fun getFields(){
        println("getFields")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/fields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Get fields response $response")


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject get fields = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, com.example.AdminMatic.myView.context, com.example.AdminMatic.myView)) {
                        globalVars.populateFields(context, parentObject)
                        layoutViews()
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
                params["salesTax"] = "1"
                params["tax"] = "1"
                params["crews"] = "1"
                params["departments"] = "1"
                params["terms"] = "1"
                params["templates"] = "1"
                params["depositTypes"] = "1"
                params["defaults"] = "1"

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "workOrderNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    private fun layoutViews() {

        if (!editMode) {
            workOrder = WorkOrder()
            workOrder!!.recID = "0"
            workOrder!!.recSettings = ContractRecSettings("0", null, null, "", "", "0", "1")
        }

        // Flag edits made false after all the views have time to set their states
        Timer("WorkOrderEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        // Customer search
        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@NewEditWorkOrderFragment, false
                )
            }


            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.customerSearchRv.addItemDecoration(itemDecoration)

            binding.customerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.customerSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as CustomersAdapter).filter.filter(newText)
                    if(newText == ""){
                        binding.customerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.customerSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })

            val closeButton: View? = binding.customerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.customerSearch.setQuery("", false)
                workOrder!!.customer = "0"
                workOrder!!.custName = ""
                myView.hideKeyboard()
                binding.customerSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
            }

            binding.customerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.customerSearch.setQuery(workOrder!!.custName, false)
                    binding.customerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        if (!workOrder!!.custName.isNullOrBlank()) {
            binding.customerSearch.setQuery(workOrder!!.custName!!, false)
        }

        // Template search
        if (!editMode) {
            binding.templateSearchRv.apply {
                layoutManager = LinearLayoutManager(activity)

                adapter = activity?.let {
                    TemplatesAdapter(
                        GlobalVars.templates!!,
                        this@NewEditWorkOrderFragment
                    )
                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                binding.templateSearchRv.addItemDecoration(itemDecoration)

                binding.templateSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        binding.templateSearchRv.visibility = View.INVISIBLE
                        myView.hideKeyboard()
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        (adapter as TemplatesAdapter).filter.filter(newText)
                        if (newText == "") {
                            binding.templateSearchRv.visibility = View.INVISIBLE
                        } else {
                            binding.templateSearchRv.visibility = View.VISIBLE
                        }
                        return false
                    }

                })

                val closeButton: View? = binding.templateSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
                closeButton?.setOnClickListener {
                    binding.templateSearch.setQuery("", false)
                    templateID = ""
                    templateName = ""
                    myView.hideKeyboard()
                    binding.templateSearch.clearFocus()
                    binding.templateSearchRv.visibility = View.INVISIBLE
                }

                binding.templateSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                    if (!isFocused) {
                        binding.templateSearch.setQuery(templateName, false)
                        binding.templateSearchRv.visibility = View.INVISIBLE
                    }
                }
            }
        }
        else {
            binding.templateSearch.visibility = View.GONE
            binding.templateTv.visibility = View.GONE
        }


        // Title
        binding.titleEditText.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        if (workOrder!!.title.isNotBlank()) {
            binding.titleEditText.setText(workOrder!!.title)
        }

        // Sales rep search
        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@NewEditWorkOrderFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.salesRepSearchRv.addItemDecoration(itemDecoration)

            binding.repSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as EmployeesAdapter).filter.filter(newText)
                    if(newText == ""){
                        binding.salesRepSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.salesRepSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })

            val closeButton: View? = binding.repSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.repSearch.setQuery("", false)
                workOrder!!.salesRep = ""
                workOrder!!.salesRepName = ""
                myView.hideKeyboard()
                binding.repSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.repSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.repSearch.setQuery(workOrder!!.salesRepName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        if (!workOrder!!.salesRepName.isNullOrBlank()) {
            binding.repSearch.setQuery(workOrder!!.salesRepName!!, false)
        }

        // Charge Type Spinner
        val chargeTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))
        chargeTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            chargeTypeArray
        )
        binding.chargeTypeSpinner.adapter = chargeTypeAdapter
        binding.chargeTypeSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.charge == "") {
            workOrder!!.charge = GlobalVars.defaultFields!!.defCharge
        }
        binding.chargeTypeSpinner.setSelection(workOrder!!.charge!!.toInt() - 1)


        // Invoice Payment Terms Spinner
        val paymentTermsList = mutableListOf<String>()
        GlobalVars.paymentTerms!!.forEach {
            //println("Name: ${it.name}")
            paymentTermsList.add(it.name)
        }
        paymentTermsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            paymentTermsList
        )
        binding.invoiceTermsSpinner.adapter = paymentTermsAdapter
        binding.invoiceTermsSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.paymentTermsID == "") {
            workOrder!!.paymentTermsID = GlobalVars.defaultFields!!.defPayTerms
        }


        if (workOrder!!.paymentTermsID != "") {
            GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
                if (e.ID == workOrder!!.paymentTermsID) {
                    binding.invoiceTermsSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.invoiceTermsSpinner.setSelection(0)
        }



        // Department Spinner
        val departmentNameList = mutableListOf<String>()
        GlobalVars.departments!!.forEach {
            departmentNameList.add(it.name)
        }

        departmentsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentNameList
        )
        binding.departmentSpinner.adapter = departmentsAdapter
        binding.departmentSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.department != "") {
            GlobalVars.departments!!.forEachIndexed { i, e ->
                if (e.ID == workOrder!!.department) {
                    binding.departmentSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.departmentSpinner.setSelection(0)
        }


        // Crew Spinner
        val crewNameList = mutableListOf<String>()
        GlobalVars.crews!!.forEach {
            crewNameList.add(it.name)
        }
        crewsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            crewNameList.toTypedArray()
        )
        binding.crewSpinner.adapter = crewsAdapter
        binding.crewSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.crew != "") {
            GlobalVars.crews!!.forEachIndexed { i, e ->
                if (e.ID == workOrder!!.crew) {
                    binding.crewSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.crewSpinner.setSelection(0)
        }

        // Invoice Type Spinner
        invoiceTypesArray = arrayOf(getString(R.string.invoice_type_upon_completion),
            getString(R.string.invoice_type_batch),
            getString(R.string.invoice_type_no_invoice))

        invoiceTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            invoiceTypesArray
        )
        binding.invoiceTypeSpinner.adapter = invoiceTypesAdapter
        binding.invoiceTypeSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.invoiceType == "") {
            workOrder!!.invoiceType = GlobalVars.defaultFields!!.defInvoice
        }
        else {
            binding.invoiceTypeSpinner.setSelection(workOrder!!.invoiceType!!.toInt() - 1)
        }

        // Deadline
        binding.deadlineEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(deadlineValue.year, deadlineValue.monthValue-1, deadlineValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    deadlineValue = LocalDate.of(year, month+1, dayOfMonth)
                    binding.deadlineEt.setText(deadlineValue.format(GlobalVars.dateFormatterShort))
                    workOrder!!.deadline = deadlineValue.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        if (workOrder!!.deadline != null && workOrder!!.deadline != "" && workOrder!!.deadline != "0000-00-00" && workOrder!!.deadline != "0000-00-00 00:00:00") {
            println("SETTING DEADLINE")
            deadlineValue = LocalDate.parse(workOrder!!.deadline, GlobalVars.dateFormatterPHP)
            binding.deadlineEt.setText(GlobalVars.dateFormatterShort.format(deadlineValue))
        }

        // Renewal Type Spinner

        renewalTypesArray = arrayOf(getString(R.string.does_not_renew),
            getString(R.string.renews_when_finished))

        renewalTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            renewalTypesArray
        )
        binding.renewalTypeSpinner.adapter = renewalTypesAdapter
        binding.renewalTypeSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment


        if (workOrder!!.prompt == "1" || workOrder!!.recID != "0") {
            binding.renewalTypeSpinner.setSelection(1)
            binding.renewalTypeSpinner.isEnabled = false
        }


        // Has Range Switch
        binding.hasRangeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!editsMadeDelayPassed) {
                return@setOnCheckedChangeListener
            }

            editsMade = true

            if (isChecked) {
                if (binding.promptToRepeatSwitch.isChecked) {
                    binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterMonthDay))
                }
                else {
                    binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
                }
                if (binding.promptToRepeatSwitch.isChecked) {
                    binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterMonthDay))
                } else {
                    binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
                }

                if (!binding.hasFrequencySwitch.isChecked) {
                    binding.hasFrequencySwitch.isChecked = true
                }
                //binding.hasFrequencySwitch.isFocusableInTouchMode = false


            }
            else {
                workOrder!!.recSettings!!.startDate = null
                workOrder!!.recSettings!!.endDate = null
                //binding.hasFrequencySwitch.isFocusableInTouchMode = true

            }

            enableDisableFields()
        }

        if (workOrder!!.recSettings!!.startDate != null || workOrder!!.recSettings!!.endDate != null) {
            binding.hasRangeSwitch.isChecked = true
        }
        else {
            binding.hasRangeSwitch.isChecked = false
        }
        binding.hasRangeSwitch.jumpDrawablesToCurrentState()


        // Range Start
        rangeStartValue = LocalDate.of(LocalDate.now().year, GlobalVars.defaultFields!!.recRangeMS.toInt(), GlobalVars.defaultFields!!.recRangeDS.toInt())

        binding.rangeStartEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(rangeStartValue.year, rangeStartValue.monthValue-1, rangeStartValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    val pendingDate = LocalDate.of(year, month+1, dayOfMonth)

                    if (pendingDate < rangeEndValue) {
                        rangeStartValue = pendingDate
                        if (binding.promptToRepeatSwitch.isChecked) {
                            binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterMonthDay))
                        }
                        else {
                            binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
                        }
                        workOrder!!.recSettings!!.startDate = rangeStartValue.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    else {
                        com.example.AdminMatic.globalVars.simpleAlert(
                            com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error),
                            com.example.AdminMatic.myView.context.getString(R.string.dialogue_start_time_after_stop))

                    }
                }
            })
        }

        if (workOrder!!.recSettings!!.startDate != null) {
            rangeStartValue = LocalDate.parse(workOrder!!.recSettings!!.startDate, GlobalVars.dateFormatterYYYYMMDD)

            if (workOrder!!.recSettings!!.fixedDate == "0") {
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterNoYear))
            }
            else {
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
            }
        }

        // Range End
        rangeEndValue = LocalDate.of(LocalDate.now().year, GlobalVars.defaultFields!!.recRangeME.toInt(), GlobalVars.defaultFields!!.recRangeDE.toInt())

        binding.rangeEndEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(rangeEndValue.year, rangeEndValue.monthValue-1, rangeEndValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true

                    val pendingDate = LocalDate.of(year, month+1, dayOfMonth)

                    if (pendingDate > rangeStartValue) {
                        rangeEndValue = pendingDate
                        if (binding.promptToRepeatSwitch.isChecked) {
                            binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterMonthDay))
                        } else {
                            binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
                        }
                        workOrder!!.recSettings!!.endDate = rangeEndValue.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    else {
                        com.example.AdminMatic.globalVars.simpleAlert(
                            com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error),
                            com.example.AdminMatic.myView.context.getString(R.string.dialogue_stop_time_before_start))

                    }
                }
            })
        }

        if (workOrder!!.recSettings!!.endDate != null) {
            rangeEndValue = LocalDate.parse(workOrder!!.recSettings!!.endDate, GlobalVars.dateFormatterYYYYMMDD)

            if (workOrder!!.recSettings!!.fixedDate == "0") {
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterNoYear))
            }
            else {
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
            }
        }

        // Prompt to Repeat Switch
        binding.promptToRepeatSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                workOrder!!.recSettings!!.fixedDate = "0"
                binding.rangeStartText.setText(R.string.season_start_label)
                binding.rangeEndText.setText(R.string.season_end_label)
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterMonthDay))
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterMonthDay))
            }
            else {
                workOrder!!.recSettings!!.fixedDate = "1"
                binding.rangeStartText.setText(R.string.range_start_label)
                binding.rangeEndText.setText(R.string.range_end_label)
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
            }
        }

        if (workOrder!!.recSettings!!.fixedDate == "0" && binding.hasRangeSwitch.isChecked) {
            binding.promptToRepeatSwitch.isChecked = true
        }
        else {
            binding.promptToRepeatSwitch.isChecked = false
        }
        binding.promptToRepeatSwitch.jumpDrawablesToCurrentState()

        // Fill in start/stop fields
        if (binding.promptToRepeatSwitch.isChecked) {
            binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterMonthDay))
        } else {
            binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
        }

        if (binding.promptToRepeatSwitch.isChecked) {
            binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterMonthDay))
        }
        else {
            binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
        }

        // Has Frequency Switch
        binding.hasFrequencySwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (!isChecked) {
                workOrder!!.recSettings!!.frequency = "0"
            }
            enableDisableFields()
        }

        if (workOrder!!.recSettings!!.frequency != "0" && workOrder!!.recSettings!!.frequency != "") {
            binding.hasFrequencySwitch.isChecked = true
            binding.frequencyEt.setText(workOrder!!.recSettings!!.frequency)
        }
        else {
            binding.hasFrequencySwitch.isChecked = false
            binding.frequencyEt.setText("7")
        }
        binding.hasFrequencySwitch.jumpDrawablesToCurrentState()

        // Frequency
        binding.frequencyEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.frequencyEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
                if (s.isNotEmpty()) {
                    val valueInt = s.toString().toInt()
                    if (valueInt != 0 && valueInt % 7 == 0) {
                        binding.preferredDaySpinner.isEnabled = true
                        //binding.preferredDaySpinner.alpha = 0
                    } else {
                        binding.preferredDaySpinner.isEnabled = false
                        binding.preferredDaySpinner.setSelection(0)
                        workOrder!!.recSettings!!.prefDay = "0"
                    }
                }
                else {
                    binding.preferredDaySpinner.isEnabled = false
                }
            }
        })

        if (binding.frequencyEt.text.length > 0) {

            if (binding.frequencyEt.text.toString().toInt() % 7 == 0) {
                binding.preferredDaySpinner.isEnabled = true
                //binding.preferredDaySpinner.alpha = 0
            } else {
                binding.preferredDaySpinner.isEnabled = false
                binding.preferredDaySpinner.setSelection(0)
                workOrder!!.recSettings!!.prefDay = "0"
            }
        }
        else {
            binding.preferredDaySpinner.isEnabled = false
        }

        // Min Between
        binding.minDaysBetweenEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.minDaysBetweenEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        if (workOrder!!.recSettings!!.minDays != "") {
            binding.minDaysBetweenEt.setText(workOrder!!.recSettings!!.minDays)
        }
        else {
            binding.minDaysBetweenEt.setText("0")
        }


        // Preferred Day Spinner
        daysArray = arrayOf(getString(R.string.none),
            getString(R.string.sunday),
            getString(R.string.monday),
            getString(R.string.tuesday),
            getString(R.string.wednesday),
            getString(R.string.thursday),
            getString(R.string.friday),
            getString(R.string.tuesday))

        daysAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            daysArray
        )
        binding.preferredDaySpinner.adapter = daysAdapter
        binding.preferredDaySpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        if (workOrder!!.recSettings!!.prefDay!! != "") {
            binding.preferredDaySpinner.setSelection(workOrder!!.recSettings!!.prefDay!!.toInt())
        }

        // Notes
        binding.notesEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.notesEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.notesEt.setText(workOrder!!.notes)

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateWorkOrder()
            }
        }

        binding.templateSearchRv.visibility = View.INVISIBLE
        binding.salesRepSearchRv.visibility = View.INVISIBLE
        binding.customerSearchRv.visibility = View.INVISIBLE


        enableDisableFields()

        editsMade = false

        //binding.woConfigCl.visibility = View.GONE

        hideProgressView()

        println("finished layoutviews")
    }


    private fun validateFields(): Boolean {

        if (workOrder!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_wo),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        if (binding.hasRangeSwitch.isChecked) {

            if (binding.promptToRepeatSwitch.isChecked) {
                // Set the years the same if we're in season mode
                rangeEndValue = LocalDate.of(rangeStartValue.year, rangeEndValue.monthValue, rangeEndValue.dayOfMonth)

            }

            if (rangeEndValue < rangeStartValue) {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.dialogue_start_time_after_stop))
                return false
            }
        }

        return true
    }

    private fun updateWorkOrder() {
        println("updateWorkOrder")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/work.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("New/edit WO response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        val newWoID: String = gson.fromJson(parentObject["woID"].toString(), String::class.java)
                        workOrder!!.woID = newWoID
                        globalVars.playSaveSound(myView.context)
                        editsMade = false


                        if (editMode) {
                            myView.findNavController().navigateUp()
                        } else {
                            val directions = NewEditWorkOrderFragmentDirections.navigateToWorkOrder(workOrder!!)
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


                workOrder!!.notes = binding.notesEt.text.toString()
                workOrder!!.title = binding.titleEditText.text.toString()
                if (workOrder!!.total.isNullOrBlank()) {
                    workOrder!!.total = "0"
                }

                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["woID"] = workOrder!!.woID
                if (workOrder!!.woID == "0") {
                    params["templateID"] = templateID
                }

                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["customer"] = workOrder!!.customer.toString()
                params["custName"] = workOrder!!.custName.toString()
                params["charge"] = workOrder!!.charge.toString()
                params["status"] = workOrder!!.status
                params["total"] = workOrder!!.total
                params["notes"] = workOrder!!.notes.toString()
                params["urgent"] = workOrder!!.urgent.toString()
                params["archived"] = workOrder!!.archived.toString()
                params["copied"] = workOrder!!.copied.toString()
                params["skipped"] = workOrder!!.skipped.toString()
                params["invoiceID"] = workOrder!!.invoiceID.toString()
                params["ignoreExpired"] = workOrder!!.ignoreExpired.toString()
                params["templateID"] = templateID
                params["salesTaxID"] = workOrder!!.salesTaxID.toString()


                if (workOrder!!.salesRep != "0") {
                    params["salesRep"] = workOrder!!.salesRep.toString()
                    params["repName"] = workOrder!!.salesRepName.toString()
                }
                else {
                    params["salesRep"] = ""
                    params["repName"] = ""
                }


                if (binding.deadlineEt.text.isNotBlank()) {
                    params["deadline"] = GlobalVars.dateFormatterYYYYMMDD.format(deadlineValue)
                }
                else {
                    params["deadline"] = ""
                }

                if (workOrder!!.custName != null) {
                    params["customerName"] = workOrder!!.custName.toString()
                }
                else {
                    params["customerName"] = ""
                }

                params["title"] = workOrder!!.title


                params["departmentID"] = workOrder!!.department.toString()
                params["crew"] = workOrder!!.crew.toString()
                params["crewName"] = workOrder!!.crewName.toString()
                params["invoice"] = workOrder!!.invoiceType.toString()
                params["paymentTermsID"] = workOrder!!.paymentTermsID.toString()
                params["prompt"] = workOrder!!.prompt!!


                params["recID"] = workOrder!!.recID.toString()
                if (workOrder!!.recID!! != "0" || binding.renewalTypeSpinner.selectedItemPosition == 1) { // If there's already a recID or we turned renew on, pass recSettings

                    if (binding.hasRangeSwitch.isChecked) {
                        workOrder!!.recSettings!!.startDate = GlobalVars.dateFormatterYYYYMMDD.format(rangeStartValue)
                        workOrder!!.recSettings!!.endDate = GlobalVars.dateFormatterYYYYMMDD.format(rangeEndValue)
                    }
                    else {
                        workOrder!!.recSettings!!.startDate = null
                        workOrder!!.recSettings!!.endDate = null
                    }

                    if (binding.hasFrequencySwitch.isChecked) {
                        workOrder!!.recSettings!!.frequency = binding.frequencyEt.text.trim().toString()
                    }
                    else {
                        workOrder!!.recSettings!!.frequency = ""
                    }
                    workOrder!!.recSettings!!.minDays = binding.minDaysBetweenEt.text.trim().toString()
                    workOrder!!.recSettings!!.prefDay = binding.preferredDaySpinner.selectedItemPosition.toString()

                    if (binding.promptToRepeatSwitch.isChecked) {
                        workOrder!!.recSettings!!.fixedDate = "0"
                    }
                    else {
                        workOrder!!.recSettings!!.fixedDate = "1"
                    }

                    params["recSettings"] = Gson().toJson(workOrder!!.recSettings!!)
                }


                println("params = $params")




                return params



            }
        }
        postRequest1.tag = "workOrderNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true
        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                val positionPlus1 = position+1
                workOrder!!.charge = positionPlus1.toString()
            }

            R.id.invoice_terms_spinner -> {
                workOrder!!.paymentTermsID = GlobalVars.paymentTerms?.get(position)?.ID.toString()
                println("setting invoice type to ${(position+1).toString()}")
                //println("Selected Payment terms ID: ${GlobalVars.paymentTerms?.get(position)?.ID.toString()}")
            }

            R.id.department_spinner -> {
                workOrder!!.department = GlobalVars.departments?.get(position)?.ID.toString()
            }

            R.id.crew_spinner -> {
                workOrder!!.crew = GlobalVars.crews?.get(position)?.ID.toString()
                workOrder!!.crewName = GlobalVars.crews?.get(position)?.name.toString()
            }

            R.id.invoice_type_spinner -> {

                workOrder!!.invoiceType = (position+1).toString()
            }

            R.id.renewal_type_spinner -> {
                //workOrder!!.renew = position.toString()
                enableDisableFields()
            }
        }

    }

    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        workOrder!!.customer = data.ID
        workOrder!!.custName = data.sysname

        /*
        if (!data.taxID.isNullOrBlank()) {
            workOrder!!.salesTax = data.taxID
        }
         */

        if (!data.termsID.isNullOrBlank()) {
            workOrder!!.paymentTermsID = data.termsID
        }


        GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
            if (e.ID == workOrder!!.paymentTermsID) {
                binding.invoiceTermsSpinner.setSelection(i)
            }
        }

        binding.customerSearch.setQuery(workOrder!!.custName, false)
        binding.customerSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onTemplateCellClickListener(data: Template) {
        editsMade = true
        binding.templateSearch.setQuery(data.name, false)
        binding.templateSearch.clearFocus()
        myView.hideKeyboard()

        templateID = data.ID
        templateName = data.name

        binding.titleEditText.setText(data.name)

        workOrder!!.charge = data.chargeType
        println("charge type: ${data.chargeType}")
        binding.chargeTypeSpinner.setSelection(data.chargeType!!.toInt()-1)

        workOrder!!.department = data.departmentID
        GlobalVars.departments!!.forEachIndexed { i, e ->
            if (e.ID == data.departmentID) {
                binding.departmentSpinner.setSelection(i)
            }
        }

        workOrder!!.crew = data.mainCrew
        GlobalVars.crews!!.forEachIndexed { i, e ->
            if (e.ID == data.mainCrew) {
                binding.crewSpinner.setSelection(i)
            }
        }

        workOrder!!.invoiceType = data.invoiceType
        println("invoice type: ${data.invoiceType}")

        binding.invoiceTypeSpinner.setSelection(data.invoiceType!!.toInt() - 1)

        workOrder!!.paymentTermsID = data.paymentTerms
        GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
            if (e.ID == workOrder!!.paymentTermsID) {
                binding.invoiceTermsSpinner.setSelection(i)
            }
        }

        //workOrder!!.renew = data.renew
        binding.renewalTypeSpinner.setSelection(data.renew!!.toInt())

        if (data.renew == "1") {
            println("renew = true")
            if (data.recSettings != null) {
                workOrder!!.recSettings = data.recSettings

                if (data.recSettings!!.fixedDate == "1") {
                    binding.promptToRepeatSwitch.isChecked = false
                }
                else {
                    binding.promptToRepeatSwitch.isChecked = true
                }
                binding.promptToRepeatSwitch.jumpDrawablesToCurrentState()

                if (data.recSettings!!.startDate != null) {
                    binding.hasRangeSwitch.isChecked = true
                    rangeStartValue = LocalDate.parse(data.recSettings!!.startDate, GlobalVars.dateFormatterYYYYMMDD)
                    binding.rangeStartEt.setText(GlobalVars.dateFormatterShort.format(rangeStartValue))

                    if (workOrder!!.recSettings!!.fixedDate == "0") {
                        binding.rangeStartEt.setText(GlobalVars.dateFormatterNoYear.format(rangeStartValue))
                    }
                    else {
                        binding.rangeStartEt.setText(GlobalVars.dateFormatterShort.format(rangeStartValue))
                    }
                }
                else {
                    binding.hasRangeSwitch.isChecked = false
                }
                binding.hasRangeSwitch.jumpDrawablesToCurrentState()

                if (data.recSettings!!.endDate != null) {
                    rangeEndValue = LocalDate.parse(data.recSettings!!.endDate, GlobalVars.dateFormatterYYYYMMDD)
                    binding.rangeEndEt.setText(GlobalVars.dateFormatterShort.format(rangeEndValue))
                    if (workOrder!!.recSettings!!.fixedDate == "0") {
                        binding.rangeEndEt.setText(GlobalVars.dateFormatterNoYear.format(rangeEndValue))
                    }
                    else {
                        binding.rangeEndEt.setText(GlobalVars.dateFormatterShort.format(rangeEndValue))
                    }
                }

                if (data.recSettings!!.frequency != null) {
                    workOrder!!.recSettings!!.frequency = data.recSettings!!.frequency
                    binding.frequencyEt.setText(data.recSettings!!.frequency)
                }

                if (data.recSettings!!.minDays != null) {
                    workOrder!!.recSettings!!.minDays = data.recSettings!!.minDays
                    binding.minDaysBetweenEt.setText(data.recSettings!!.minDays)
                }

                if (data.recSettings!!.prefDay != null) {
                    workOrder!!.recSettings!!.prefDay = data.recSettings!!.prefDay

                    val prefDayIndex = data.recSettings!!.prefDay!!.toInt()

                    binding.preferredDaySpinner.setSelection(prefDayIndex)

                }


            }

        }

        enableDisableFields()

    }

    override fun onEmployeeCellClickListener(data: Employee) {
        editsMade = true
        workOrder!!.salesRep = data.ID
        workOrder!!.salesRepName = data.name
        binding.repSearch.setQuery(workOrder!!.custName, false)
        binding.repSearch.clearFocus()
        myView.hideKeyboard()
    }

    private fun enableDisableFields() {
        println("enableDisableFields")

        if (binding.renewalTypeSpinner.selectedItemPosition == 0) {
            binding.renewalCl.visibility = View.GONE
        }
        else {
            binding.renewalCl.visibility = View.VISIBLE
        }

        if (binding.hasRangeSwitch.isChecked) {
            binding.rangeCl.visibility = View.VISIBLE
        }
        else {
            binding.rangeCl.visibility = View.GONE
        }

        if (binding.hasFrequencySwitch.isChecked) {
            binding.frequencyCl.visibility = View.VISIBLE
        }
        else {
            binding.frequencyCl.visibility = View.GONE
        }

        if (binding.hasRangeSwitch.isChecked) {
            binding.hasFrequencySwitch.isEnabled = false
        }
        else {
            binding.hasFrequencySwitch.isEnabled = true
        }
        binding.hasFrequencySwitch.jumpDrawablesToCurrentState()

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
