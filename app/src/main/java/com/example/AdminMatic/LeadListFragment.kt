package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.AdminMatic.GlobalVars.Companion.globalLeadList
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_lead_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_lead_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_lead_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



interface LeadCellClickListener {
    fun onLeadCellClickListener(data:Lead)
}


class LeadListFragment : Fragment(), LeadCellClickListener {


    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    private lateinit var pgsBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView:androidx.appcompat.widget.SearchView
    private lateinit var swipeRefresh:SwipeRefreshLayout
    private lateinit var allCl: ConstraintLayout

    //lateinit var  newLeadBtn: Button
    private lateinit var leadCountTv: TextView
    private lateinit var mapBtn: Button

    lateinit var adapter:LeadsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_lead_list, container, false)


        //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
        // progBar.alpha = 0.2f

        val emptyList:MutableList<Lead> = mutableListOf()

        adapter = LeadsAdapter(emptyList, this.myView.context, this)





        //(activity as AppCompatActivity).supportActionBar?.title = "Lead List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_list)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

        (activity as MainActivity?)!!.setLeadList(this)

        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.leads_search)
        swipeRefresh = view.findViewById(R.id.customerSwipeContainer)
        mapBtn = view.findViewById(R.id.map_btn)
        leadCountTv = view.findViewById(R.id.lead_count_textview)

        allCl = view.findViewById(R.id.all_cl)

        mapBtn.setOnClickListener{
            println("Map button clicked!")
            val directions = LeadListFragmentDirections.navigateToMap(1)
            myView.findNavController().navigate(directions)
        }
        getLeads()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("leadList")
    }


    fun getLeads(){
        println("getLeads")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/leads.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val leads:JSONArray = parentObject.getJSONArray("leads")
                    println("leads = $leads")
                    println("leads count = ${leads.length()}")


                    if (globalLeadList != null) {
                        globalLeadList!!.clear()
                    }
                    val gson = GsonBuilder().create()
                    globalLeadList = gson.fromJson(leads.toString() , Array<Lead>::class.java).toMutableList()
                    leadCountTv.text = getString(R.string.x_active_leads, globalLeadList!!.size)

                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)


                        adapter = activity?.let {
                            LeadsAdapter(
                                globalLeadList!!, context,this@LeadListFragment
                            )
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
                            getLeads()
                        }
                        // Configure the refreshing colors
                        // Configure the refreshing colors
                        swipeRefresh.setColorSchemeResources(
                            R.color.button,
                            R.color.black,
                            R.color.colorAccent,
                            R.color.colorPrimaryDark
                        )



                        //(adapter as LeadsAdapter).notifyDataSetChanged();

                        // Remember to CLEAR OUT old items before appending in the new ones

                        // ...the data has come back, add new items to your adapter...

                        // Now we call setRefreshing(false) to signal refresh has finished
                        customerSwipeContainer.isRefreshing = false

                        // Toast.makeText(activity,"${leadsList.count()} Leads Loaded",Toast.LENGTH_SHORT).show()



                        //search listener
                        leads_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {


                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as LeadsAdapter).filter.filter(newText)
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
            val directions = LeadListFragmentDirections.navigateToLead(it)
            myView.findNavController().navigate(directions)
        }


    }



    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        allCl.visibility = View.INVISIBLE

    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        allCl.visibility = View.VISIBLE
    }


}