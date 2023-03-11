package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.*
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.HashMap


/*
interface OnLoadMoreListener {
    fun onLoadMore()
}
*/



interface ImageCellClickListener {
    fun onImageCellClickListener(data:Image)
}

interface TagCellClickListener {
    fun onTagCellClickListener(data:Tag)
}

//interface ImageUploadInterface {
    //fun onUploadComplete()
//}


class ImageListFragment : Fragment(), ImageCellClickListener, CustomerCellClickListener, TagCellClickListener {//, ImageUploadInterface {

    // Settings variables
    private var filterBy = 0
    private var orderBy = 0
    private var dates = 0
    private var searchByTag = false

    // Search variables
    private var custName = ""
    private var custID = 0
    private var selectedTag = ""
    var tags:Array<Tag>? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View
    var imagesLoaded:Boolean = false

    lateinit var adapter:ImagesAdapter

    lateinit var imageList: MutableList<Image>
    lateinit var loadMoreImageList: MutableList<Image>
    //lateinit var scrollListener: RecyclerViewLoadMoreScroll
    //lateinit var mLayoutManager:RecyclerView.LayoutManager

    var refreshing = false
    var searching = false

    private var _binding: FragmentImageListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("imageListSettings") { _, bundle ->
            val newFilterBy = bundle.getInt("filterBy")
            val newOrderBy = bundle.getInt("orderBy")
            val newDates = bundle.getInt("dates")
            val newSearchByTag = bundle.getBoolean("searchByTag")
            val clearSearch = bundle.getBoolean("clearSearch")

            if (newFilterBy != filterBy || newOrderBy != orderBy || newDates != dates || newSearchByTag != searchByTag) {

                if (newSearchByTag != searchByTag) {
                    searchByTag = newSearchByTag
                    prepareSearch()
                }

                filterBy = newFilterBy
                orderBy = newOrderBy
                dates = newDates
                searchByTag = newSearchByTag

                if (filterBy != 0 || orderBy != 0 || dates != 0 || searchByTag) {
                    println("setting button color yellow")
                    ImageViewCompat.setImageTintList(binding.settingsIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, R.color.settingsActive)))
                }
                else {
                    println("setting button color default")
                    ImageViewCompat.setImageTintList(binding.settingsIv, null)
                }

                if (clearSearch) {
                    binding.imagesSearch.setQuery("", false)
                    custID = 0
                    custName = ""
                    selectedTag = ""
                    myView.hideKeyboard()
                    binding.imagesSearch.clearFocus()
                    binding.customerSearchRv.visibility = View.INVISIBLE
                }

                refreshImages()
            }
            else { // If nothing is changed but "clear all filters" was clicked, we still need to refresh
                if (clearSearch) {
                    binding.imagesSearch.setQuery("", false)
                    custID = 0
                    custName = ""
                    selectedTag = ""
                    myView.hideKeyboard()
                    binding.imagesSearch.clearFocus()
                    binding.customerSearchRv.visibility = View.INVISIBLE
                    refreshImages()
                }
            }



        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")

        if (!imagesLoaded){

            println("myView = null")
            globalVars = GlobalVars()
            _binding = FragmentImageListBinding.inflate(inflater, container, false)
            myView = binding.root

            imageList = mutableListOf()

            adapter = ImagesAdapter(imageList,myView.context, false, this)


        }
        else {
            println("images already loaded")
        }

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.image_list)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

        (activity as MainActivity?)!!.setImageList(this)

        if(!imagesLoaded){
            binding.addImagesBtn.setOnClickListener{
                println("add images btn clicked")

                val directions = ImageListFragmentDirections.navigateToGalleryImageUpload("GALLERY",
                    arrayOf(),"","","","","","","","","", "")
                myView.findNavController().navigate(directions)
            }


            prepareSearch()


            binding.listRecyclerView.onScrollToEnd { if(!searching){
                getImages()}
            }

            binding.listRecyclerView.adapter = adapter

            // recyclerView.apply {
            // layoutManager = GridLayoutManager(myView.context, 2)


            // }

            binding.listRecyclerView.layoutManager = GridLayoutManager(myView.context, 3)


            binding.listRecyclerView.apply {



                // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                // Setup refresh listener which triggers new data loading
                // Setup refresh listener which triggers new data loading
                binding.customerSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    //fetchTimelineAsync(0)

                    /*
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    //clear list
                    imageList = mutableListOf()
                    refreshing = true



                    getImages()
                    */
                    refreshImages()

                }
                // Configure the refreshing colors
                // Configure the refreshing colors
                binding.customerSwipeContainer.setColorSchemeResources(
                    R.color.button,
                    R.color.black,
                    R.color.colorAccent,
                    R.color.colorPrimaryDark
                )

                //(adapter as ImagesAdapter).notifyDataSetChanged();

                //Change the boolean isLoading to false
                //  scrollListener.setLoaded()

                //search listener

            }
            binding.settingsBtn.setOnClickListener {
                val directions = ImageListFragmentDirections.navigateToImageListSettings(filterBy, orderBy, dates, searchByTag)
                myView.findNavController().navigate(directions)
            }

            getTags()

            //getImages()
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageList")
    }

    private fun prepareSearch() {

        if (searchByTag) {
            binding.imagesSearch.queryHint = getString(R.string.search_by_tag_hint)
            custID = 0
            custName = ""
        }
        else {
            binding.imagesSearch.queryHint = getString(R.string.search_by_customer_hint)
            selectedTag = ""
        }

        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)



            if (searchByTag) {
                adapter = activity?.let {
                    TagsAdapter(
                        tags!!.toMutableList(),
                        this@ImageListFragment
                    )
                }
            }
            else {
                adapter = activity?.let {
                    CustomersAdapter(
                        GlobalVars.customerList!!,
                        this@ImageListFragment, false
                    )
                }
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.customerSearchRv.addItemDecoration(itemDecoration)

            binding.imagesSearch.onFocusChangeListener

            binding.imagesSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.customerSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if (searchByTag) {
                        (adapter as TagsAdapter).filter.filter(newText)
                    }
                    else {
                        (adapter as CustomersAdapter).filter.filter(newText)
                    }
                    if(newText == ""){
                        binding.customerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.customerSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })


            val closeButton: View? = binding.imagesSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.imagesSearch.setQuery("", false)
                custID = 0
                custName = ""
                selectedTag = ""
                myView.hideKeyboard()
                binding.imagesSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
                refreshImages()
            }

            binding.imagesSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    if (searchByTag) {
                        binding.imagesSearch.setQuery(selectedTag, false)
                    }
                    else {
                        binding.imagesSearch.setQuery(custName, false)
                    }

                    binding.customerSearchRv.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun refreshImages(){
        binding.imagesSearch.setQuery("", false)
        binding.imagesSearch.clearFocus()
        //clear list
        imageList = mutableListOf()
        refreshing = true



        getImages()
    }

    fun getImagesList():MutableList<Image> {
        return imageList
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

                //hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val tagsArray: JSONArray = parentObject.getJSONArray("tags")
                        println("tagsArray = $tagsArray")
                        println("tagsArray count = ${tagsArray.length()}")

                        val gson = GsonBuilder().create()
                        tags = gson.fromJson(tagsArray.toString() , Array<Tag>::class.java)

                        getImages()
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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["loginID"] = loggedInEmployee!!.ID

                println("getTags params = $params")
                return params
            }
        }
        postRequest1.tag = "imageList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun getImages(){
        println("getImages")


        // println("pgsBar = $pgsBar")


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

                        // Now we call setRefreshing(false) to signal refresh has finished
                        binding.customerSwipeContainer.isRefreshing = false

                        // Toast.makeText(activity,"${imageList.count()} Images Loaded",Toast.LENGTH_SHORT).show()


                        adapter.filterList = imageList

                        adapter.notifyDataSetChanged()

                        imagesLoaded = true
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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["loginID"] = loggedInEmployee!!.ID
                params["limit"] = limit.toString()
                params["offset"] = offset.toString()
                if (custID != 0) {
                    params["customer"] = custID.toString()
                }
                if (selectedTag.isNotBlank()) {
                    params["tag"] = selectedTag
                }

                when (filterBy) {
                    1 -> { // My images
                        params["uploadedBy"] = loggedInEmployee!!.ID
                    }
                    2 -> { // Portfolio
                        params["portfolio"] = "1"
                    }
                    3 -> { // Fieldnote
                        params["fieldnotes"] = "1"
                    }
                    4 -> { // Task
                        params["task"] = "1"
                    }
                }

                when (orderBy) {
                    0 -> {
                        params["order"] = "ID DESC"
                    }
                    1 -> {
                        params["order"] = "ID ASC"
                    }
                    2 -> {
                        params["order"] = "likes DESC, ID DESC"
                    }
                }

                when (dates) {
                    1 -> { // Today
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.atStartOfDay()
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    2 -> { // Yesterday (you said you'd call Sears)
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.atStartOfDay().minusDays(1)
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    3 -> { // This Week
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atStartOfDay()
                        val ldtStop: LocalDateTime = ldtStart.plusDays(6)
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    4 -> { // Last Week
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atStartOfDay().minusDays(7)
                        val ldtStop: LocalDateTime = ldtStart.plusDays(6)
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    5 -> { // This Month
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.withDayOfMonth(1).atStartOfDay()
                        val ldtStop: LocalDateTime = ldtStart.plusDays(31) // This should be fine because if it goes over it's just in the future so there won't be results anyways
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    6 -> { // Last Month
                        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                        val ldtStart: LocalDateTime = today.minusMonths(1).withDayOfMonth(1).atStartOfDay()
                        val ldtStop: LocalDateTime = today.withDayOfMonth(1).minusDays(1).atStartOfDay()
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    7 -> { // This Year
                        val ldtStart: LocalDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).atStartOfDay()
                        val ldtStop: LocalDateTime = LocalDate.ofYearDay(LocalDate.now().year+1, 1).minusDays(1).atStartOfDay()
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                    8 -> { // Last Year
                        val ldtStart: LocalDateTime = LocalDate.ofYearDay(LocalDate.now().year-1, 1).atStartOfDay()
                        val ldtStop: LocalDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).minusDays(1).atStartOfDay()
                        params["startDate"] = ldtStart.format(GlobalVars.dateFormatterYYYYMMDD)
                        params["endDate"] = ldtStop.format(GlobalVars.dateFormatterYYYYMMDD)
                    }
                }


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun RecyclerView.onScrollToEnd(
        onScrollNearEnd: (Unit) -> Unit
    ) = addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(1)) {
                onScrollNearEnd(Unit)
            }
        }
    })




    override fun onImageCellClickListener(data:Image) {
        data.let {
            val directions = ImageListFragmentDirections.navigateToImage(imageList.toTypedArray(), imageList.indexOf(it))
            myView.findNavController().navigate(directions)
        }
    }

    /*
     fun onUploadComplete() {
        println("Upload Complete")
        myView.findNavController().navigateUp()
        //myView.findNavController().popBackStack()
         imageList = mutableListOf()
         getImages()
    }
*/

    override fun onCustomerCellClickListener(data: Customer) {
        //workOrder!!.customer = data.ID
        //workOrder!!.custName = data.sysname
        custID = data.ID.toInt()
        custName = data.sysname
        binding.imagesSearch.setQuery(custName, false)
        binding.imagesSearch.clearFocus()
        refreshImages()
        myView.hideKeyboard()
    }

    override fun onTagCellClickListener(data: Tag) {
        print("FUCK")
        selectedTag = data.name
        binding.imagesSearch.setQuery(selectedTag, false)
        binding.imagesSearch.clearFocus()
        refreshImages()
        myView.hideKeyboard()
    }



    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE

        //searchView.alpha = 0.5f
        //recyclerView.alpha = 0.5f
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE

        //searchView.alpha = 1.0f
        //recyclerView.alpha = 1.0f
    }

}