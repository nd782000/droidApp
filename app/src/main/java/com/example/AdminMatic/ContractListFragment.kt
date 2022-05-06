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

import kotlinx.android.synthetic.main.fragment_contract_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_contract_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_contract_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



interface ContractCellClickListener {
    fun onContractCellClickListener(data:Contract)
}


class ContractListFragment : Fragment(), ContractCellClickListener {


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView:androidx.appcompat.widget.SearchView
    lateinit var footerCL:androidx.constraintlayout.widget.ConstraintLayout
    lateinit var swipeRefresh:SwipeRefreshLayout
    lateinit var contractCountText:TextView


    // lateinit var  btn: Button

    lateinit var adapter:ContractsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_contract_list, container, false)


        //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
        // progBar.alpha = 0.2f

        val emptyList:MutableList<Contract> = mutableListOf()

        adapter = ContractsAdapter(emptyList, this.myView.context, this)





        //(activity as AppCompatActivity).supportActionBar?.title = "Contract List"
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contracts)


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.contracts_search)
        swipeRefresh = view.findViewById(R.id.customerSwipeContainer)
        contractCountText = view.findViewById(R.id.contract_count_textview)
        footerCL = view.findViewById(R.id.footer_cl)

        getContracts()


    }


    private fun getContracts(){
        println("getContracts")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/contracts.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
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
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        val contracts:JSONArray = parentObject.getJSONArray("contracts")
                        println("contracts = $contracts")
                        println("contracts count = ${contracts.length()}")



                        val gson = GsonBuilder().create()
                        val contractsList = gson.fromJson(contracts.toString() , Array<Contract>::class.java).toMutableList()

                        contractCountText.text = getString(R.string.x_active_contracts, contractsList.size)


                        list_recycler_view.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                ContractsAdapter(contractsList, context,this@ContractListFragment)
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
                                searchView.setQuery("", false)
                                searchView.clearFocus()
                                getContracts()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            swipeRefresh.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )



                            (adapter as ContractsAdapter).notifyDataSetChanged();

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            customerSwipeContainer.isRefreshing = false

                            // Toast.makeText(activity,"${contractsList.count()} Contracts Loaded",Toast.LENGTH_SHORT).show()



                            //search listener
                            contracts_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as ContractsAdapter).filter.filter(newText)
                                    return false
                                }

                            })
                        }
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
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    override fun onContractCellClickListener(data:Contract) {
        //Toast.makeText(activity,"${data.custName} Clicked",Toast.LENGTH_SHORT).show()

        data.let {
            val directions = ContractListFragmentDirections.navigateToContract(data)
            myView.findNavController().navigate(directions)
        }


    }



    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        searchView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        footerCL.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        searchView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        footerCL.visibility = View.VISIBLE
    }




}