package com.example.AdminMatic

import android.app.AlertDialog
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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditItemBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Timer
import kotlin.concurrent.schedule


class NewEditItemFragment : Fragment(), AdapterView.OnItemSelectedListener, ItemCellClickListener {

    //todo: add "select X" option to most spinners
    //todo: probably hide tax spinner while tax checkbox is off
    //todo: double check all things that should flag editsMade on
    //todo: clean up strings from old section

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var item:Item? = null

    private var editMode = false

    private var parentName = ""
    private var asOfValue: LocalDate = LocalDate.now(ZoneOffset.UTC)

    private var originalNameValue = ""


    private var showingAdvanced = false

    private lateinit var itemsList: MutableList<Item>

    private lateinit var itemTypesArray: Array<String>
    private lateinit var itemTypesAdapter: ArrayAdapter<String>

    private var unitTypesList = mutableListOf<String>()
    private lateinit var salesUnitAdapter: ArrayAdapter<String>
    private lateinit var taxTypesAdapter: ArrayAdapter<String>
    private lateinit var purchaseUnitAdapter: ArrayAdapter<String>
    private lateinit var estimateUnitAdapter: ArrayAdapter<String>
    private lateinit var departmentsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getParcelable("item")
        }
        if (item != null) {
            editMode = true
        }
        else {
            item = Item("0", "", "", "","", "", "", "", "", "0", "0")
        }

        originalNameValue = item!!.name


    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("itemNewEdit")
    }



    private var _binding: FragmentNewEditItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditItemBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade && editsMadeDelayPassed){
                    println("edits made")
                    val builder = AlertDialog.Builder(myView.context)
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_item_num, item!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_item)
        }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("on view created")

        getItems()
    }

    private fun getItems(){
        println("getItems")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/items.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Get Items Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val items: JSONArray = parentObject.getJSONArray("items")

                        val gson = GsonBuilder().create()
                        itemsList = gson.fromJson(items.toString(), Array<Item>::class.java).toMutableList()

                        layoutViews()
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
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "itemNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {

        println("LayoutViews()")

        Timer("ItemEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        // Type Spinner
        itemTypesArray = arrayOf(
            getString(R.string.item_type_service),
            getString(R.string.item_type_inventory_part),
            getString(R.string.item_type_non_inventory_part),
            getString(R.string.item_type_other_charge)
        )

        itemTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemTypesArray
        )
        binding.typeSpinner.adapter = itemTypesAdapter
        binding.typeSpinner.onItemSelectedListener = this@NewEditItemFragment

        binding.yesNoSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!editsMadeDelayPassed) {
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                item!!.subcontractor = "1"
            }
            else {
                item!!.subcontractor = "0"
            }

            hideShowFields()
        }

        // Item Name
        binding.nameEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.nameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.nameEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.name = binding.nameEt.text.trim().toString()
                checkUniqueName(false)
                populateFullName()
            }
        }

        // Sales Unit Spinner
        GlobalVars.unitTypes!!.forEach {
            unitTypesList.add(it.unitName)
        }

        salesUnitAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            unitTypesList
        )
        binding.salesUnitSpinner.adapter = salesUnitAdapter
        binding.salesUnitSpinner.onItemSelectedListener = this@NewEditItemFragment

        // Rate
        binding.rateEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.rateEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.rateEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.price = binding.rateEt.text.toString()
            }
            updateMarkupAndMargin()
        }

        // Sales Description
        //binding.salesDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.salesDescriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.salesDescriptionEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.salesDescription = binding.salesDescriptionEt.text.toString()
            }
        }

        // Advanced Settings Toggle
        binding.advancedSettingsCl.visibility = View.GONE
        binding.advancedSettingsText.setOnClickListener {
            if (showingAdvanced) {
                binding.advancedSettingsText.text = getString(R.string.advanced_settings)
                binding.advancedSettingsCl.visibility = View.GONE
                showingAdvanced = false
            }
            else {
                binding.advancedSettingsText.text = getString(R.string.hide_advanced_settings)
                binding.advancedSettingsCl.visibility = View.VISIBLE
                showingAdvanced = true
            }
        }

        // Parent Item Search
        binding.parentSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = activity?.let {
                ItemsAdapter(
                    itemsList, myView.context,
                    this@NewEditItemFragment, false
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.parentSearchRv.addItemDecoration(itemDecoration)

            binding.parentSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.parentSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as ItemsAdapter).filter.filter(newText)
                    if(newText == ""){
                        binding.parentSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.parentSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })

            val closeButton: View? = binding.parentSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.parentSearch.setQuery("", false)
                //contract!!.customer = "0"
                //contract!!.custName = ""
                item!!.parentID = "0"
                parentName = ""
                myView.hideKeyboard()
                binding.parentSearch.clearFocus()
                binding.parentSearchRv.visibility = View.INVISIBLE
            }

            binding.parentSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    //binding.parentSearch.setQuery(contract!!.custName, false)
                    binding.parentSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        // Taxable Switch
        binding.taxableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!editsMadeDelayPassed) {
                return@setOnCheckedChangeListener
            }

            editsMade = true

            if (isChecked) {
                item!!.taxable = "1"
                binding.taxRateSpinner.isEnabled = true
            }
            else {
                item!!.taxable = "0"
                binding.taxRateSpinner.isEnabled = false
            }
        }

        // Tax Rate Spinner
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
        binding.taxRateSpinner.adapter = taxTypesAdapter
        binding.taxRateSpinner.onItemSelectedListener = this@NewEditItemFragment

        // Cost
        binding.costEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.costEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.costEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.cost = binding.costEt.text.toString()
            }
            updateMarkupAndMargin()
        }

        // Vendors

        // Part Number
        binding.partNumberEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.partNumberEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.partNumberEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.partNum = binding.partNumberEt.text.toString()
            }
        }

        // Purchase Unit Spinner
        purchaseUnitAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            unitTypesList
        )
        binding.purchaseUnitSpinner.adapter = purchaseUnitAdapter
        binding.purchaseUnitSpinner.onItemSelectedListener = this@NewEditItemFragment

        // Purchase Description
        //binding.salesDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.purchaseDescriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.purchaseDescriptionEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.purchaseDescription = binding.purchaseDescriptionEt.text.toString()
            }
        }

        // Reorder Point
        binding.reorderPointEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.reorderPointEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.reorderPointEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.reorderMin = binding.reorderPointEt.text.toString()
            }
        }

        // Max Qty
        binding.maxEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.maxEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.maxEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.reorderMax = binding.maxEt.text.toString()
            }
        }

        // On Hand
        binding.onHandEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.onHandEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.onHandEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.onHand = binding.onHandEt.text.toString()
            }
        }

        // As Of
        binding.asOfEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(asOfValue.year, asOfValue.monthValue-1, asOfValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    asOfValue = LocalDate.of(year, month+1, dayOfMonth)
                    binding.asOfEt.setText(asOfValue.format(GlobalVars.dateFormatterShort))
                    item!!.asOf = asOfValue.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        // Estimate Unit Spinner
        estimateUnitAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            unitTypesList
        )
        binding.estimateUnitSpinner.adapter = estimateUnitAdapter
        binding.estimateUnitSpinner.onItemSelectedListener = this@NewEditItemFragment

        // Suggested Min Qty
        binding.suggestedMinEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.suggestedMinEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.suggestedMinEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.minQty = binding.suggestedMinEt.text.toString()
            }
        }

        // Estimate Notes
        //binding.salesDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.estimateNotesEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.estimateNotesEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.estNotes = binding.estimateNotesEt.text.toString()
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
        binding.departmentSpinner.onItemSelectedListener = this@NewEditItemFragment

        // Terms
        //binding.salesDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.termsEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.termsEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item!!.itemTerms = binding.termsEt.text.toString()
            }
        }

        binding.submitBtn.setOnClickListener {
            submit()
        }




        // Populate Fields

        if (!item!!.type.isNullOrBlank()) {
            binding.typeSpinner.setSelection(item!!.type!!.toInt()-1)
        }

        if (item!!.subcontractor == "1") {
            binding.yesNoSwitch.isChecked = true
            binding.yesNoSwitch.jumpDrawablesToCurrentState()
        }
        else {
            binding.yesNoSwitch.isChecked = false
            binding.yesNoSwitch.jumpDrawablesToCurrentState()
        }

        if (item!!.ID != "0") {
            binding.typeSpinner.isEnabled = false
            binding.yesNoSwitch.isEnabled = false
        }

        if (item!!.name.isNotBlank()) {
            binding.nameEt.setText(item!!.name)
        }

        if (!item!!.unit.isNullOrBlank()) {
            GlobalVars.unitTypes!!.forEachIndexed { i, e ->
                if (e.unitID == item!!.unit) {
                    binding.salesUnitSpinner.setSelection(i)
                }
            }
        }

        if (!item!!.price.isNullOrBlank()) {
            binding.rateEt.setText(item!!.price)
        }

        if (!item!!.salesDescription.isNullOrBlank()) {
            binding.salesDescriptionEt.setText(item!!.salesDescription)
        }

        if (!item!!.parentID.isNullOrBlank() && item!!.parentID != "0") {
            for (i in itemsList) {
                if (item!!.parentID == i.ID) {
                    binding.parentSearch.setQuery(i.name, false)
                    parentName = i.name
                    break
                }
            }
        }

        if (item!!.fullName.isNotBlank()) {
            binding.fullNameEt.setText(item!!.fullName)
        }

        if (!item!!.taxable.isNullOrBlank()) {
            if (item!!.taxable == "1") {
                binding.taxableSwitch.isChecked = true
                binding.taxRateSpinner.isEnabled = true
            }
            else {
                binding.taxableSwitch.isChecked = false
                binding.taxRateSpinner.isEnabled = false
            }
        }
        else {
            item!!.taxable = "0"
            binding.taxableSwitch.isChecked = false
            binding.taxRateSpinner.isEnabled = false
        }
        binding.taxableSwitch.jumpDrawablesToCurrentState()


        if (item!!.salesTaxID == "") {
            item!!.salesTaxID = GlobalVars.defaultFields!!.defTaxRate
        }

        GlobalVars.salesTaxTypes!!.forEachIndexed { i, e ->
            if (e.ID == item!!.salesTaxID) {
                binding.taxRateSpinner.setSelection(i)
            }
        }

        if (!item!!.cost.isNullOrBlank()) {
            binding.costEt.setText(item!!.cost)
        }

        if (!item!!.partNum.isNullOrBlank()) {
            binding.partNumberEt.setText(item!!.partNum)
        }

        if (!item!!.purchaseUnit.isNullOrBlank()) {
            GlobalVars.unitTypes!!.forEachIndexed { i, e ->
                if (e.unitID == item!!.purchaseUnit) {
                    binding.purchaseUnitSpinner.setSelection(i)
                }
            }
        }

        if (!item!!.purchaseDescription.isNullOrBlank()) {
            binding.purchaseDescriptionEt.setText(item!!.purchaseDescription)
        }

        if (!item!!.reorderMin.isNullOrBlank()) {
            binding.reorderPointEt.setText(item!!.reorderMin)
        }

        if (!item!!.reorderMax.isNullOrBlank()) {
            binding.maxEt.setText(item!!.reorderMax)
        }

        if (!item!!.onHand.isNullOrBlank()) {
            binding.onHandEt.setText(item!!.onHand)
        }

        if (!item!!.totalValue.isNullOrBlank()) {
            binding.totalEt.setText(item!!.totalValue)
        }

        if (item!!.asOf != null && item!!.asOf != "" && item!!.asOf != "0000-00-00") {
            println("SETTING AS OF DATE")
            asOfValue = LocalDate.parse(item!!.asOf, GlobalVars.dateFormatterYYYYMMDD)
            binding.asOfEt.setText(GlobalVars.dateFormatterShort.format(asOfValue))
        }

        if (!item!!.estUnit.isNullOrBlank()) {
            GlobalVars.unitTypes!!.forEachIndexed { i, e ->
                if (e.unitID == item!!.estUnit) {
                    binding.estimateUnitSpinner.setSelection(i)
                }
            }
        }

        if (!item!!.minQty.isNullOrBlank()) {
            binding.suggestedMinEt.setText(item!!.minQty)
        }

        if (!item!!.estNotes.isNullOrBlank()) {
            binding.estimateNotesEt.setText(item!!.estNotes)
        }

        if (item!!.depID != "") {
            GlobalVars.departments!!.forEachIndexed { i, e ->
                if (e.ID == item!!.depID) {
                    binding.departmentSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.departmentSpinner.setSelection(0)
        }

        if (!item!!.itemTerms.isNullOrBlank()) {
            binding.termsEt.setText(item!!.itemTerms)
        }

        if (item!!.active == "1") {
            binding.activeSwitch.isChecked = true
        }
        else {
            binding.activeSwitch.isChecked = false
        }
        binding.activeSwitch.jumpDrawablesToCurrentState()

        updateMarkupAndMargin()


        hideShowFields()

        hideProgressView()

    }

    private fun hideShowFields() {

        println("when: ${binding.typeSpinner.selectedItemPosition}")

        when (binding.typeSpinner.selectedItemPosition) {

            0 -> { // Service
                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_subcontractor)

                if (binding.yesNoSwitch.isChecked) {
                    println("Service Yes")
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.purchaseInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                }
                else {
                    println("Service No")
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.purchaseInfoCl.visibility = View.GONE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.GONE
                }

            }
            1 -> { // Inventory Part
                println("Inventory Part")
                binding.yesNoSwitch.visibility = View.GONE
                binding.yesNoTv.visibility = View.GONE

                binding.salesInfoCl.visibility = View.VISIBLE
                binding.purchaseInfoCl.visibility = View.VISIBLE
                binding.inventoryInfoCl.visibility = View.VISIBLE
                binding.estimateInfoCl.visibility = View.VISIBLE
                binding.markupMarginCl.visibility = View.VISIBLE

            }
            2 -> { // Non-inventory Part
                println("Non-inventory Part")

                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_specific_customer_job)

                binding.salesInfoCl.visibility = View.VISIBLE
                binding.purchaseInfoCl.visibility = View.VISIBLE
                binding.inventoryInfoCl.visibility = View.GONE
                binding.estimateInfoCl.visibility = View.VISIBLE
                binding.markupMarginCl.visibility = View.VISIBLE

            }
            3 -> { // Other Charge
                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_reimbursable)

                if (binding.yesNoSwitch.isChecked) {
                    println("Other Charge Yes")
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.purchaseInfoCl.visibility = View.GONE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.GONE
                }
                else {
                    println("Other Charge No")
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.purchaseInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                }

            }

        }
    }

    private fun populateFullName() {
        if (parentName.isNotBlank()) {
            item!!.fullName = parentName + ":" + item!!.name
        }
        else {
            item!!.fullName = ""
        }
        binding.fullNameEt.setText(item!!.fullName)
    }

    private fun updateMarkupAndMargin() {
        print("updateMarkupAndMargin")

        val markup:Float
        val margin:Float

        if (!item!!.price.isNullOrBlank() && !item!!.cost.isNullOrBlank()) {

            val price = item!!.price!!.toFloat()
            val cost = item!!.cost!!.toFloat()

            if (price > 0 && cost > 0) {
                markup = price/ cost * 100 - 100
                binding.markupValueTv.text = getString(R.string.x_percent, String.format("%.2f", markup))

                margin = 1 - (cost/price) * 100
                binding.marginValueTv.text = getString(R.string.x_percent, String.format("%.2f", margin))

            }
            else {
                binding.markupValueTv.text = "---"
                binding.marginValueTv.text = "---"
            }

        }
        else {
            binding.markupValueTv.text = "---"
            binding.marginValueTv.text = "---"
        }
    }

    private fun checkUniqueName(submitting:Boolean) {
        if (submitting) {
            showProgressView()
        }

        if (item!!.name == originalNameValue) {
            if (submitting) {
                submit()
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

                    val gson = GsonBuilder().create()
                    val result: Boolean = gson.fromJson(parentObject["unique"].toString(), Boolean::class.java)

                    println("Unique sysname check result: $result")

                    if (result) {
                        println("Unique true")
                        if (submitting) {
                            submit()
                        }

                    }
                    else {
                        globalVars.simpleAlert(myView.context,getString(R.string.duplicate_sysname_title),getString(R.string.duplicate_sysname_body, item!!.name))
                        println("Unique false")
                        item!!.name = ""
                        binding.nameEt.setText("")
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
                params["val"] = item!!.name
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "itemNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun submit() {

        if (!editsMade) {
            globalVars.simpleAlert(myView.context, getString(R.string.no_edits_made))
            return
        }

        if (item!!.name.isBlank()) {
            globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.enter_an_item_name))
            return
        }

        if (item!!.type == "2") {

            if (item!!.reorderMin == null) {
                item!!.reorderMin = ""
            }
            val stringTrimmed = item!!.reorderMin!!.replace('.'.toString(), "")

            if (stringTrimmed.isBlank()) {
                globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.enter_a_reorder_min))
                return
            }
        }

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/item.php"

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
                    val newItemID: String = gson.fromJson(parentObject["itemID"].toString(), String::class.java)
                    item!!.ID = newItemID

                    if (editMode) {
                        myView.findNavController().navigateUp()
                    }
                    else {
                        val directions = NewEditItemFragmentDirections.navigateToItem(item!!)
                        myView.findNavController().navigate(directions)
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
                params["ID"] = item!!.ID
                params["item"] = item!!.name
                params["type"] = item!!.type!!
                params["subcontractor"] = item!!.subcontractor
                params["parentID"] = item!!.parentID!!
                params["fullname"] = item!!.fullName
                params["unit"] = item!!.unit!!
                params["partNum"] = item!!.partNum!!
                params["cost"] = item!!.cost!!
                params["purchaseDescription"] = item!!.purchaseDescription!!
                params["purchaseUnit"] = item!!.purchaseUnit!!
                params["price"] = item!!.price!!
                params["tax"] = item!!.taxable!!
                params["salesTaxID"] = item!!.salesTaxID!!
                params["salesDescription"] = item!!.salesDescription!!
                params["reorderMin"] = item!!.reorderMin!!
                params["reorderMax"] = item!!.reorderMax!!
                params["onHand"] = item!!.onHand!!
                params["totalValue"] = item!!.totalValue!!
                params["asOf"] = item!!.asOf!!
                params["estUnit"] = item!!.estUnit!!
                params["minQty"] = item!!.minQty!!
                params["estNotes"] = item!!.estNotes!!
                params["depID"] = item!!.depID!!
                params["itemTerms"] = item!!.itemTerms!!
                params["active"] = item!!.active
                params["synced"] = item!!.synced

                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "itemNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }





    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        editsMade = true

        when (parent!!.id) {
            R.id.type_spinner -> {
                item!!.type = position.toString() + 1

                when (item!!.type) {
                    "1" -> {
                        binding.taxableSwitch.isChecked = false
                        binding.taxableSwitch.jumpDrawablesToCurrentState()
                        item!!.taxable = "0"
                    }
                    "2" -> {
                        binding.taxableSwitch.isChecked = true
                        binding.taxableSwitch.jumpDrawablesToCurrentState()
                        item!!.taxable = "1"

                        binding.yesNoSwitch.isChecked = false
                        binding.yesNoSwitch.jumpDrawablesToCurrentState()
                        item!!.subcontractor = "0"
                    }
                    "3" -> {
                        binding.taxableSwitch.isChecked = true
                        binding.taxableSwitch.jumpDrawablesToCurrentState()
                        item!!.taxable = "1"
                    }
                }

                hideShowFields()
            }
            R.id.sales_unit_spinner -> {
                item!!.unit = GlobalVars.unitTypes?.get(position)?.unitID
                item!!.unitName = GlobalVars.unitTypes?.get(position)?.unitName
            }
            R.id.tax_spinner -> {
                item!!.salesTaxID = GlobalVars.salesTaxTypes?.get(position)?.ID.toString()
            }
            R.id.purchase_unit_spinner -> {
                item!!.purchaseUnit = GlobalVars.unitTypes?.get(position)?.unitID
                item!!.purchaseUnitName = GlobalVars.unitTypes?.get(position)?.unitName
            }
            R.id.estimate_unit_spinner -> {
                item!!.estUnit = GlobalVars.unitTypes?.get(position)?.unitID
                item!!.estUnitName = GlobalVars.unitTypes?.get(position)?.unitName
            }
            R.id.department_spinner -> {
                item!!.depID = GlobalVars.departments?.get(position)?.ID.toString()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("todo")
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        println("HIDING PROGRESS VIEW")
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun onItemCellClickListener(data: Item) {
        editsMade = true
        item!!.parentID = data.ID
        parentName = data.name
        binding.parentSearch.setQuery(parentName, false)
        binding.parentSearch.clearFocus()
        populateFullName()
        myView.hideKeyboard()
    }

}