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
import com.AdminMatic.databinding.FragmentInvoiceListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface InvoiceCellClickListener {
    fun onInvoiceCellClickListener(data:Invoice)
}


class InvoiceListFragment : Fragment(), InvoiceCellClickListener {

    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    lateinit var adapter:InvoicesAdapter

    private var _binding: FragmentInvoiceListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentInvoiceListBinding.inflate(inflater, container, false)
        myView = binding.root

        val emptyList:MutableList<Invoice> = mutableListOf()
        adapter = InvoicesAdapter(emptyList, com.example.AdminMatic.myView.context, this)
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.invoice_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("onViewCreated")
        getInvoices()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("invoiceList")
    }


    private fun getInvoices(){
        println("getInvoices")


        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/invoices.php"

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

                        val invoices: JSONArray = parentObject.getJSONArray("invoices")
                        println("invoices = $invoices")
                        println("invoices count = ${invoices.length()}")


                        val gson = GsonBuilder().create()
                        val invoicesList =
                            gson.fromJson(invoices.toString(), Array<Invoice>::class.java)
                                .toMutableList()


                        binding.listRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                InvoicesAdapter(
                                    invoicesList, it,
                                    this@InvoiceListFragment
                                )
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
                                binding.invoicesSearch.setQuery("", false)
                                binding.invoicesSearch.clearFocus()
                                getInvoices()
                            }
                            // Configure the refreshing colors
                            // Configure the refreshing colors
                            binding.customerSwipeContainer.setColorSchemeResources(
                                R.color.button,
                                R.color.black,
                                R.color.colorAccent,
                                R.color.colorPrimaryDark
                            )


                            //(adapter as InvoicesAdapter).notifyDataSetChanged()

                            // Remember to CLEAR OUT old items before appending in the new ones

                            // ...the data has come back, add new items to your adapter...

                            // Now we call setRefreshing(false) to signal refresh has finished
                            binding.customerSwipeContainer.isRefreshing = false

                            //  Toast.makeText(activity,"${invoicesList.count()} Invoices Loaded",Toast.LENGTH_SHORT).show()


                            //search listener
                            binding.invoicesSearch.setOnQueryTextListener(object :
                                SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    myView.hideKeyboard()
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    println("onQueryTextChange = $newText")
                                    (adapter as InvoicesAdapter).filter.filter(newText)
                                    return false
                                }

                            })
                        }

                        binding.footerTv.text = getString(R.string.x_active_invoices, invoicesList.size)

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
        postRequest1.tag = "invoiceList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onInvoiceCellClickListener(data:Invoice) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        //Toast.makeText(activity,"${data.custName} Clicked",Toast.LENGTH_SHORT).show()

        data.let {
            val directions = InvoiceListFragmentDirections.navigateToInvoice(it)
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