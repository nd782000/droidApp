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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentLeadListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.globalLeadList
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



interface LeadCellClickListener {
    fun onLeadCellClickListener(data:Lead)
}


class LeadListFragment : Fragment(), LeadCellClickListener {

    private lateinit var status:String
    private lateinit var salesRep:String
    private lateinit var zone:String

    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    lateinit var adapter:LeadsAdapter

    private var dataLoaded = false

    private var _binding: FragmentLeadListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("leadListSettings") { _, bundle ->
            println("fragnmentResultListener")
            val newStatus = bundle.getString("status")
            val newSalesRep = bundle.getString("salesRep")
            val newZone = bundle.getString("zone")

            if (newStatus != status || newSalesRep != salesRep || newZone != zone) {
                status = newStatus!!
                salesRep = newSalesRep!!
                zone = newZone!!

                if (status != "" || salesRep != "" || zone != "") {
                    println("setting button color yellow")
                    ImageViewCompat.setImageTintList(binding.settingsIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, R.color.settingsActive)))
                }
                else {
                    println("setting button color default")
                    ImageViewCompat.setImageTintList(binding.settingsIv, null)
                }

                getLeads()
            }

            println("Status: $status")
            println("SalesRep: $salesRep")
            println("Zone: $zone")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!dataLoaded) {
            println("onCreateView")
            globalVars = GlobalVars()
            _binding = FragmentLeadListBinding.inflate(inflater, container, false)
            myView = binding.root
        }




        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

        if (!dataLoaded) {
            status = ""
            salesRep = ""
            zone = ""


            binding.mapBtn.setOnClickListener {
                println("Map button clicked!")
                val directions = LeadListFragmentDirections.navigateToMap(1)
                myView.findNavController().navigate(directions)
            }

            binding.newLeadBtn.setOnClickListener {
                if (GlobalVars.permissions!!.leadsEdit == "1") {
                    val directions = LeadListFragmentDirections.navigateToNewEditLead(null)
                    myView.findNavController().navigate(directions)
                } else {
                    globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_leads_edit))
                }
            }

            binding.settingsBtn.setOnClickListener {
                val directions = LeadListFragmentDirections.navigateToLeadListSettingsFragment(status, salesRep, zone)
                myView.findNavController().navigate(directions)
            }




            dataLoaded = true
            val emptyList: MutableList<Lead> = mutableListOf()
            adapter = LeadsAdapter(emptyList, this.myView.context, this)

            (activity as MainActivity?)!!.setLeadList(this)

            getLeads()
        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("leadList")
    }

    fun getLeads(){
        println("getLeads")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/leads.php"

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

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val leads: JSONArray = parentObject.getJSONArray("leads")
                        println("leads = $leads")
                        println("leads count = ${leads.length()}")


                        if (globalLeadList != null) {
                            globalLeadList!!.clear()
                        }
                        val gson = GsonBuilder().create()
                        globalLeadList = gson.fromJson(leads.toString(), Array<Lead>::class.java).toMutableList()
                        binding.leadCountTextview.text = getString(R.string.x_active_leads, globalLeadList!!.size)

                        binding.listRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)


                            adapter = activity?.let {
                                LeadsAdapter(
                                    globalLeadList!!, context, this@LeadListFragment
                                )
                            }

                            val itemDecoration: ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
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
                                binding.leadsSearch.setQuery("", false)
                                binding.leadsSearch.clearFocus()
                                getLeads()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            binding.customerSwipeContainer.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )


                            //(adapter as LeadsAdapter).notifyDataSetChanged();

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            binding.customerSwipeContainer.isRefreshing = false

                            // Toast.makeText(activity,"${leadsList.count()} Leads Loaded",Toast.LENGTH_SHORT).show()


                            //search listener
                            binding.leadsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    myView.hideKeyboard()
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as LeadsAdapter).filter.filter(newText)
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
                if (status.isNotBlank()) {
                    params["status"] = status
                }
                if (salesRep.isNotBlank()) {
                    params["salesRep"] = salesRep
                }
                if (zone.isNotBlank()) {
                    params["zone"] = zone
                }
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "leadList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onLeadCellClickListener(data:Lead) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.custName} Clicked",Toast.LENGTH_SHORT).show()

        data.let {
            val directions = LeadListFragmentDirections.navigateToLead(it.ID)
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