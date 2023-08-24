package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEditImageBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule


class EditImageFragment : Fragment(), CustomerCellClickListener, VendorCellClickListener, TagCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private lateinit var imageList:Array<Image>
    private var imageListIndex:Int = 0
    private var image: Image? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var vendorList:MutableList<Vendor>

    lateinit var tagList:MutableList<Tag>

    private lateinit var appliedTagsList:MutableList<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //image = it.getParcelable("image")
            imageList = (it.getParcelableArray("imageList") as Array<Image>?)!!
            imageListIndex = it.getInt("imageListIndex")
        }
        image = imageList[imageListIndex]
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageEdit")
    }



    private var _binding: FragmentEditImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditImageBinding.inflate(inflater, container, false)
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

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_image_num, image!!.ID)


        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flag edits made false after all the views have time to set their states
        Timer("LeadEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

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
                //println("Get Customers Response $response")
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

                    getVendors()


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
        postRequest1.tag = "customerList"
        VolleyRequestQueue.getInstance(requireActivity().application)
            .addToRequestQueue(postRequest1)
    }

    private fun getVendors(){
        println("getVendors")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/vendors.php"

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

                        val vendors: JSONArray = parentObject.getJSONArray("vendors")
                        println("vendors = $vendors")
                        println("vendors count = ${vendors.length()}")

                        val gson = GsonBuilder().create()
                        vendorList = gson.fromJson(vendors.toString(), Array<Vendor>::class.java).toMutableList()

                        vendorList.forEach {
                            if (it.itemString == null) {
                                it.itemString = ""
                            }
                        }

                        getTags()

                    }


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
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "vendorList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getTags() {
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/tags.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val tagsArray: JSONArray = parentObject.getJSONArray("tags")
                        println("tagsArray = $tagsArray")
                        println("tagsArray count = ${tagsArray.length()}")

                        val gson = GsonBuilder().create()
                        tagList = gson.fromJson(tagsArray.toString() , Array<Tag>::class.java).toMutableList()

                        hideProgressView()
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
                params["loginID"] = GlobalVars.loggedInEmployee!!.ID

                println("getTags params = $params")
                return params
            }
        }
        postRequest1.tag = "imageList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {
        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@EditImageFragment, false
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
                    if(newText == ""){
                        binding.customerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.customerSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as CustomersAdapter).filter.filter(newText)
                    return false
                }

            })


            val closeButton: View? = binding.customerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.customerSearch.setQuery("", false)
                image!!.customer = "0"
                image!!.customerName = ""
                myView.hideKeyboard()
                binding.customerSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
            }

            binding.customerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.customerSearch.setQuery(image!!.customerName, false)
                    binding.customerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        // uncompressed switch
        binding.uncompressedSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                image!!.noCompress = "1"
            }
            else {
                image!!.noCompress = "0"
            }
        }

        // Vendor search
        binding.vendorSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                VendorsAdapter(
                    vendorList, this@EditImageFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.vendorSearchRv.addItemDecoration(itemDecoration)

            binding.vendorSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.vendorSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.vendorSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.vendorSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as VendorsAdapter).filter.filter(newText)
                    return false
                }

            })

            val closeButton: View? = binding.vendorSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.vendorSearch.setQuery("", false)
                image!!.vendorID = ""
                //image!!.vendorName = ""
                myView.hideKeyboard()
                binding.vendorSearch.clearFocus()
                binding.vendorSearchRv.visibility = View.INVISIBLE
            }



            binding.vendorSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    for (it in 0 until vendorList.size) {

                        if (vendorList[it].ID == image!!.vendorID) {
                            binding.vendorSearch.setQuery(vendorList[it].name, false)
                            break
                        }
                    }
                    binding.vendorSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        // Tag search
        binding.tagsSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                TagsAdapter(
                    tagList, this@EditImageFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.tagsSearchRv.addItemDecoration(itemDecoration)

            binding.tagsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.tagsSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.tagsSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.tagsSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as TagsAdapter).filter.filter(newText)
                    return false
                }

            })

            val closeButton: View? = binding.tagsSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.tagsSearch.setQuery("", false)
                myView.hideKeyboard()
                binding.tagsSearch.clearFocus()
                binding.tagsSearchRv.visibility = View.INVISIBLE
            }



            binding.tagsSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    for (it in 0 until tagList.size) {
                        /*
                        if (vendorList[it].ID == image!!.vendorID) {
                            binding.tagsSearch.setQuery(vendorList[it].name, false)
                            break
                        }
                         */
                    }
                    binding.tagsSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        binding.newTagBtn.setOnClickListener {
            if (binding.tagsSearch.query.isNotEmpty()) {
                val newTag = capitalizeFirst(binding.tagsSearch.query.toString())
                if (appliedTagsList.contains(newTag)) {
                    globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.tag_already_on_image, newTag))
                    binding.tagsSearch.setQuery("", false)
                    binding.tagsSearch.clearFocus()
                    myView.hideKeyboard()
                    return@setOnClickListener
                }

                for (i in 0 until tagList.size) {
                    if (tagList[i].name == newTag) {
                        globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.tag_already_in_system, newTag))
                        break
                    }
                }

                binding.tagsSearch.setQuery("", false)
                binding.tagsSearch.clearFocus()
                myView.hideKeyboard()


                appliedTagsList.add(newTag)
                addTag(newTag)

            }
        }

        // Description
        binding.descriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.descriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateImage()
            }
        }



        // ===== POPULATE FIELDS =====

        if (image!!.tags!!.isNotEmpty()) {
            val tagsListTemp = image!!.tags!!.split(",").map { it.trim() }
            appliedTagsList = tagsListTemp.toMutableList()
        }
        else {
            appliedTagsList = mutableListOf()
        }


        if (appliedTagsList.size > 0) {
            binding.tapToDeleteTv.visibility = View.VISIBLE
        }
        else {
            binding.tapToDeleteTv.visibility = View.INVISIBLE
        }

        println("tags:")
        appliedTagsList.forEach {
            addTag(it)
        }

        binding.nameEt.setText(image!!.name)
        if (image!!.customerName != "Unknown") {
            binding.customerSearch.setQuery(image!!.customerName, false)
        }

        for (i in 0 until vendorList.size) {
            if (vendorList[i].ID == image!!.vendorID) {
                binding.vendorSearch.setQuery(vendorList[i].name, false)
                break
            }
        }

        if (image!!.noCompress == "1") {
            binding.uncompressedSwitch.isChecked = true
            binding.uncompressedSwitch.jumpDrawablesToCurrentState()
        }
        else {
            binding.uncompressedSwitch.isChecked = false
            binding.uncompressedSwitch.jumpDrawablesToCurrentState()
        }

        binding.descriptionEt.setText(image!!.description)
        binding.customerSearchRv.visibility = View.INVISIBLE
        binding.vendorSearchRv.visibility = View.INVISIBLE

        editsMade = false
    }

    private fun validateFields(): Boolean {

        if (binding.nameEt.text.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.edit_image_name_empty))
            return false
        }

        return true
    }

    private fun addTag(newTag:String) {
        binding.tapToDeleteTv.visibility = View.VISIBLE
        val newTV = TextView(myView.context)
        binding.tagsFlexbox.addView(newTV)
        newTV.text = newTag
        newTV.setTextColor(ContextCompat.getColor(myView.context, R.color.white))
        newTV.background = ContextCompat.getDrawable(myView.context, R.drawable.tag_layout)
        val params = newTV.layoutParams
        params.height = 60
        newTV.setPadding(20,0,20,0)
        val marginParams = params as ViewGroup.MarginLayoutParams
        marginParams.setMargins(5,5,5,5)
        newTV.layoutParams = params
        newTV.setOnClickListener {
            println("removing tag from list")
            appliedTagsList.remove(newTV.text)
            if (appliedTagsList.size == 0) {
                binding.tapToDeleteTv.visibility = View.INVISIBLE
            }
            binding.tagsFlexbox.removeView(newTV)
        }
    }

    private fun updateImage() {
        println("update Image")
        showProgressView()

        if (image!!.equipmentID == null) {
            image!!.equipmentID = ""
        }
        if (image!!.taskID == null) {
            image!!.taskID = ""
        }

        val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()
        //val tagsJsonArray = JSONArray(appliedTagsList)
        val tagsJsonArray = gsonPretty.toJson(appliedTagsList)



        println("tags jsonarray: $tagsJsonArray")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/image.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("update image response: $response")

                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        getImage()


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
                params["ID"] = image!!.ID
                params["createdBy"] = image!!.createdBy.toString()
                params["equipmentID"] = image!!.equipmentID.toString()
                params["task"] = image!!.taskID.toString()
                params["name"] = binding.nameEt.text.trim().toString()
                params["desc"] = binding.descriptionEt.text.trim().toString()
                params["customer"] = image!!.customer.toString()
                params["vendor"] = image!!.vendorID.toString()
                params["tags"] = tagsJsonArray.toString()
                params["noCompress"] = image!!.noCompress.toString()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("update image params = $params")
                return params
            }
        }
        postRequest1.tag = "imageEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getImage() {
        println("get Image")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/image.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("get image response: $response")

                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val images: JSONArray = parentObject.getJSONArray("images")
                        val gson = GsonBuilder().create()
                        val imageListJSON = gson.fromJson(images.toString(), Array<Image>::class.java).toMutableList()
                        print(imageListJSON.size)

                        image = imageListJSON[0]
                        imageList[imageListIndex] = image!!

                        //val temp = activity?.supportFragmentManager?.findFragmentById(R.id.imageListFragment) as ImageListFragment?


                        println("This fragment ID: ${this.id}")
                        requireActivity().supportFragmentManager.fragments.forEach {
                            print("Fragment ID: ${it.id}")
                        }

                        //temp!!.onImageUpdate(image!!, imageListIndex)

                        setFragmentResult("imageEditListener", bundleOf("shouldRefreshImages" to true))

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
                val params: MutableMap<String, String> = HashMap()
                params["ID"] = image!!.ID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("get image params = $params")
                return params
            }
        }
        postRequest1.tag = "imageEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        image!!.customer = data.ID
        image!!.customerName = data.sysname
        binding.customerSearch.setQuery(image!!.customerName, false)
        binding.customerSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onVendorCellClickListener(data: Vendor) {
        editsMade = true
        image!!.vendorID = data.ID
        //lead!!.repName = data.name
        binding.vendorSearch.setQuery(image!!.vendorID, false)
        binding.vendorSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onTagCellClickListener(data: Tag) {
        editsMade = true

        val newTag = capitalizeFirst(data.name)

        if (appliedTagsList.contains(newTag)) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.tag_already_on_image, newTag))
        }
        else {
            appliedTagsList.add(newTag)
            addTag(newTag)
        }

        binding.tagsSearch.setQuery("", false)
        binding.tagsSearch.clearFocus()
        myView.hideKeyboard()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }


    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    fun capitalizeFirst(str: String): String {
        return str.trim().split("\\s+".toRegex())
            .map { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }.joinToString(" ")
    }

}