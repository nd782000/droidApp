package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCustomerBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class CustomerFragment : Fragment(), AdapterView.OnItemSelectedListener, LeadCellClickListener, ContractCellClickListener, WorkOrderCellClickListener, InvoiceCellClickListener, ImageCellClickListener {

    lateinit  var customerID: String

    lateinit  var customer: Customer

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var tableMode:String

    lateinit var adapter:ImagesAdapter

    private lateinit var yearAdapter:ArrayAdapter<String>
    private val yearList = mutableListOf<String>()
    private var yearValue = "1"

    lateinit var imageList: MutableList<Image>
    private var imagesLoaded:Boolean = false
    lateinit var loadMoreImageList: MutableList<Image>
    var refreshing = false
    private var initialViewsLaidOut = false


    lateinit var addBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("customer onCreate")
        arguments?.let {
            println("cust frag onCreate")
           // customer = it.getParcelable<Customer?>("customer")!!
            customerID = it.getString("customerID")!!
        }
    }

    private var _binding: FragmentCustomerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()
        _binding = FragmentCustomerBinding.inflate(inflater, container, false)
        myView = binding.root

        imageList = mutableListOf()
        adapter = ImagesAdapter(imageList,myView.context, true, this)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.customer_num, customerID)
        setHasOptionsMenu(true)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCustomer()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("customer")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.customer_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_customer_item) {
            if (GlobalVars.permissions!!.customersEdit == "1") {
                val directions = CustomerFragmentDirections.navigateToNewEditCustomer(customer.ID)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCustomer(){
       // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customer.php"

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
                            val customerArray =
                                gson.fromJson(parentObject.toString(), CustomerArray::class.java)

                            customer = customerArray.customers[0]
                        }

                        getLeads()

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
                //params["ID"] = customer!!.ID
                params["ID"] = customerID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }





    private fun getLeads(){
        println("getLeads")


        // println("pgsBar = $pgsBar")


       //showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/leads.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                //hideProgressView()


                try {

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val leads: JSONArray = parentObject.getJSONArray("leads")
                        println("leads = $leads")
                        println("leads count = ${leads.length()}")


                        val gson = GsonBuilder().create()
                        val leadsList =
                            gson.fromJson(leads.toString(), Array<Lead>::class.java).toMutableList()


                        val leadAdapter = LeadsAdapter(leadsList, this.myView.context, this, true)

                        binding.customerLeadsRv.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.customerLeadsRv.adapter = leadAdapter


                    }
                    getContracts()

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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["custID"] = customer.ID
                params["year"] = yearValue
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }




    override fun onLeadCellClickListener(data:Lead) {

        data.let {
            val directions = CustomerFragmentDirections.navigateCustomerToLead(it.ID)
            myView.findNavController().navigate(directions)
        }


    }




    private fun getContracts(){
        println("getContracts")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/contracts.php"

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

                        val contracts: JSONArray = parentObject.getJSONArray("contracts")
                        println("contracts = $contracts")
                        println("contracts count = ${contracts.length()}")

                        val gson = GsonBuilder().create()
                        val contractsList =
                            gson.fromJson(contracts.toString(), Array<Contract>::class.java)
                                .toMutableList()

                        val contractAdapter =
                            ContractsAdapter(contractsList, this.myView.context, this, true)

                        binding.customerContractsRv.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.customerContractsRv.adapter = contractAdapter


                    }
                    getWorkOrders()


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
                params["custID"] = customer.ID
                params["year"] = yearValue
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onContractCellClickListener(data:Contract) {

        data.let {
            val directions = CustomerFragmentDirections.navigateCustomerToContract(data.ID)
            myView.findNavController().navigate(directions)
        }

    }



    private fun getWorkOrders(){
        println("getWorkOrders")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/workOrders.php"

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


                        val workOrders: JSONArray = parentObject.getJSONArray("workOrders")
                        println("workOrders = $workOrders")
                        println("workOrders count = ${workOrders.length()}")

                        val gson = GsonBuilder().create()
                        val workOrdersList =
                            gson.fromJson(workOrders.toString(), Array<WorkOrder>::class.java)
                                .toMutableList()

                        val workOrderAdapter =
                            WorkOrdersAdapter(workOrdersList, this.myView.context, this, true)

                        binding.customerWosRv.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.customerWosRv.adapter = workOrderAdapter


                    }
                    getInvoices()


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
                params["custID"] = customer.ID
                params["empID"] = ""
                params["active"] = "1"
                params["year"] = yearValue
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int) {

        data.let {
            val directions = CustomerFragmentDirections.navigateCustomerToWorkOrder(it)
            myView.findNavController().navigate(directions)
        }
    }


    private fun getInvoices(){
        println("getInvoices")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/invoices.php"

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

                        val invoices: JSONArray = parentObject.getJSONArray("invoices")
                        println("invoices = $invoices")
                        println("invoices count = ${invoices.length()}")

                        val gson = GsonBuilder().create()
                        val invoicesList =
                            gson.fromJson(invoices.toString(), Array<Invoice>::class.java)
                                .toMutableList()


                        val invoicesAdapter = InvoicesAdapter(
                            invoicesList,
                            com.example.AdminMatic.myView.context,
                            this
                        )

                        binding.customerInvoicesRv.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.customerInvoicesRv.adapter = invoicesAdapter
                    }
                    layoutViews()
                   // getImages()


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
                params["custID"] = customer.ID
                params["year"] = yearValue
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onInvoiceCellClickListener(data:Invoice) {

        data.let {
            val directions = CustomerFragmentDirections.navigateCustomerToInvoice(it)
            myView.findNavController().navigate(directions)
        }
    }

    private fun getImages(){
        println("getImages")
        showProgressView()

        //loadMoreItemsCells = mutableListOf<Image?>()

        var offset = adapter.itemCount
        if (refreshing){
            offset = 0
        }
        refreshing = false

        val limit = 200

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/images.php"

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

                        val images: JSONArray = parentObject.getJSONArray("images")
                        println("images = $images")
                        println("images count = ${images.length()}")

                        val gson = GsonBuilder().create()
                        loadMoreImageList =
                            gson.fromJson(images.toString(), Array<Image>::class.java)
                                .toMutableList()
                        println("loadMoreImageList count = ${loadMoreImageList.count()}")
                        imageList.addAll(loadMoreImageList)
                        println("imageList count = ${imageList.count()}")

                        Toast.makeText(
                            activity,
                            "${imageList.count()} Images Loaded",
                            Toast.LENGTH_SHORT
                        ).show()

                        adapter.filterList = imageList
                        imagesLoaded = true
                        binding.customerImagesRv.adapter = adapter

                        binding.customerImagesRv.layoutManager =
                            GridLayoutManager(myView.context, 2)
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
                params["loginID"] = GlobalVars.loggedInEmployee!!.ID
                params["limit"] = limit.toString()
                params["offset"] = offset.toString()
                params["year"] = yearValue
                params["customer"] = customer.ID


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customer"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    /*
    fun RecyclerView.onScrollToEnd(
        onScrollNearEnd: (Unit) -> Unit
    ) = addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(1)) {
                onScrollNearEnd(Unit)
            }
        }
    })

     */



    fun layoutViews(){
        println("layoutViews")

        hideProgressView()


        //swipeRefresh= myView.findViewById(R.id.customerSwipeContainer)



        binding.customerNameTxt.text = customer.sysname

        binding.customerPhoneBtnTv.text = getString(R.string.no_phone_found)

        binding.customerEmailBtnTv.text = getString(R.string.no_email_found)

        binding.customerAddressBtnTv.text = getString(R.string.no_address_found)

        if (!customer.mainAddr.isNullOrBlank()){
            binding.customerAddressBtnTv.text = customer.mainAddr
        }


        if (customer.contacts.isNotEmpty()){
            for (contact in customer.contacts) {
                when (contact.type) {
                    "1" -> {
                        println("1")
                        if (!contact.value.isNullOrBlank()) {
                            binding.customerPhoneBtnTv.text = contact.value!!
                            binding.customerPhoneBtnTv.setOnClickListener {

                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.value!!))
                                com.example.AdminMatic.myView.context.startActivity(intent)
                            }
                        }

                    }
                    "2" -> {
                        println("2")
                        if (!contact.value.isNullOrBlank()) {
                            binding.customerEmailBtnTv.text = contact.value!!
                            binding.customerEmailBtnTv.setOnClickListener {
                                println("email btn clicked ${contact.value!!}")
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:") // only email apps should handle this
                                val emailArray = arrayOf(contact.value!!)
                                intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                                // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                                // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                                myView.context.startActivity(intent)
                            }
                        }

                    }



                }
            }
        }

        if (initialViewsLaidOut) {
            return
        }

        binding.customerAddressBtnTv.setOnClickListener {
            println("map btn clicked ${customer.mainAddr}")

            val lng: String
            val lat: String
            if(customer.lng != null && customer.lat != null){
                lng = customer.lng!!
                lat = customer.lat!!
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q="+lng+","+lat+" (" + customer.sysname + ")")
                )
                startActivity(intent)
            }
        }

        binding.contactsBtn.setOnClickListener{
            println("contacts btn clicked")

            val directions = CustomerFragmentDirections.navigateToCustomerContacts(customer)


            myView.findNavController().navigate(directions)


        }

        binding.documentsBtn.setOnClickListener{
            println("documents btn clicked")

            val directions = CustomerFragmentDirections.navigateToDocuments(customer)
            myView.findNavController().navigate(directions)

        }


        binding.customerNotesBtn.setOnClickListener{
            println("notes btn clicked")

            val directions = CustomerFragmentDirections.navigateToCustomerNotes(customer)

            myView.findNavController().navigate(directions)
            //val directions = EmployeeListFragmentDirections.navigateToEmployee(data)
            // val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
            // myView.findNavController().navigate(directions)
        }

        addBtn = myView.findViewById((R.id.customer_add_btn))
        binding.customerTableTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {



                when (tab!!.position) {
                    0 -> {

                        tableMode = "LEADS"
                        addBtn.text = getString(R.string.add_lead)
                       // Toast.makeText(com.example.AdminMatic.myView.context, "Leads", Toast.LENGTH_SHORT).show()
                        binding.customerLeadsRv.visibility = View.VISIBLE
                        binding.customerContractsRv.visibility = View.GONE
                        binding.customerWosRv.visibility = View.GONE
                        binding.customerInvoicesRv.visibility = View.GONE
                        binding.customerImagesRv.visibility = View.GONE
                        binding.customerNoImageCollectionText.visibility = View.GONE
                    }
                    1 -> {
                        tableMode = "CONTRACTS"
                        addBtn.text = getString(R.string.add_contract)
                        //Toast.makeText(com.example.AdminMatic.myView.context, "Contracts", Toast.LENGTH_SHORT).show()
                        binding.customerLeadsRv.visibility = View.GONE
                        binding.customerContractsRv.visibility = View.VISIBLE
                        binding.customerWosRv.visibility = View.GONE
                        binding.customerInvoicesRv.visibility = View.GONE
                        binding.customerImagesRv.visibility = View.GONE
                        binding.customerNoImageCollectionText.visibility = View.GONE
                    }
                    2 -> {
                        tableMode = "WOS"
                        addBtn.text = getString(R.string.add_work_order)
                        //Toast.makeText(com.example.AdminMatic.myView.context, "Work Orders", Toast.LENGTH_SHORT).show()
                        binding.customerLeadsRv.visibility = View.GONE
                        binding.customerContractsRv.visibility = View.GONE
                        binding.customerWosRv.visibility = View.VISIBLE
                        binding.customerInvoicesRv.visibility = View.GONE
                        binding.customerImagesRv.visibility = View.GONE
                        binding.customerNoImageCollectionText.visibility = View.GONE
                    }
                    3 -> {
                        tableMode = "INVOICES"
                        addBtn.text = getString(R.string.add_invoice)
                        //Toast.makeText(com.example.AdminMatic.myView.context, "Invoices", Toast.LENGTH_SHORT).show()
                        binding.customerLeadsRv.visibility = View.GONE
                        binding.customerContractsRv.visibility = View.GONE
                        binding.customerWosRv.visibility = View.GONE
                        binding.customerInvoicesRv.visibility = View.VISIBLE
                        binding.customerImagesRv.visibility = View.GONE
                        binding.customerNoImageCollectionText.visibility = View.GONE
                    }
                    4 -> {
                        tableMode = "IMAGES"
                        addBtn.text = getString(R.string.add_images)
                        //Toast.makeText(com.example.AdminMatic.myView.context, "Images", Toast.LENGTH_SHORT).show()

                        if (customer.allowImages == "1") {
                            if (!imagesLoaded) {
                                getImages()
                            }
                        }
                        else {
                            addBtn.visibility = View.GONE
                            binding.customerNoImageCollectionText.visibility = View.VISIBLE
                        }
                        binding.customerLeadsRv.visibility = View.GONE
                        binding.customerContractsRv.visibility = View.GONE
                        binding.customerWosRv.visibility = View.GONE
                        binding.customerInvoicesRv.visibility = View.GONE
                        binding.customerImagesRv.visibility = View.VISIBLE
                    }

                }






            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        tableMode = "WOS"
        addBtn.text = getString(R.string.add_work_order)
        binding.customerTableTl.getTabAt(2)!!.select()



        addBtn.setOnClickListener{
            println("add btn clicked")


            when (tableMode) {
                "LEADS" -> {

                }
                "CONTRACTS" -> {

                }
                "WOS" -> {

                }
                "INVOICES" -> {

                }
                "IMAGES" -> {
                    customer.let { customer ->
                        val directions = CustomerFragmentDirections.navigateCustomerToImageUpload("CUSTOMER",
                            arrayOf(),customerID,customer.sysname,"","","", "","","","", "","", "")
                        myView.findNavController().navigate(directions)
                    }
                }
            }

            //val directions = EmployeeListFragmentDirections.navigateToEmployee(data)
            // val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
            // myView.findNavController().navigate(directions)
        }


        val dateToday = LocalDate.now()
        var currentYear = dateToday.year

        yearList.clear()

        yearList.add(getString(R.string.all))
        repeat(10) {
            yearList.add(0, currentYear.toString())
            currentYear--
        }

        yearAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            yearList
        )
        binding.yearSpinner.adapter = yearAdapter
        binding.yearSpinner.onItemSelectedListener = this@CustomerFragment

        binding.yearSpinner.setSelection(yearList.size-1)

        initialViewsLaidOut = true

    }


    override fun onImageCellClickListener(data:Image) {
        data.let {
            val directions = CustomerFragmentDirections.navigateCustomerToImage(imageList.toTypedArray(), imageList.indexOf(data))
            myView.findNavController().navigate(directions)
        }
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.customerUiCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.customerUiCl.visibility = View.VISIBLE
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("onItemSelected")

        var yearValueNew = yearList[binding.yearSpinner.selectedItemPosition]

        if (binding.yearSpinner.selectedItemPosition == yearList.size - 1) {
            yearValueNew = "1"
        }

        if (yearValueNew != yearValue) {
            yearValue = yearValueNew
            showProgressView()
            getLeads()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

}