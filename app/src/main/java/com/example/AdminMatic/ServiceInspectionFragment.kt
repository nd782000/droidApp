package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

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

    private lateinit var  submitBtn: Button

    lateinit var adapter:ServiceInspectionAdapter

    // The list is declared here so the recycler can be passed a reference to it and the radio buttons can edit it
    var questionsList:MutableList<InspectionQuestion> = emptyList<InspectionQuestion>().toMutableList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable("service")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_service_inspection, container, false)


        val emptyList:MutableList<InspectionQuestion> = mutableListOf()

        adapter = ServiceInspectionAdapter(emptyList, this)


        //(activity as AppCompatActivity).supportActionBar?.title = "Equipment List"

        //TODO: fetch equipment name here instead of just ID
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.inspection_page_title, service!!.equipmentID)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        //println(recyclerView)
        recyclerView = view.findViewById(R.id.service_inspection_recycler_view)

        pgsBar = view.findViewById(R.id.progressBar)
        submitBtn = view.findViewById(R.id.service_inspection_submit_btn)
        submitBtn.setOnClickListener{
            println(questionsList[0].answer)
            val gson = GsonBuilder().disableHtmlEscaping().create()
            println("questions = " + gson.toJson(questionsList))
        }

        getInspectionItems()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("serviceInspection")
    }

    private fun getInspectionItems() {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/inspection.php"

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
                    val questions:JSONArray = parentObject.getJSONArray("questions")
                    println("questions = $questions")
                    //println("questions count = ${questions.length()}")

                    val gson = GsonBuilder().create()
                    questionsList = gson.fromJson(questions.toString(), Array<InspectionQuestion>::class.java).toMutableList()

                    service_inspection_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)

                        adapter = activity?.let {
                            ServiceInspectionAdapter(
                                questionsList,
                                this@ServiceInspectionFragment
                            )
                        }


                        val itemDecoration: ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        recyclerView.addItemDecoration(itemDecoration)

                        //for item animations
                        // recyclerView.itemAnimator = SlideInUpAnimator()




                        //(adapter as ServiceInspectionAdapter).notifyDataSetChanged()
                        println(adapter!!.itemCount)

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
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "serviceInspection"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun submitInspection() {

        //Todo: check for status here, line 395 at https://github.com/nd782000/adminMatic.ios/blob/master/AdminMatic/Equipment/EquipmentInspectionViewController.swift

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/update/equipmentServiceComplete.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(com.example.AdminMatic.myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        hideProgressView()
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

                val gson = GsonBuilder().disableHtmlEscaping().create()
                gson.toJson(questionsList)

                val params: MutableMap<String, String> = java.util.HashMap()
                params["ID"] = service!!.ID
                params["completeValue"] = service!!.completionMileage.toString()
                params["completedBy"] = loggedInEmployee!!.ID
                params["completionNotes"] = service!!.notes.toString()
                params["nextValue"] = service!!.nextValue.toString()
                params["questions"] = gson.toJson(questionsList)
                params["status"] = service!!.status.toString()
                params["type"] = service!!.type
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["companyUnique"] = loggedInEmployee!!.companyUnique

                println("params = $params")
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
        Toast.makeText(activity,"$data Clicked",Toast.LENGTH_SHORT).show()
    }

}