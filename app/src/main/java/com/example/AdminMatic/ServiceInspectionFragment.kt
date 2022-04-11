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
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_equipment_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_equipment_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_equipment_list.*
import kotlinx.android.synthetic.main.fragment_service_inspection.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



interface ServiceInspectionCellClickListener {
    fun onServiceInspectionCellClickListener(data:InspectionQuestion)
}


class ServiceInspectionFragment : Fragment(), ServiceInspectionCellClickListener {

    private  var service: EquipmentService? = null
    lateinit var pgsBar: ProgressBar
    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var recyclerView: RecyclerView

    // lateinit var  btn: Button

    lateinit var adapter:ServiceInspectionAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable<EquipmentService?>("service")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_service_inspection, container, false)


        var emptyList:MutableList<InspectionQuestion> = mutableListOf()

        adapter = ServiceInspectionAdapter(emptyList,myView.context, this)


        //(activity as AppCompatActivity).supportActionBar?.title = "Equipment List"

        //TODO: fetch equipment name here instead of just ID
        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.inspection_page_title, service!!.equipmentID)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        //println(recyclerView)
        recyclerView = view.findViewById(R.id.service_inspection_recycler_view)

        println(recyclerView)


        pgsBar = view.findViewById(R.id.progressBar)


        getInspectionItems()


    }

    private fun getInspectionItems() {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/inspection.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var questions:JSONArray = parentObject.getJSONArray("questions")
                    println("questions = ${questions.toString()}")
                    println("questions count = ${questions.length()}")


                    val gson = GsonBuilder().create()
                    val questionsList = gson.fromJson(questions.toString() , Array<InspectionQuestion>::class.java).toMutableList()




                    service_inspection_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)


                        adapter = activity?.let {
                            ServiceInspectionAdapter(questionsList,
                                it, this@ServiceInspectionFragment)
                        }

                        val itemDecoration: ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        recyclerView.addItemDecoration(itemDecoration)

                        //for item animations
                        // recyclerView.itemAnimator = SlideInUpAnimator()


                        (adapter as ServiceInspectionAdapter).notifyDataSetChanged();

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
                params["ID"] = service!!.ID
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    override fun onServiceInspectionCellClickListener(data:InspectionQuestion) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity,"${data} Clicked",Toast.LENGTH_SHORT).show()
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
            EquipmentListFragment().apply {

            }
    }
}