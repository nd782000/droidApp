package com.example.AdminMatic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEditItemVendorBinding
import com.AdminMatic.databinding.FragmentUsageBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import java.util.Timer
import kotlin.concurrent.schedule

class EditItemVendorFragment : Fragment(), VendorCellClickListener {

    private var item: Item? = null
    private var vendor: Vendor? = null

    private var editMode = false
    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var vendorList = mutableListOf<Vendor>()


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item")
            vendor = it.getParcelable("vendor")
        }
        
        if (vendor != null) {
            editMode = true
        }
        
    }

    private var _binding: FragmentEditItemVendorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditItemVendorBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_vendor_for_x, item!!.name)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_vendor_for_x, item!!.name)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgressView()

        // Flag edits made false after all the views have time to set their states
        Timer("EditItemVendorEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        if (editMode) {
            layoutViews()
        }
        else {
            getVendors()
        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("editItemVendorListFragment")
    }

    private fun getVendors(){
        println("getVendors")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/vendors.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val vendors: JSONArray = parentObject.getJSONArray("vendors")
                        println("vendors = $vendors")
                        println("vendors count = ${vendors.length()}")

                        val gson = GsonBuilder().create()
                        vendorList = gson.fromJson(vendors.toString(), Array<Vendor>::class.java).toMutableList()

                        vendorList.forEach {
                            if (it.itemString == null) {
                                it.itemString = ""
                            }
                        }

                        layoutViews()

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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "editItemVendorListFragment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {
        if (!editMode) {
            // set up search

            binding.vendorSearchRv.apply {
                layoutManager = LinearLayoutManager(activity)

                /*
                layoutManager = LinearLayoutManager(myView.context).apply {
                    stackFromEnd = true
                    reverseLayout = false
                }
                */

                adapter = activity?.let {
                    VendorsAdapter(
                        vendorList,
                        this@EditItemVendorFragment
                    )
                }

                val itemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
                itemDecoration.setDrawable(AppCompatResources.getDrawable(context, R.drawable.opaque_divider)!!)
                addItemDecoration(itemDecoration)

                /*
                var itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                binding.vendorSearchRv.addItemDecoration(itemDecoration)

                 */

                binding.vendorSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        binding.vendorSearchRv.visibility = View.INVISIBLE
                        myView.hideKeyboard()
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        if (newText == "") {
                            binding.vendorSearchRv.visibility = View.INVISIBLE
                        }
                        else {
                            binding.vendorSearchRv.visibility = View.VISIBLE
                        }
                        (adapter as VendorsAdapter).filter.filter(newText)
                        return false
                    }

                })


                val closeButton: View? = binding.vendorSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
                closeButton?.setOnClickListener {
                    binding.vendorSearch.setQuery("", false)
                    //equipment.dealer = "0"
                    //equipment.dealerName = ""
                    myView.hideKeyboard()
                    binding.vendorSearch.clearFocus()
                    binding.vendorSearchRv.visibility = View.INVISIBLE
                }

                binding.vendorSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                    if (!isFocused) {
                        //binding.vendorSearch.setQuery(equipment.dealerName, false)
                        binding.vendorSearchRv.visibility = View.INVISIBLE
                    }
                }
            }

        }
        else {
            println("setting vendor search to disabled")
            binding.vendorSearch.setQuery(vendor!!.name, false)
            globalVars.enableSearchView(binding.vendorSearch, false)
        }

        // Cost
        binding.costEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.costEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        if (editMode) {
            binding.costEt.setText(vendor!!.cost)
        }

        // Suggested Price
        binding.priceEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.priceEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        if (editMode) {
            binding.priceEt.setText(vendor!!.price)
        }

        // Submit Button
        binding.submitBtn.setOnClickListener {
            myView.findNavController().navigateUp()
        }

        hideProgressView()

    }
    

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun onVendorCellClickListener(data: Vendor) {
        binding.vendorSearch.setQuery(data.name, false)
        binding.vendorSearch.clearFocus()
        myView.hideKeyboard()
    }


}
