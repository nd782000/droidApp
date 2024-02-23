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
import com.AdminMatic.databinding.FragmentNewEditContractBinding
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

interface TemplateCellClickListener {
    fun onTemplateCellClickListener(data:Template)
}

class NewEditContractFragment : Fragment(), AdapterView.OnItemSelectedListener, CustomerCellClickListener, EmployeeCellClickListener, TemplateCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var contract: Contract? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private var templateID = ""
    private var templateName = ""

    private var dateValue: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var deadlineValue: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private lateinit var rangeStartValue: LocalDate
    private lateinit var rangeEndValue: LocalDate


    private lateinit var chargeTypeAdapter: ArrayAdapter<String>
    private lateinit var paymentTermsAdapter: ArrayAdapter<String>
    private lateinit var depositTypesAdapter: ArrayAdapter<String>
    private lateinit var taxTypesAdapter: ArrayAdapter<String>
    private lateinit var departmentsAdapter: ArrayAdapter<String>
    private lateinit var crewsAdapter: ArrayAdapter<String>

    private lateinit var invoiceTypesArray: Array<String>
    private lateinit var invoiceTypesAdapter: ArrayAdapter<String>

    private lateinit var renewalTypesArray: Array<String>
    private lateinit var renewalTypesAdapter: ArrayAdapter<String>

    private lateinit var daysArray: Array<String>
    private lateinit var daysAdapter: ArrayAdapter<String>

    private var showingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable("contract")
        }
        if (contract != null) {
            editMode = true
            if (contract!!.recSettings == null) {
                contract!!.recSettings = ContractRecSettings(contract!!.ID, null, null, "", "", "0", "1")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contractNewEdit")
    }

    private var _binding: FragmentNewEditContractBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditContractBinding.inflate(inflater, container, false)
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_contract_bar, contract!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_contract_bar)
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
        postRequest1.tag = "contractNewEdit"
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
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    private fun layoutViews() {

        if (!editMode) {
            contract = Contract("0", "0")
            contract!!.recSettings = ContractRecSettings("0", null, null, "", "", "0", "1")
        }

        // Flag edits made false after all the views have time to set their states
        Timer("ContractEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        // Customer search
        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@NewEditContractFragment, false
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
                contract!!.customer = "0"
                contract!!.custName = ""
                myView.hideKeyboard()
                binding.customerSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
            }

            binding.customerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.customerSearch.setQuery(contract!!.custName, false)
                    binding.customerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        if (!contract!!.custName.isNullOrBlank()) {
            binding.customerSearch.setQuery(contract!!.custName!!, false)
        }

        // Template search
        if (!editMode) {
            if (!editMode) {
                binding.templateSearchRv.apply {
                    layoutManager = LinearLayoutManager(activity)

                    adapter = activity?.let {
                        TemplatesAdapter(
                            GlobalVars.templates!!,
                            this@NewEditContractFragment
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

        if (contract!!.title.isNotBlank()) {
            binding.titleEditText.setText(contract!!.title)
        }

        // Date
        binding.dateEditText.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(dateValue.year, dateValue.monthValue-1, dateValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    dateValue = LocalDate.of(year, month+1, dayOfMonth)
                    binding.dateEditText.setText(dateValue.format(GlobalVars.dateFormatterShort))
                    contract!!.contractDate = dateValue.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        if (contract!!.contractDate != "" && contract!!.contractDate != "" && contract!!.contractDate != "0000-00-00") {
            println("date being parsed: ${contract!!.contractDate}")
            dateValue = LocalDate.parse(contract!!.contractDate, GlobalVars.dateFormatterYYYYMMDD)
            println("date being set: ${dateValue.format(GlobalVars.dateFormatterShort)}")

            binding.dateEditText.setText(dateValue.format(GlobalVars.dateFormatterShort))
        }

        // Sales rep search
        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@NewEditContractFragment
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
                contract!!.salesRep = ""
                contract!!.repName = ""
                myView.hideKeyboard()
                binding.repSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.repSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.repSearch.setQuery(contract!!.repName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        if (!contract!!.repName.isNullOrBlank()) {
            binding.repSearch.setQuery(contract!!.repName!!, false)
        }

        // Charge Type Spinner
        val chargeTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))
        chargeTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            chargeTypeArray
        )
        binding.chargeTypeSpinner.adapter = chargeTypeAdapter
        binding.chargeTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.chargeType == "") {
            contract!!.chargeType = GlobalVars.defaultFields!!.defCharge
        }
        binding.chargeTypeSpinner.setSelection(contract!!.chargeType!!.toInt() - 1)


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
        binding.invoiceTermsSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.paymentTermsID == "") {
            contract!!.paymentTermsID = GlobalVars.defaultFields!!.defPayTerms
        }


        if (contract!!.paymentTermsID != "") {
            GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
                if (e.ID == contract!!.paymentTermsID) {
                    binding.invoiceTermsSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.invoiceTermsSpinner.setSelection(0)
        }

        /*
        GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.paymentTermsID) {
                binding.invoiceTermsSpinner.setSelection(i)
            }
        }

         */


        // Deposit Type Spinner
        val depositTypesList = mutableListOf<String>()
        GlobalVars.depositTypes!!.forEach {
            //println("Name: ${it.name}")
            depositTypesList.add(it.name)
        }
        depositTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            depositTypesList
        )
        binding.depositTypeSpinner.adapter = depositTypesAdapter
        binding.depositTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.depositType == "") {
            contract!!.depositType = GlobalVars.defaultFields!!.defDeposit
        }

        GlobalVars.depositTypes!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.depositType) {
                binding.depositTypeSpinner.setSelection(i)
            }
        }

        // Sales Tax Spinner
        val taxTypesList = mutableListOf<String>()
        GlobalVars.salesTaxTypes!!.forEach {
            //println("Name: ${it.name}")
            taxTypesList.add(it.nameAndRate!!)
        }
        taxTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            taxTypesList
        )
        binding.taxSpinner.adapter = taxTypesAdapter
        binding.taxSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.salesTax == "") {
            contract!!.salesTax = GlobalVars.defaultFields!!.defTaxRate
        }

        GlobalVars.salesTaxTypes!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.salesTax) {
                binding.taxSpinner.setSelection(i)
            }
        }

        // Show more button
        binding.woConfigCl.visibility = View.GONE
        binding.showWoConfigText.setOnClickListener {
            //showingMore = !showingMore

            if (showingMore) {
                binding.showWoConfigText.text = getString(R.string.show_wo_config)
                binding.woConfigCl.visibility = View.GONE
                showingMore = false

            }
            else {
                binding.showWoConfigText.text = getString(R.string.hide_wo_config)
                binding.woConfigCl.visibility = View.VISIBLE
                showingMore = true
            }
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
        binding.departmentSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.sugDepartment != "") {
            GlobalVars.departments!!.forEachIndexed { i, e ->
                if (e.ID == contract!!.sugDepartment) {
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
        binding.crewSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.sugCrew != "") {
            GlobalVars.crews!!.forEachIndexed { i, e ->
                if (e.ID == contract!!.sugCrew) {
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
        binding.invoiceTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.sugInvoice == "") {
            contract!!.sugInvoice = GlobalVars.defaultFields!!.defInvoice
        }

        binding.invoiceTypeSpinner.setSelection(contract!!.sugInvoice!!.toInt() - 1)

        // Deadline
        binding.deadlineEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(deadlineValue.year, deadlineValue.monthValue-1, deadlineValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    deadlineValue = LocalDate.of(year, month+1, dayOfMonth)
                    binding.deadlineEt.setText(deadlineValue.format(GlobalVars.dateFormatterShort))
                    contract!!.sugDeadline = deadlineValue.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        println("contract!!.deadline: ${contract!!.sugDeadline}")
        if (contract!!.sugDeadline != null && contract!!.sugDeadline != "" && contract!!.sugDeadline != "0000-00-00") {
            println("SETTING DEADLINE")
            deadlineValue = LocalDate.parse(contract!!.sugDeadline, GlobalVars.dateFormatterYYYYMMDD)
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
        binding.renewalTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.sugRenew == "1") {
            binding.renewalTypeSpinner.setSelection(1)
        }
        else {
            binding.renewalTypeSpinner.setSelection(0)
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
                contract!!.recSettings!!.startDate = null
                contract!!.recSettings!!.endDate = null
                //binding.hasFrequencySwitch.isFocusableInTouchMode = true

            }

            enableDisableFields()
        }

        if (contract!!.recSettings!!.startDate != null || contract!!.recSettings!!.endDate != null) {
            binding.hasRangeSwitch.isChecked = true
        }
        else {
            binding.hasRangeSwitch.isChecked = false
        }

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
                        contract!!.recSettings!!.startDate = rangeStartValue.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    else {
                        com.example.AdminMatic.globalVars.simpleAlert(
                            com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error),
                            com.example.AdminMatic.myView.context.getString(R.string.dialogue_start_time_after_stop))

                    }
                }
            })
        }

        if (contract!!.recSettings!!.startDate != null) {
            rangeStartValue = LocalDate.parse(contract!!.recSettings!!.startDate, GlobalVars.dateFormatterYYYYMMDD)

            if (contract!!.recSettings!!.fixedDate == "0") {
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
                        contract!!.recSettings!!.endDate = rangeEndValue.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    else {
                        com.example.AdminMatic.globalVars.simpleAlert(
                            com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error),
                            com.example.AdminMatic.myView.context.getString(R.string.dialogue_stop_time_before_start))

                    }
                }
            })
        }

        if (contract!!.recSettings!!.endDate != null) {
            rangeEndValue = LocalDate.parse(contract!!.recSettings!!.endDate, GlobalVars.dateFormatterYYYYMMDD)

            if (contract!!.recSettings!!.fixedDate == "0") {
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
                contract!!.recSettings!!.fixedDate = "0"
                binding.rangeStartText.setText(R.string.season_start_label)
                binding.rangeEndText.setText(R.string.season_end_label)
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterMonthDay))
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterMonthDay))
            }
            else {
                contract!!.recSettings!!.fixedDate = "1"
                binding.rangeStartText.setText(R.string.range_start_label)
                binding.rangeEndText.setText(R.string.range_end_label)
                binding.rangeStartEt.setText(rangeStartValue.format(GlobalVars.dateFormatterShort))
                binding.rangeEndEt.setText(rangeEndValue.format(GlobalVars.dateFormatterShort))
            }
        }

        if (contract!!.recSettings!!.fixedDate == "0" && binding.hasRangeSwitch.isChecked) {
            binding.promptToRepeatSwitch.isChecked = true
        }
        else {
            binding.promptToRepeatSwitch.isChecked = false
        }

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
                contract!!.recSettings!!.frequency = "0"
            }
            enableDisableFields()
        }

        if (contract!!.recSettings!!.frequency != "0" && contract!!.recSettings!!.frequency != "") {
            binding.promptToRepeatSwitch.isChecked = true
        }
        else {
            binding.promptToRepeatSwitch.isChecked = false
        }

        // Frequency
        binding.frequencyEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.frequencyEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
                if (s.length > 0) {
                    val valueInt = s.toString().toInt()
                    if (valueInt != 0 && valueInt % 7 == 0) {
                        binding.preferredDaySpinner.isEnabled = true
                        //binding.preferredDaySpinner.alpha = 0
                    } else {
                        binding.preferredDaySpinner.isEnabled = false
                        binding.preferredDaySpinner.setSelection(0)
                        contract!!.recSettings!!.prefDay = "0"
                    }
                }
                else {
                    binding.preferredDaySpinner.isEnabled = false
                }
            }
        })

        if (contract!!.recSettings!!.frequency != "") {
            binding.frequencyEt.setText(contract!!.recSettings!!.frequency)
            binding.hasFrequencySwitch.isChecked = true
        }
        else {
            binding.hasFrequencySwitch.isChecked = false
            binding.frequencyEt.setText("7")
        }

        if (binding.frequencyEt.text.length > 0) {

            if (binding.frequencyEt.text.toString().toInt() % 7 == 0) {
                binding.preferredDaySpinner.isEnabled = true
                //binding.preferredDaySpinner.alpha = 0
            } else {
                binding.preferredDaySpinner.isEnabled = false
                binding.preferredDaySpinner.setSelection(0)
                contract!!.recSettings!!.prefDay = "0"
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

        if (contract!!.recSettings!!.minDays != "") {
            binding.minDaysBetweenEt.setText(contract!!.recSettings!!.minDays)
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
        binding.preferredDaySpinner.onItemSelectedListener = this@NewEditContractFragment

        if (contract!!.recSettings!!.prefDay!! != "") {
            binding.preferredDaySpinner.setSelection(contract!!.recSettings!!.prefDay!!.toInt())
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

        binding.notesEt.setText(contract!!.notes)

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateContract()
            }
        }

        binding.templateSearchRv.visibility = View.INVISIBLE
        binding.salesRepSearchRv.visibility = View.INVISIBLE
        binding.customerSearchRv.visibility = View.INVISIBLE

        // ===== POPULATE FIELDS =====
        /*
        if (editMode) {

            binding.customerSearch.setQuery(contract!!.custName, false)
            binding.customerSearchRv.visibility = View.INVISIBLE

            binding.titleEditText.setText(contract!!.title)


            if (!contract!!.chargeType.isNullOrBlank()) {
                binding.chargeTypeSpinner.setSelection(contract!!.chargeType!!.toInt()-1)
            }


            if (!contract!!.paymentTermsID.isNullOrBlank()) {

                for (i in 0 until GlobalVars.paymentTerms!!.size) {
                    if (contract!!.paymentTermsID == GlobalVars.paymentTerms!![i].ID) {
                        binding.invoiceTermsSpinner.setSelection(i)
                        break
                    }
                }

            }

            binding.repSearch.setQuery(contract!!.repName, false)
            binding.salesRepSearchRv.visibility = View.INVISIBLE

            binding.notesEt.setText(contract!!.notes)
        }

         */

        enableDisableFields()

        editsMade = false

        //binding.woConfigCl.visibility = View.GONE

        hideProgressView()

        println("finished layoutviews")
    }


    private fun validateFields(): Boolean {

        if (contract!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        if (binding.titleEditText.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_wo_set_title))
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

    private fun updateContract() {
        println("updateContract")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contract.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("New contract response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        val newContractID: String = gson.fromJson(parentObject["contractID"].toString(), String::class.java)
                        contract!!.ID = newContractID
                        globalVars.playSaveSound(myView.context)
                        editsMade = false


                        if (editMode) {
                            myView.findNavController().navigateUp()
                        } else {
                            val directions = NewEditContractFragmentDirections.navigateToContract(contract!!.ID)
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

                contract!!.notes = binding.notesEt.text.toString()
                contract!!.title = binding.titleEditText.text.toString()
                if (contract!!.total.isNullOrBlank()) {
                    contract!!.total = "0"
                }

                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["contractID"] = contract!!.ID
                if (contract!!.ID == "0") {
                    params["templateID"] = templateID
                }
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["customer"] = contract!!.customer.toString()
                params["chargeType"] = contract!!.chargeType.toString()
                params["status"] = contract!!.status
                params["total"] = contract!!.total.toString()
                params["notes"] = contract!!.notes.toString()
                params["salesTax"] = GlobalVars.salesTaxTypes!![binding.taxSpinner.selectedItemPosition].ID


                if (contract!!.salesRep != "0") {
                    params["salesRep"] = contract!!.salesRep.toString()
                    params["repName"] = contract!!.repName.toString()
                }
                else {
                    params["salesRep"] = ""
                    params["repName"] = ""
                }

                if (binding.dateEditText.text.isNotBlank()) {
                    params["contractDate"] = GlobalVars.dateFormatterYYYYMMDD.format(dateValue)
                }

                if (binding.deadlineEt.text.isNotBlank()) {
                    params["sugDeadline"] = GlobalVars.dateFormatterYYYYMMDD.format(deadlineValue)
                }

                if (contract!!.custName != null) {
                    params["customerName"] = contract!!.custName.toString()
                }
                else {
                    params["customerName"] = ""
                }

                params["title"] = contract!!.title

                if (contract!!.repSignature != null) {
                    params["companySigned"] = contract!!.repSignature.toString()
                }
                if (contract!!.customerSigned != null) {
                    params["customerSigned"] = contract!!.customerSigned.toString()
                }
                if (contract!!.lead != null) {
                    params["leadID"] = contract!!.lead.toString()
                }
                params["paymentTermsID"] = contract!!.paymentTermsID.toString()

                //params["contractDate"] = contract!!.contractDate.toString()
                params["depositType"] = contract!!.depositType.toString()
                params["sugDepartment"] = contract!!.sugDepartment.toString()
                params["sugCrew"] = contract!!.sugCrew.toString()
                params["sugInvoice"] = contract!!.sugInvoice.toString()
                params["sugRenew"] = contract!!.sugRenew.toString()

                if (contract!!.sugRenew == "1") {
                    contract!!.recSettings!!.startDate = GlobalVars.dateFormatterYYYYMMDD.format(rangeStartValue)
                    contract!!.recSettings!!.endDate = GlobalVars.dateFormatterYYYYMMDD.format(rangeEndValue)

                    if (binding.hasFrequencySwitch.isChecked) {
                        contract!!.recSettings!!.frequency = binding.frequencyEt.text.trim().toString()
                    }
                    else {
                        contract!!.recSettings!!.frequency = ""
                    }
                    contract!!.recSettings!!.minDays = binding.minDaysBetweenEt.text.trim().toString()
                    contract!!.recSettings!!.prefDay = binding.preferredDaySpinner.selectedItemPosition.toString()

                    if (binding.promptToRepeatSwitch.isChecked) {
                        contract!!.recSettings!!.fixedDate = "0"
                    }
                    else {
                        contract!!.recSettings!!.fixedDate = "1"
                    }
                }



                params["recSettings"] = Gson().toJson(contract!!.recSettings!!)



                //params["companySigned"] = contract!!.repSignature.toString()
                //params["customerSigned"] = contract!!.customerSignature.toString()

                println("params = $params")
                return params


                /*
                if self.contract.lead != nil{
                    "contractID": self.contract.ID,
                    "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId),
                    "customer": self.contract.customerID!,
                    "salesRep": rep,
                    "chargeType": contract.chargeType! ,
                    "status": self.contract.status,
                    "total":self.contract.total!,
                    "notes":notes, "repName":repName,
                    "customerName":customerName,
                    "title":self.contract.title,
                    "companySigned":self.contract.repSignature!,
                    "customerSigned":self.contract.customerSignature!,
                    "leadID":self.contract.lead!.ID,
                    "paymentTermsID":self.contract.paymentTerms!,
                    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!] as! [String : String]
                }else{
                    "contractID": self.contract.ID,
                    "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId),
                    "customer": self.contract.customerID!,
                    "salesRep": rep,
                    "chargeType": contract.chargeType ,
                    "status": self.contract.status,
                    "total":self.contract.total,
                    "notes":notes,
                    "repName":repName,
                    "customerName":customerName,
                    "title":self.contract.title,
                    "companySigned":self.contract.repSignature,
                    "customerSigned":self.contract.customerSignature,
                    "paymentTermsID":self.contract.paymentTerms!,
                    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!] as! [String : String]
                }

                 */


            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true
        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                val positionPlus1 = position+1
                contract!!.chargeType = positionPlus1.toString()
            }

            R.id.invoice_terms_spinner -> {
                contract!!.paymentTermsID = GlobalVars.paymentTerms?.get(position)?.ID.toString()
                //println("Selected Payment terms ID: ${GlobalVars.paymentTerms?.get(position)?.ID.toString()}")
            }

            R.id.deposit_type_spinner -> {
                contract!!.depositType = GlobalVars.depositTypes?.get(position)?.ID.toString()
            }

            R.id.tax_spinner -> {
                contract!!.salesTax = GlobalVars.salesTaxTypes?.get(position)?.ID.toString()
            }

            R.id.department_spinner -> {
                contract!!.sugDepartment = GlobalVars.departments?.get(position)?.ID.toString()
            }

            R.id.crew_spinner -> {
                contract!!.sugCrew = GlobalVars.crews?.get(position)?.ID.toString()
            }

            R.id.invoice_type_spinner -> {
                contract!!.sugInvoice = (position+1).toString()
            }

            R.id.renewal_type_spinner -> {
                contract!!.sugRenew = position.toString()
                enableDisableFields()
            }
        }

    }

    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        contract!!.customer = data.ID
        contract!!.custName = data.sysname

        if (!data.taxID.isNullOrBlank()) {
            contract!!.salesTax = data.taxID
        }
        if (!data.termsID.isNullOrBlank()) {
            contract!!.paymentTermsID = data.termsID
        }

        GlobalVars.salesTaxTypes!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.salesTax) {
                binding.taxSpinner.setSelection(i)
            }
        }

        GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.paymentTermsID) {
                binding.invoiceTermsSpinner.setSelection(i)
            }
        }

        binding.customerSearch.setQuery(contract!!.custName, false)
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

        contract!!.chargeType = data.chargeType
        println("charge type: ${data.chargeType}")
        binding.chargeTypeSpinner.setSelection(data.chargeType!!.toInt()-1)

        contract!!.depositType = data.depositType
        GlobalVars.depositTypes!!.forEachIndexed { i, e ->
            if (e.ID == data.depositType) {
                binding.depositTypeSpinner.setSelection(i)
            }
        }

        contract!!.sugDepartment = data.departmentID
        GlobalVars.departments!!.forEachIndexed { i, e ->
            if (e.ID == data.departmentID) {
                binding.departmentSpinner.setSelection(i)
            }
        }

        contract!!.sugCrew = data.mainCrew
        GlobalVars.crews!!.forEachIndexed { i, e ->
            if (e.ID == data.mainCrew) {
                binding.crewSpinner.setSelection(i)
            }
        }

        contract!!.sugInvoice = data.invoiceType
        println("invoice type: ${data.invoiceType}")

        binding.invoiceTypeSpinner.setSelection(data.invoiceType!!.toInt() - 1)

        GlobalVars.paymentTerms!!.forEachIndexed { i, e ->
            if (e.ID == contract!!.paymentTermsID) {
                binding.invoiceTermsSpinner.setSelection(i)
            }
        }

        contract!!.sugRenew = data.renew
        println("renewal type: ${data.invoiceType}")
        binding.renewalTypeSpinner.setSelection(data.renew!!.toInt())

        if (data.renew == "1") {
            if (data.recSettings != null) {
                contract!!.recSettings = data.recSettings

                if (data.recSettings!!.fixedDate == "1") {
                    binding.promptToRepeatSwitch.isChecked = false
                }
                else {
                    binding.promptToRepeatSwitch.isChecked = true
                }

                if (data.recSettings!!.startDate != null) {
                    binding.hasRangeSwitch.isChecked = true
                    rangeStartValue = LocalDate.parse(data.recSettings!!.startDate, GlobalVars.dateFormatterYYYYMMDD)
                    binding.rangeStartEt.setText(GlobalVars.dateFormatterShort.format(rangeStartValue))

                    if (contract!!.recSettings!!.fixedDate == "0") {
                        binding.rangeStartEt.setText(GlobalVars.dateFormatterNoYear.format(rangeStartValue))
                    }
                    else {
                        binding.rangeStartEt.setText(GlobalVars.dateFormatterShort.format(rangeStartValue))
                    }
                }
                else {
                    binding.hasRangeSwitch.isChecked = false
                }

                if (data.recSettings!!.endDate != null) {
                    rangeEndValue = LocalDate.parse(data.recSettings!!.endDate, GlobalVars.dateFormatterYYYYMMDD)
                    binding.rangeEndEt.setText(GlobalVars.dateFormatterShort.format(rangeEndValue))
                    if (contract!!.recSettings!!.fixedDate == "0") {
                        binding.rangeEndEt.setText(GlobalVars.dateFormatterNoYear.format(rangeEndValue))
                    }
                    else {
                        binding.rangeEndEt.setText(GlobalVars.dateFormatterShort.format(rangeEndValue))
                    }
                }

                if (data.recSettings!!.frequency != null) {
                    contract!!.recSettings!!.frequency = data.recSettings!!.frequency
                    binding.frequencyEt.setText(data.recSettings!!.frequency)
                }

                if (data.recSettings!!.minDays != null) {
                    contract!!.recSettings!!.minDays = data.recSettings!!.minDays
                    binding.minDaysBetweenEt.setText(data.recSettings!!.minDays)
                }

                if (data.recSettings!!.prefDay != null) {
                    contract!!.recSettings!!.prefDay = data.recSettings!!.prefDay

                    val prefDayIndex = data.recSettings!!.prefDay!!.toInt()

                    binding.preferredDaySpinner.setSelection(prefDayIndex)

                }


            }

        }

        enableDisableFields()

    }

    override fun onEmployeeCellClickListener(data: Employee) {
        editsMade = true
        contract!!.salesRep = data.ID
        contract!!.repName = data.name
        binding.repSearch.setQuery(contract!!.custName, false)
        binding.repSearch.clearFocus()
        myView.hideKeyboard()
    }

    private fun enableDisableFields() {
        println("enableDisableFields")

        if (contract!!.sugRenew == "0") {
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