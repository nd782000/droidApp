package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContractListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface ContractCellClickListener {
    fun onContractCellClickListener(data:Contract)
}


class ContractListFragment : Fragment(), ContractCellClickListener {

    private lateinit var status:String
    private lateinit var salesRep:String

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    lateinit var adapter:ContractsAdapter

    private var dataLoaded = false


    private var _binding: FragmentContractListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("contractListSettings") { _, bundle ->
            println("fragnmentResultListener")
            val newStatus = bundle.getString("status")
            val newSalesRep = bundle.getString("salesRep")

            if (newStatus != status || newSalesRep != salesRep ) {
                status = newStatus!!
                salesRep = newSalesRep!!

                if (status != "" || salesRep != "") {
                    println("setting button color yellow")
                    ImageViewCompat.setImageTintList(binding.settingsIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, R.color.settingsActive)))
                }
                else {
                    println("setting button color default")
                    ImageViewCompat.setImageTintList(binding.settingsIv, null)
                }

                getContracts()
            }

            println("Status: $status")
            println("SalesRep: $salesRep")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (!dataLoaded) {

            status = ""
            salesRep = ""

            println("onCreateView")
            globalVars = GlobalVars()
            _binding = FragmentContractListBinding.inflate(inflater, container, false)
            myView = binding.root

            //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
            // progBar.alpha = 0.2f

            val emptyList: MutableList<Contract> = mutableListOf()

            adapter = ContractsAdapter(emptyList, this.myView.context, this)


        }





        //(activity as AppCompatActivity).supportActionBar?.title = "Contract List"
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contracts)


        // Inflate the layout for this fragment

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")

        if (!dataLoaded) {
            binding.newContractBtn.setOnClickListener {
                if (GlobalVars.permissions!!.contractsEdit == "1") {
                    val directions = ContractListFragmentDirections.navigateToNewEditContract(null)
                    myView.findNavController().navigate(directions)
                } else {
                    com.example.AdminMatic.globalVars.simpleAlert(
                        myView.context,
                        getString(R.string.access_denied),
                        getString(R.string.no_permission_contracts_edit)
                    )
                }
            }
            binding.settingsBtn.setOnClickListener {
                val directions = ContractListFragmentDirections.navigateToContractListSettings(status, salesRep)
                myView.findNavController().navigate(directions)
            }

            dataLoaded = true
            getContracts()
        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contractList")
    }

    private fun getContracts(){
        println("getContracts")


        // println("pgsBar = $pgsBar")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/contracts.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        //val preferences =
        //this.requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
        // val session = preferences.getString("sessionKey","")
        //val companyUnique = preferences.getString("companyUnique","")


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Get contracts esponse $response")

                hideProgressView()

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

                        binding.contractCountTextview.text =
                            getString(R.string.x_active_contracts, contractsList.size)


                        binding.listRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                ContractsAdapter(contractsList, context, this@ContractListFragment)
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(
                                    myView.context,
                                    DividerItemDecoration.VERTICAL
                                )
                            binding.listRecyclerView.addItemDecoration(itemDecoration)

                            //for item animations
                            // recyclerView.itemAnimator = SlideInUpAnimator()


                            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
                            // Setup refresh listener which triggers new data loading
                            // Setup refresh listener which triggers new data loading
                            binding.customerSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                                // Make sure you call swipeContainer.setRefreshing(false)
                                // once the network request has completed successfully.
                                //fetchTimelineAsync(0)
                                binding.contractsSearch.setQuery("", false)
                                binding.contractsSearch.clearFocus()
                                getContracts()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            binding.customerSwipeContainer.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )



                            (adapter as ContractsAdapter).notifyDataSetChanged()

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            binding.customerSwipeContainer.isRefreshing = false

                            // Toast.makeText(activity,"${contractsList.count()} Contracts Loaded",Toast.LENGTH_SHORT).show()


                            //search listener
                            binding.contractsSearch.setOnQueryTextListener(object :
                                SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    myView.hideKeyboard()
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
                if (status.isNotBlank()) {
                    params["status"] = status
                }
                if (salesRep.isNotBlank()) {
                    params["salesRep"] = salesRep
                }
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "contractList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onContractCellClickListener(data:Contract) {
        //Toast.makeText(activity,"${data.custName} Clicked",Toast.LENGTH_SHORT).show()
        myView.hideKeyboard()

        data.let {
            val directions = ContractListFragmentDirections.navigateToContract(data.ID)
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