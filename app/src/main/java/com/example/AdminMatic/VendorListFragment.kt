package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_vendor_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_vendor_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_vendor_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



interface VendorCellClickListener {
    fun onVendorCellClickListener(data:Vendor)
}


class VendorListFragment : Fragment(), VendorCellClickListener {


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView:androidx.appcompat.widget.SearchView
    lateinit var  swipeRefresh:SwipeRefreshLayout


    // lateinit var  btn: Button

    lateinit var adapter:VendorsAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_vendor_list, container, false)


        //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
       // progBar.alpha = 0.2f

        var emptyList:MutableList<Vendor> = mutableListOf()

        adapter = VendorsAdapter(emptyList,myView.context, this)





       // (activity as AppCompatActivity).supportActionBar?.title = "Vendor List"

        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Vendor List"


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.vendors_search)
        swipeRefresh= view.findViewById(R.id.customerSwipeContainer)

        getVendors()


    }


    private fun getVendors(){
        println("getVendors")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/vendors.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)


        //val preferences =
        //this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
        // val session = preferences.getString("sessionKey","")
        //val companyUnique = preferences.getString("companyUnique","")


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var vendors:JSONArray = parentObject.getJSONArray("vendors")
                    println("vendors = ${vendors.toString()}")
                    println("vendors count = ${vendors.length()}")



                    val gson = GsonBuilder().create()
                    val vendorsList = gson.fromJson(vendors.toString() , Array<Vendor>::class.java).toMutableList()


                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)


                        adapter = activity?.let {
                            VendorsAdapter(vendorsList,
                                it, this@VendorListFragment)
                        }

                        val itemDecoration: ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        recyclerView.addItemDecoration(itemDecoration)

                        //for item animations
                        // recyclerView.itemAnimator = SlideInUpAnimator()



                        // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                        // Setup refresh listener which triggers new data loading
                        // Setup refresh listener which triggers new data loading
                        swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                            // Make sure you call swipeContainer.setRefreshing(false)
                            // once the network request has completed successfully.
                            //fetchTimelineAsync(0)
                            searchView.setQuery("", false);
                            searchView.clearFocus();
                            getVendors()
                        }
                        // Configure the refreshing colors
                        // Configure the refreshing colors
                        swipeRefresh.setColorSchemeResources(
                            R.color.button,
                            R.color.black,
                            R.color.colorAccent,
                            R.color.colorPrimaryDark
                        )



                        (adapter as VendorsAdapter).notifyDataSetChanged();

                        // Remember to CLEAR OUT old items before appending in the new ones

                        // ...the data has come back, add new items to your adapter...

                        // Now we call setRefreshing(false) to signal refresh has finished
                        customerSwipeContainer.isRefreshing = false;

                        Toast.makeText(activity,"${vendorsList.count()} Vendors Loaded",Toast.LENGTH_SHORT).show()



                        //search listener
                        vendors_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {


                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as VendorsAdapter).filter.filter(newText)
                                return false
                            }

                        })





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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)
    }

    override fun onVendorCellClickListener(data:Vendor) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()

        data?.let { data ->
            val directions = VendorListFragmentDirections.navigateToVendor(data)
            myView.findNavController().navigate(directions)
        }
    }



    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        searchView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        searchView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
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
            VendorListFragment().apply {

            }
    }
}