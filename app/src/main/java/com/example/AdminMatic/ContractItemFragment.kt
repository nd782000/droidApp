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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContractItemBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractItem = it.getParcelable("contractItem")
            addMode = it.getBoolean("addMode")
        }
    }

    private var _binding: FragmentContractItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()

        _binding = FragmentContractItemBinding.inflate(inflater, container, false)
        myView = binding.root

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

        //contractItemSearch.isSubmitButtonEnabled = false

        if (addMode == true) {
            binding.contractItemTasksRv.visibility = View.GONE
            editMode = true

        }
        else {
            binding.contractItemSearch.isEnabled = false
            binding.contractItemChargeSpinner.isEnabled = false
            binding.contractItemQtyValEt.isEnabled = false
            binding.contractItemPriceValEt.isEnabled = false
            binding.contractItemTotalValEt.isEnabled = false
            binding.contractItemHideQtySwitch.isEnabled = false
            binding.contractItemTaxableSwitch.isEnabled = false


            if (contractItem!!.hideUnits == "1") {
                binding.contractItemHideQtySwitch.isChecked = true
            }

            if (contractItem!!.taxType == "1") {
                binding.contractItemTaxableSwitch.isChecked = true
            }
            //contractItemSearch.setQuery(contractItem!!.name, false)

            binding.contractItemTasksRv.apply {
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
                binding.contractItemTasksRv.addItemDecoration(itemDecoration)
                (adapter as ContractTasksAdapter).notifyDataSetChanged()
            }

        }

        binding.contractItemQtyValEt.setText(contractItem!!.qty)

        binding.contractItemPriceValEt.setText(contractItem!!.price)
        binding.contractItemTotalValEt.setText(contractItem!!.total)


        // Set up fields


        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray
        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.contractItemChargeSpinner.adapter = adapter
        binding.contractItemChargeSpinner.setSelection(contractItem!!.chargeType.toInt() - 1)
        binding.contractItemChargeSpinner.onItemSelectedListener = this@ContractItemFragment

        binding.contractItemHideQtySwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                contractItem!!.hideUnits = "1"
            }
            else {
                contractItem!!.hideUnits = "0"
            }
        }

        binding.contractItemTaxableSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                contractItem!!.taxType = "1"
            }
            else {
                contractItem!!.taxType = "0"
            }
        }

        binding.contractItemQtyValEt.setRawInputType(Configuration.KEYBOARD_12KEY)
        binding.contractItemQtyValEt.setSelectAllOnFocus(true)
        binding.contractItemQtyValEt.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                binding.contractItemQtyValEt.clearFocus()
                myView.hideKeyboard()
                editsMade = true

                if (binding.contractItemQtyValEt.text.toString() != "") {
                    val costInput = binding.contractItemQtyValEt.text.toString().toDouble()
                    val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                    contractItem!!.qty = costInputTrimmed.toString()
                    binding.contractItemQtyValEt.setText(costInputTrimmed.toString())
                }
                else {
                    contractItem!!.qty = "0.00"
                    binding.contractItemQtyValEt.setText(contractItem!!.qty)
                }
                setTotalText()
                true
            } else {
                false
            }
        }

        binding.contractItemPriceValEt.setRawInputType(Configuration.KEYBOARD_12KEY)
        binding.contractItemPriceValEt.setSelectAllOnFocus(true)
        binding.contractItemPriceValEt.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                binding.contractItemPriceValEt.clearFocus()
                myView.hideKeyboard()
                editsMade = true

                if (binding.contractItemPriceValEt.text.toString() != "") {
                    val costInput = binding.contractItemPriceValEt.text.toString().toDouble()
                    val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                    contractItem!!.price = costInputTrimmed.toString()
                    binding.contractItemPriceValEt.setText(costInputTrimmed.toString())
                }
                else {
                    contractItem!!.price = "0.00"
                    binding.contractItemPriceValEt.setText(contractItem!!.price)
                }
                setTotalText()
                true
            } else {
                false
            }
        }


        binding.contractItemSubmitBtn.setOnClickListener {
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


                    binding.contractItemSearchRv.apply {
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
                        binding.contractItemSearchRv.addItemDecoration(itemDecoration)


                        (adapter as SearchItemsAdapter).notifyDataSetChanged()


                        binding.contractItemSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(query: String?): Boolean {
                                //customerRecyclerView.visibility = View.GONE
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as SearchItemsAdapter).filter.filter(newText)
                                if(newText == ""){
                                    binding.contractItemSearchRv.visibility = View.GONE
                                }else{
                                    binding.contractItemSearchRv.visibility = View.VISIBLE
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
        binding.contractItemSearch.setQuery(itemsList[data.index].name, false)
        binding.contractItemSearch.clearFocus()
        contractItem!!.itemID = itemsList[data.index].ID
        binding.contractItemSearchRv.visibility = View.GONE

        if (itemsList[data.index].price != "") {
            binding.contractItemPriceValEt.setText(itemsList[data.index].price)
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
        binding.contractItemTotalValEt.setText(contractItem!!.total)
    }

    fun showProgressView() {
        binding.allCl.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.allCl.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }


}