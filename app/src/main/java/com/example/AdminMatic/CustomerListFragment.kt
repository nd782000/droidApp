package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCustomerListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface CustomerCellClickListener {
    fun onCustomerCellClickListener(data:Customer)
}

//great resource fo recyclerView inf
//https://guides.codepath.com/android/using-the-recyclerview


class CustomerListFragment : Fragment(), CustomerCellClickListener {

    private lateinit var globalVars: GlobalVars
    private lateinit var adapter: CustomersAdapter

    private var _binding: FragmentCustomerListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()
        _binding = FragmentCustomerListBinding.inflate(inflater, container, false)
        myView = binding.root


        val emptyList: MutableList<Customer> = mutableListOf()
        adapter = CustomersAdapter(emptyList, this, false)
        //(activity as AppCompatActivity).supportActionBar?.title = "Customer List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
            getString(R.string.customer_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")


        binding.addCustomerBtn.setOnClickListener {
            if (GlobalVars.permissions!!.customersEdit == "1") {
                val directions = CustomerListFragmentDirections.navigateToCustomerLookup()
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers_edit))
            }
        }

        //getCustomers()

        showCustomers()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("customerList")
    }


    fun showCustomers() {
        println("showCustomers")
        binding.listRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)

            hideProgressView()


            if (GlobalVars.customerList != null) {
                adapter = activity?.let {


                    CustomersAdapter(
                        GlobalVars.customerList!!,
                        this@CustomerListFragment,
                        false
                    )


                }

                val itemDecoration: ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                binding.listRecyclerView.addItemDecoration(itemDecoration)


                // Setup refresh listener which triggers new data loading
                binding.customerSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    binding.customersSearch.setQuery("", false)
                    binding.customersSearch.clearFocus()
                    getCustomers()
                }
                // Configure the refreshing colors
                binding.customerSwipeContainer.setColorSchemeResources(
                    R.color.button,
                    R.color.black,
                    R.color.colorAccent,
                    R.color.colorPrimaryDark
                )
                //(adapter as CustomersAdapter).notifyDataSetChanged()

                // Remember to CLEAR OUT old items before appending in the new ones

                // Now we call setRefreshing(false) to signal refresh has finished
                binding.customerSwipeContainer.isRefreshing = false
/*

                Toast.makeText(
                    activity,
                    "${GlobalVars.customerList!!.count()} Customers Loaded",
                    Toast.LENGTH_SHORT
                ).show()
*/

                //search listener
                binding.customersSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        myView.hideKeyboard()
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        (adapter as CustomersAdapter).filter.filter(newText)
                        return false
                    }

                })
            }

            binding.listRecyclerView.adapter!!.notifyDataSetChanged()
        }
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
                println("Response $response")
                hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val customers: JSONArray = parentObject.getJSONArray("customers")

                        val gson = GsonBuilder().create()
                        GlobalVars.customerList =
                            gson.fromJson(customers.toString(), Array<Customer>::class.java)
                                .toMutableList()
                    }
                    showCustomers()

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
        postRequest1.tag = "customerList"
        VolleyRequestQueue.getInstance(requireActivity().application)
            .addToRequestQueue(postRequest1)
    }


    override fun onCustomerCellClickListener(data: Customer) {
        println("Cell clicked with customer: ${data.sysname}")
        data.let {
            // val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            val directions = CustomerListFragmentDirections.navigateToCustomer(it.ID)
            myView.findNavController().navigate(directions)
        }
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }
}