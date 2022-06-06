package com.example.AdminMatic

//import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
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
import kotlinx.android.synthetic.main.fragment_customer_list.*
import org.json.JSONException
import org.json.JSONObject

interface CustomerCellClickListener {
    fun onCustomerCellClickListener(data:Customer)
}

//great resource fo recyclerView inf
//https://guides.codepath.com/android/using-the-recyclerview





class CustomerListFragment : Fragment(), CustomerCellClickListener {

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView:androidx.appcompat.widget.SearchView
    lateinit var  swipeRefresh:SwipeRefreshLayout
    lateinit var adapter:CustomersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_customer_list, container, false)


        val emptyList:MutableList<Customer> = mutableListOf()
        adapter = CustomersAdapter(emptyList, this)
        //(activity as AppCompatActivity).supportActionBar?.title = "Customer List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.customer_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.customers_search)
        swipeRefresh= view.findViewById(R.id.customerSwipeContainer)

        //getCustomers()

        showCustomers()

    }


    fun showCustomers(){
        println("showCustomers")
        list_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)

            hideProgressView()


            if(GlobalVars.customerList != null) {
                adapter = activity?.let {


                    CustomersAdapter(
                        GlobalVars.customerList!!,
                        this@CustomerListFragment
                    )


                }

                val itemDecoration: ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                recyclerView.addItemDecoration(itemDecoration)


                // Setup refresh listener which triggers new data loading
                swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    searchView.setQuery("", false)
                    searchView.clearFocus()
                    getCustomers()
                }
                // Configure the refreshing colors
                swipeRefresh.setColorSchemeResources(
                    R.color.button,
                    R.color.black,
                    R.color.colorAccent,
                    R.color.colorPrimaryDark
                )
                //(adapter as CustomersAdapter).notifyDataSetChanged()

                // Remember to CLEAR OUT old items before appending in the new ones

                // Now we call setRefreshing(false) to signal refresh has finished
                customerSwipeContainer.isRefreshing = false
/*

                Toast.makeText(
                    activity,
                    "${GlobalVars.customerList!!.count()} Customers Loaded",
                    Toast.LENGTH_SHORT
                ).show()
*/

                //search listener
                customers_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        (adapter as CustomersAdapter).filter.filter(newText)
                        return false
                    }

                })
            }
        }
    }



    private fun getCustomers(){
        println("getCustomers")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/customers.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        //var customers: JSONObject = parentObject.getJSONObject("customers")
                        //val customers:JSONArray = parentObject.getJSONArray("customers")
                        // println("customers = ${customers.toString()}")
                        // println("customers count = ${customers.length()}")

                        //val gson = GsonBuilder().create()
                        //val customersList = gson.fromJson(customers.toString() , Array<Customer>::class.java).toMutableList()

                        /*
                        list_recycler_view.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = activity?.let {
                                CustomersAdapter(customersList,
                                    it, this@CustomerListFragment)
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            recyclerView.addItemDecoration(itemDecoration)


                            // Setup refresh listener which triggers new data loading
                            swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                                // Make sure you call swipeContainer.setRefreshing(false)
                                // once the network request has completed successfully.
                                searchView.setQuery("", false);
                                searchView.clearFocus();
                                getCustomers()
                            }
                            // Configure the refreshing colors
                            swipeRefresh.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )
                            (adapter as CustomersAdapter).notifyDataSetChanged();

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // Now we call setRefreshing(false) to signal refresh has finished
                            customerSwipeContainer.isRefreshing = false;

                            Toast.makeText(activity,"${customersList.count()} Customers Loaded",Toast.LENGTH_SHORT).show()

                            //search listener
                            customers_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as CustomersAdapter).filter.filter(newText)
                                    return false
                                }

                            })
                        }

                         */

                        showCustomers()
                    }


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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }






    override fun onCustomerCellClickListener(data:Customer) {
        println("Cell clicked with customer: ${data.sysname}")
        data.let {
            // val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            val directions = CustomerListFragmentDirections.navigateToCustomer(it.ID)
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
    /*
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CustomerListFragment().apply {

            }
    }

     */
}