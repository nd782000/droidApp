package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
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
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentDocumentsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface DocumentCellClickListener {
    fun onDocumentCellClickListener(data:Document)
    fun onShareButtonClicked(data:Document)
}


class DocumentsFragment : Fragment(), DocumentCellClickListener {

    private  var customer: Customer? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customer = it.getParcelable("customer")
        }
    }

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_documents, customer!!.sysname)

        return myView
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("documents")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDocuments()

        /*
        if (customer!!.documents == null) {
            employee!!.licenses = emptyArray()
        }

        val licenseList = employee!!.licenses!!.toMutableList()

        if (licenseList.size > 0) {

            binding.licensesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = activity?.let {
                    LicensesAdapter(
                        licenseList,
                        context
                    )
                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                binding.licensesRecyclerView.addItemDecoration(itemDecoration)
                //(adapter as WoItemsAdapter).notifyDataSetChanged()
            }
        }
        else {
            binding.licensesNoLicensesTv.visibility = View.VISIBLE
        }

         */
    }


    private fun getDocuments(){
        println("getDocuments")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/documents.php"

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
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val documents: JSONArray = parentObject.getJSONArray("documents")
                        println("documents = $documents")
                        println("documents count = ${documents.length()}")

                        val gson = GsonBuilder().create()
                        val documentsList =
                            gson.fromJson(documents.toString(), Array<Document>::class.java)
                                .toMutableList()



                        if (documentsList.size > 0) {

                            binding.recyclerView.apply {
                                layoutManager = LinearLayoutManager(activity)
                                adapter = activity?.let {
                                    DocumentsAdapter(
                                        documentsList,
                                        context, this@DocumentsFragment
                                    )
                                }

                                val itemDecoration: RecyclerView.ItemDecoration =
                                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                                binding.recyclerView.addItemDecoration(itemDecoration)
                                //(adapter as WoItemsAdapter).notifyDataSetChanged()
                            }
                        }
                        else {
                            binding.noDocumentsTv.visibility = View.VISIBLE
                        }

                        hideProgressView()

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
                params["custID"] = customer!!.ID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "documents"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun onDocumentCellClickListener(data:Document) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.name} Clicked",Toast.LENGTH_SHORT).show()

        if (data.type == "pdf") {
            data.let {
                val directions = DocumentsFragmentDirections.navigateToPdf(it)
                myView.findNavController().navigate(directions)
            }
        }
        else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.adminmatic.com" + data.fname))
            startActivity(intent)
        }
    }

    override fun onShareButtonClicked(data: Document) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = data.name + ": " + "https://www.adminmatic.com" + data.fname
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, data.name)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

}
