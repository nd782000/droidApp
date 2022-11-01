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
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
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




            val itemDecoration: ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.listRecyclerView.addItemDecoration(itemDecoration)

            binding.listRecyclerView.onScrollToEnd { if(!searching){
                getImages()}
            }

            binding.listRecyclerView.adapter = adapter

            // recyclerView.apply {
            // layoutManager = GridLayoutManager(myView.context, 2)


            // }

            binding.listRecyclerView.layoutManager = GridLayoutManager(myView.context, 2)


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
                binding.imagesSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
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