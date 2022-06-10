package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_equipment_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface EquipmentCellClickListener {
    fun onEquipmentCellClickListener(data:Equipment)
}


class EquipmentListFragment : Fragment(), EquipmentCellClickListener {


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView: androidx.appcompat.widget.SearchView
    lateinit var swipeRefresh: SwipeRefreshLayout



    // lateinit var  btn: Button

    lateinit var adapter:EquipmentAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_equipment_list, container, false)


        //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
        // progBar.alpha = 0.2f

        val emptyList:MutableList<Equipment> = mutableListOf()

        adapter = EquipmentAdapter(emptyList,myView.context, this)





        //(activity as AppCompatActivity).supportActionBar?.title = "Equipment List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment_list)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.equipment_search)
        swipeRefresh= view.findViewById(R.id.customerSwipeContainer)

        getEquipment()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("equipmentList")
    }


    private fun getEquipment(){
        println("getEquipment")


        // println("pgsBar = $pgsBar")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/equipmentList.php"

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
                    val equipment:JSONArray = parentObject.getJSONArray("equipment")
                    println("equipment = $equipment")
                    println("equipment count = ${equipment.length()}")



                    val gson = GsonBuilder().create()
                    val equipmentList = gson.fromJson(equipment.toString() , Array<Equipment>::class.java).toMutableList()


                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)


                        adapter = activity?.let {
                            EquipmentAdapter(equipmentList,
                                it, this@EquipmentListFragment)
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
                            getEquipment()
                        }
                        // Configure the refreshing colors
                        // Configure the refreshing colors
                        swipeRefresh.setColorSchemeResources(
                            R.color.button,
                            R.color.black,
                            R.color.colorAccent,
                            R.color.colorPrimaryDark
                        )

                        customerSwipeContainer.isRefreshing = false


                        //search listener
                        equipment_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {


                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as EquipmentAdapter).filter.filter(newText)
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
        postRequest1.tag = "equipmentList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onEquipmentCellClickListener(data:Equipment) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()

        data.let {
            val directions = EquipmentListFragmentDirections.navigateToEquipment(it)
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

}