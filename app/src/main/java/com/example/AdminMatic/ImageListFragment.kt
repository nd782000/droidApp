package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_image_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/*
interface OnLoadMoreListener {
    fun onLoadMore()
}
*/



interface ImageCellClickListener {
    fun onImageCellClickListener(data:Image)
}

//interface ImageUploadInterface {
    //fun onUploadComplete()
//}


class ImageListFragment : Fragment(), ImageCellClickListener{//, ImageUploadInterface {


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    var imagesLoaded:Boolean = false

    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView:androidx.appcompat.widget.SearchView
    lateinit var  swipeRefresh:SwipeRefreshLayout

    private lateinit var addImagesBtn: Button


    // lateinit var  btn: Button

    lateinit var adapter:ImagesAdapter

    lateinit var imageList: MutableList<Image>
    lateinit var loadMoreImageList: MutableList<Image>
   // lateinit var scrollListener: RecyclerViewLoadMoreScroll
    //lateinit var mLayoutManager:RecyclerView.LayoutManager

    var refreshing = false
    var searching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")

        if (!imagesLoaded){

            println("myView = null")
            globalVars = GlobalVars()
            myView = inflater.inflate(R.layout.fragment_image_list, container, false)


            //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
            // progBar.alpha = 0.2f

            imageList = mutableListOf()

            adapter = ImagesAdapter(imageList,myView.context, this)





            //(activity as AppCompatActivity).supportActionBar?.title = "Image List"

            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.image_list)



            // Inflate the layout for this fragment
        }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

        (activity as MainActivity?)!!.setImageList(this)

        if(!imagesLoaded){
            pgsBar = view.findViewById(R.id.progressBar)
            recyclerView = view.findViewById(R.id.list_recycler_view)
            searchView = view.findViewById(R.id.images_search)
            swipeRefresh= view.findViewById(R.id.customerSwipeContainer)


            addImagesBtn = view.findViewById((R.id.add_images_btn))
            addImagesBtn.setOnClickListener{
                println("add images btn clicked")

                val directions = ImageListFragmentDirections.navigateToGalleryImageUpload("GALLERY",
                    arrayOf(),"","","","","","","","","", "")
                myView.findNavController().navigate(directions)
            }




            val itemDecoration: ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)

            recyclerView.onScrollToEnd { if(!searching){
                getImages()}
            }

            recyclerView.adapter = adapter

            // recyclerView.apply {
            // layoutManager = GridLayoutManager(myView.context, 2)


            // }

            recyclerView.layoutManager = GridLayoutManager(myView.context, 2)


            recyclerView.apply {



                // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                // Setup refresh listener which triggers new data loading
                // Setup refresh listener which triggers new data loading
                swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
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
                swipeRefresh.setColorSchemeResources(
                    R.color.button,
                    R.color.black,
                    R.color.colorAccent,
                    R.color.colorPrimaryDark
                )

                //(adapter as ImagesAdapter).notifyDataSetChanged();

                //Change the boolean isLoading to false
                //  scrollListener.setLoaded()

                //search listener
                images_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {


                    override fun onQueryTextSubmit(query: String?): Boolean {
                        searching = true
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        searching = true
                        (adapter as ImagesAdapter).filter.filter(newText)
                        return false
                    }

                })
            }
            getImages()
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageList")
    }

    fun refreshImages(){
        searchView.setQuery("", false)
        searchView.clearFocus()
        //clear list
        imageList = mutableListOf()
        refreshing = true



        getImages()
    }

    fun getImagesList():MutableList<Image> {
        return imageList
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

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/images.php"

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
                    customerSwipeContainer.isRefreshing = false

                    // Toast.makeText(activity,"${imageList.count()} Images Loaded",Toast.LENGTH_SHORT).show()


                    adapter.filterList = imageList

                    adapter.notifyDataSetChanged()

                    imagesLoaded = true

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
        myView.findNavController().popBackStack()
         imageList = mutableListOf()
         getImages()
    }
*/





    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        //searchView.visibility = View.INVISIBLE
        //recyclerView.visibility = View.INVISIBLE

        searchView.alpha = 0.5f
        recyclerView.alpha = 0.5f
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
       // searchView.visibility = View.VISIBLE
        //recyclerView.visibility = View.VISIBLE
        searchView.alpha = 1.0f
        recyclerView.alpha = 1.0f
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageListFragment().apply {

            }
    }
}