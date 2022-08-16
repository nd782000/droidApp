package com.example.AdminMatic

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_item_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

interface ContractTaskCellClickListener {
    fun onContractTaskCellClickListener(data:ContractTask)
}

interface SearchItemCellClickListener {
    fun onSearchItemCellClickListener(data:SearchItem)
}


class ContractItemFragment : Fragment(), ContractTaskCellClickListener, SearchItemCellClickListener, AdapterView.OnItemSelectedListener {
    private var addMode: Boolean? = null
    private var editMode: Boolean? = false
    private var editsMade: Boolean = false
    private var itemsList: MutableList<Item> = mutableListOf()
    private var chargeTypeArray:Array<String> = arrayOf("No Charge", "Flat", "T & M")

    private  var contractItem: ContractItem? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private lateinit var contractItemSearch: androidx.appcompat.widget.SearchView
    private lateinit var contractItemRecycler: RecyclerView
    private lateinit var hideQtySwitch: SwitchCompat
    private lateinit var taxableSwitch: SwitchCompat
    private lateinit var chargeSpinner: Spinner
    private lateinit var submitBtn: Button


    private lateinit var qtyEt: EditText
    private lateinit var priceEt: EditText
    private lateinit var totalEt: EditText
    private lateinit var recycler: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractItem = it.getParcelable("contractItem")
            addMode = it.getBoolean("addMode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_contract, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_contract_item, container, false)

        globalVars = GlobalVars()

        if (addMode == true) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.add_item)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_item_number, contractItem!!.ID)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle("Unsaved Changes")
                    builder.setMessage("Go back without saving?")
                    builder.setPositiveButton("YES") { _, _ ->
                        parentFragmentManager.popBackStackImmediate()
                    }
                    builder.setNegativeButton("NO") { _, _ ->
                    }
                    builder.show()
                }else{
                    parentFragmentManager.popBackStackImmediate()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract Item View")

        //Todo: Make search view recycler appear when search is focused

        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE

        allCl = myView.findViewById(R.id.all_cl)
        contractItemSearch = myView.findViewById(R.id.contract_item_search)
        contractItemRecycler = myView.findViewById(R.id.contract_item_search_rv)
        //contractItemRecycler.visibility = View.GONE
        hideQtySwitch = myView.findViewById(R.id.contract_item_hide_qty_switch)
        taxableSwitch = myView.findViewById(R.id.contract_item_taxable_switch)
        chargeSpinner = myView.findViewById(R.id.contract_item_charge_spinner)
        submitBtn = myView.findViewById(R.id.contract_item_submit_btn)
        qtyEt = myView.findViewById(R.id.contract_item_qty_val_et)
        priceEt = myView.findViewById(R.id.contract_item_price_val_et)
        totalEt = myView.findViewById(R.id.contract_item_total_val_et)
        recycler = myView.findViewById(R.id.contract_item_tasks_rv)

        //contractItemSearch.isSubmitButtonEnabled = false

        if (addMode == true) {
            recycler.visibility = View.GONE
            editMode = true

        }
        else {
            contractItemSearch.isEnabled = false
            chargeSpinner.isEnabled = false
            qtyEt.isEnabled = false
            priceEt.isEnabled = false
            totalEt.isEnabled = false
            hideQtySwitch.isEnabled = false
            taxableSwitch.isEnabled = false


            if (contractItem!!.hideUnits == "1") {
                hideQtySwitch.isChecked = true
            }

            if (contractItem!!.taxType == "1") {
                taxableSwitch.isChecked = true
            }
            //contractItemSearch.setQuery(contractItem!!.name, false)

            recycler.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = activity?.let {
                    contractItem!!.tasks?.let { it1 ->
                        ContractTasksAdapter(
                            it1.toMutableList(),
                            context,
                            this@ContractItemFragment
                        )
                    }
                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                recycler.addItemDecoration(itemDecoration)
                //(adapter as ContractTasksAdapter).notifyDataSetChanged()
            }

        }

        qtyEt.setText(contractItem!!.qty)

        priceEt.setText(contractItem!!.price)
        totalEt.setText(contractItem!!.total)


        // Set up fields


        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray
        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        chargeSpinner.adapter = adapter
        chargeSpinner.setSelection(contractItem!!.chargeType.toInt() - 1)
        chargeSpinner.onItemSelectedListener = this@ContractItemFragment

        hideQtySwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                contractItem!!.hideUnits = "1"
            }
            else {
                contractItem!!.hideUnits = "0"
            }
        }

        taxableSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                contractItem!!.taxType = "1"
            }
            else {
                contractItem!!.taxType = "0"
            }
        }

        qtyEt.setRawInputType(Configuration.KEYBOARD_12KEY)
        qtyEt.setSelectAllOnFocus(true)
        qtyEt.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                qtyEt.clearFocus()
                myView.hideKeyboard()
                editsMade = true

                if (qtyEt.text.toString() != "") {
                    val costInput = qtyEt.text.toString().toDouble()
                    val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                    contractItem!!.qty = costInputTrimmed.toString()
                    qtyEt.setText(costInputTrimmed.toString())
                }
                else {
                    contractItem!!.qty = "0.00"
                    qtyEt.setText(contractItem!!.qty)
                }
                setTotalText()
                true
            } else {
                false
            }
        }

        priceEt.setRawInputType(Configuration.KEYBOARD_12KEY)
        priceEt.setSelectAllOnFocus(true)
        priceEt.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                priceEt.clearFocus()
                myView.hideKeyboard()
                editsMade = true

                if (priceEt.text.toString() != "") {
                    val costInput = priceEt.text.toString().toDouble()
                    val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                    contractItem!!.price = costInputTrimmed.toString()
                    priceEt.setText(costInputTrimmed.toString())
                }
                else {
                    contractItem!!.price = "0.00"
                    priceEt.setText(contractItem!!.price)
                }
                setTotalText()
                true
            } else {
                false
            }
        }


        submitBtn.setOnClickListener {
            println("Crew Click")

            if (contractItem!!.itemID.isEmpty() || contractItem!!.qty.isEmpty() || contractItem!!.price.isNullOrEmpty()) {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_fields_missing_title),getString(R.string.dialogue_fields_missing_body))
            }
            else {
                //submitContractItem()
            }
        }


        getItems()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contractItem")
    }

    private fun getItems(){
        println("getItems")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/items.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        //val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val items: JSONArray = parentObject.getJSONArray("items")
                    println("items = $items")
                    println("items count = ${items.length()}")



                    val gson = GsonBuilder().create()
                    itemsList = gson.fromJson(items.toString() , Array<Item>::class.java).toMutableList()


                    val searchItemsList = mutableListOf<SearchItem>()
                    itemsList.forEachIndexed {index, element ->
                        searchItemsList.add(SearchItem(element.name, index))
                    }


                    contractItemRecycler.apply {
                        layoutManager = LinearLayoutManager(activity)


                        adapter = activity?.let {
                            SearchItemsAdapter(
                                searchItemsList,
                                context,
                                this@ContractItemFragment
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        contractItemRecycler.addItemDecoration(itemDecoration)


                        //(adapter as SearchItemsAdapter).notifyDataSetChanged()


                        contractItemSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(query: String?): Boolean {
                                //customerRecyclerView.visibility = View.GONE
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as SearchItemsAdapter).filter.filter(newText)
                                if(newText == ""){
                                    contractItemRecycler.visibility = View.GONE
                                }else{
                                    contractItemRecycler.visibility = View.VISIBLE
                                }

                                return false
                            }

                        })

                    }
                    editsMade = false

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
        postRequest1.tag = "contractItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onContractTaskCellClickListener(data:ContractTask) {
        println("Clicked on contract task #${data.ID}")
        //val directions = ContractFragmentDirections.navigateContractToContractItem(data)
        //myView.findNavController().navigate(directions)
    }

    override fun onSearchItemCellClickListener(data:SearchItem) {
        println("Clicked on item #${data.index}")
        editsMade=true
        contractItemSearch.setQuery(itemsList[data.index].name, false)
        contractItemSearch.clearFocus()
        contractItem!!.itemID = itemsList[data.index].ID
        contractItemRecycler.visibility = View.GONE

        if (itemsList[data.index].price != "") {
            priceEt.setText(itemsList[data.index].price)
            contractItem!!.price = itemsList[data.index].price
            setTotalText()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("onItemSelected position = $position")
        editsMade = true

        if (contractItem != null){
            contractItem!!.chargeType = (position + 1).toString()
        }
    }

    private fun setTotalText() {
        //Return if any fields are null, or if
        if (contractItem!!.price.isNullOrBlank() || contractItem!!.qty.isBlank() ) {
            return
        }
        val totalCost = (contractItem!!.qty.toDouble() * contractItem!!.price!!.toDouble())
        contractItem!!.total = String.format("%.2f", totalCost)
        totalEt.setText(contractItem!!.total)
    }

    fun showProgressView() {
        allCl.visibility = View.INVISIBLE
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        allCl.visibility = View.VISIBLE
        pgsBar.visibility = View.INVISIBLE
    }


}