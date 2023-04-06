package com.example.AdminMatic

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContractItemBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
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
                        //parentFragmentManager.popBackStackImmediate()
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton("NO") { _, _ ->
                    }
                    builder.show()
                }else{
                    //parentFragmentManager.popBackStackImmediate()
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        setHasOptionsMenu(true)
        return myView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract Item View")

        //Todo: Make search view recycler appear when search is focused

        //contractItemSearch.isSubmitButtonEnabled = false

        println("contract item type: ${contractItem!!.type}")

        if (contractItem!!.type == "2") { // material
            binding.contractItemDescriptionCl.visibility = View.VISIBLE
            binding.contractItemImageCl.visibility = View.VISIBLE
            binding.contractItemTasksRv.visibility = View.GONE

            if (contractItem!!.tasks != null) {
                binding.contractItemDescriptionEt.setText(contractItem!!.tasks!![0].taskDescription)


                if(contractItem!!.tasks!![0].images!!.isNotEmpty()){
                    Picasso.with(context)
                        .load(GlobalVars.thumbBase + contractItem!!.tasks!![0].images!![0].fileName)
                        .placeholder(R.drawable.ic_images) //optional
                        //.resize(imgWidth, imgHeight)         //optional
                        //.centerCrop()                        //optional
                        .into(binding.contractItemImageIv)                       //Your image view object.
                }

                if (contractItem!!.tasks!![0].images!!.size > 1){
                    binding.contractItemImageCountTv.text = getString(R.string.plus_x, contractItem!!.tasks!![0].images!!.size - 1)
                }else{
                    binding.contractItemImageCountTv.text = ""
                }
            }
        }
        else {
            binding.contractItemDescriptionCl.visibility = View.GONE
            binding.contractItemImageCl.visibility = View.GONE
            binding.contractItemTasksRv.visibility = View.VISIBLE
        }

        if (addMode == true) {
            binding.contractItemTasksRv.visibility = View.GONE
            enableEditMode(true)

        }
        else {
            enableEditMode(false)

            if (contractItem!!.hideUnits == "1") {
                binding.contractItemHideQtySwitch.isChecked = true
                binding.contractItemHideQtySwitch.jumpDrawablesToCurrentState()
            }

            if (contractItem!!.taxType == "1") {
                binding.contractItemTaxableSwitch.isChecked = true
                binding.contractItemTaxableSwitch.jumpDrawablesToCurrentState()
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

            if (contractItem!!.itemID.isEmpty() || contractItem!!.qty.isEmpty() || contractItem!!.price.isNullOrEmpty() || contractItem!!.qty == "0" || contractItem!!.qty == "0.00") {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_fields_missing_title),getString(R.string.dialogue_fields_missing_body))
            }
            else {
                enableEditMode(false)

                updateContractItem()
            }
        }


        getItems()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contract_item_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_contract_item_item) {
            if (GlobalVars.permissions!!.contractsEdit == "1") {
                enableEditMode(true)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_contracts_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contractItem")
    }

    private fun getItems(){
        println("getItems")
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
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val items: JSONArray = parentObject.getJSONArray("items")
                        println("items = $items")
                        println("items count = ${items.length()}")


                        val gson = GsonBuilder().create()
                        itemsList =
                            gson.fromJson(items.toString(), Array<Item>::class.java).toMutableList()


                        val searchItemsList = mutableListOf<SearchItem>()
                        itemsList.forEachIndexed { index, element ->
                            searchItemsList.add(SearchItem(element.name, index))
                        }


                        binding.contractItemSearchRv.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                SearchItemsAdapter(
                                    searchItemsList,
                                    this@ContractItemFragment
                                )
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(
                                    myView.context,
                                    DividerItemDecoration.VERTICAL
                                )
                            binding.contractItemSearchRv.addItemDecoration(itemDecoration)


                            (adapter as SearchItemsAdapter).notifyDataSetChanged()


                            binding.contractItemSearch.setOnQueryTextListener(object :
                                SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    //customerRecyclerView.visibility = View.GONE
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as SearchItemsAdapter).filter.filter(newText)
                                    if (newText == "") {
                                        binding.contractItemSearchRv.visibility = View.GONE
                                    } else {
                                        binding.contractItemSearchRv.visibility = View.VISIBLE
                                    }

                                    return false
                                }

                            })

                        }
                    }

                    if (!addMode!!) {
                        for (i in 0 until itemsList.size) {
                            if (itemsList[i].ID == contractItem!!.itemID) {
                                println("FOund it")
                                binding.contractItemSearch.setQuery(itemsList[i].name, false)
                                binding.contractItemSearchRv.visibility = View.INVISIBLE
                            }
                        }
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

    private fun updateContractItem(){
        println("updateContractItem")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contractItem.php"

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
                    println("update/contractItem parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        val gson = GsonBuilder().create()
                        contractItem = gson.fromJson(response, ContractItem::class.java)
                    }

                    editsMade = false

                    if (!editMode!!) {
                        myView.findNavController().navigateUp()
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

                var subcontractorString = ""
                if (contractItem!!.subcontractor != null) {
                    subcontractorString = contractItem!!.subcontractor.toString()
                }

                val params: MutableMap<String, String> = HashMap()
                params["contractItemID"] = contractItem!!.ID
                params["contractID"] = contractItem!!.contractID
                params["itemID"] = contractItem!!.itemID
                params["type"] = contractItem!!.type.toString()
                params["chargeType"] = contractItem!!.chargeType
                params["qty"] = contractItem!!.qty
                params["price"] = contractItem!!.price.toString()
                params["total"] = contractItem!!.total.toString()
                params["name"] = contractItem!!.name
                params["subcontractor"] = subcontractorString
                params["hideUnits"] = contractItem!!.hideUnits.toString()
                params["materialDescription"] = binding.contractItemDescriptionEt.text.toString()
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID


                params["taxCode"] = contractItem!!.taxType.toString()
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
        contractItem!!.name = data.name
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

    private fun enableEditMode(value:Boolean) {
        editMode = value

        if (editMode == true) {
            binding.contractItemSearch.isEnabled = true
            binding.contractItemChargeSpinner.isEnabled = true
            binding.contractItemQtyValEt.isEnabled = true
            binding.contractItemPriceValEt.isEnabled = true
            binding.contractItemTotalValEt.isEnabled = true
            binding.contractItemHideQtySwitch.isEnabled = true
            binding.contractItemTaxableSwitch.isEnabled = true
            globalVars.enableSearchView(binding.contractItemSearch, true)
            binding.contractItemSubmitBtn.visibility = View.VISIBLE
        }
        else {
            binding.contractItemSearch.isEnabled = false
            binding.contractItemChargeSpinner.isEnabled = false
            binding.contractItemQtyValEt.isEnabled = false
            binding.contractItemPriceValEt.isEnabled = false
            binding.contractItemTotalValEt.isEnabled = false
            binding.contractItemHideQtySwitch.isEnabled = false
            binding.contractItemTaxableSwitch.isEnabled = false
            globalVars.enableSearchView(binding.contractItemSearch, false)
            binding.contractItemSubmitBtn.visibility = View.GONE
        }
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