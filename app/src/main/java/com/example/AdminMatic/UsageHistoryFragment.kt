package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentUsageBinding
import com.AdminMatic.databinding.FragmentUsageHistoryBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.HashMap


class UsageHistoryFragment : Fragment() {

    private var woItemID:String = ""
    private var type:String = ""
    private var unit:String = ""

    lateinit var globalVars:GlobalVars

    lateinit var myView:View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItemID = it.getString("woItemID")!!
            type = it.getString("type")!!
            unit = it.getString("unit")!!
        }
    }

    private var _binding: FragmentUsageHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUsageHistoryBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.full_usage_history)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideProgressView()
        getUsage()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("usage")
    }

    private fun getUsage(){
        println("getUsage")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/usage.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("get/usage response: $response")

                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val usageArray: JSONArray = parentObject.getJSONArray("usages")

                        val gson = GsonBuilder().create()
                        val usageList = gson.fromJson(usageArray.toString(), Array<Usage>::class.java).toMutableList()

                        binding.usageRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                UsageHistoryAdapter(usageList, myView.context, unit)
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.usageRecyclerView.addItemDecoration(itemDecoration)

                            var totalHours = 0.0
                            usageList.forEach {
                                totalHours += it.qty.toDouble()
                            }

                            if (type == "1") {
                                binding.usageHeaderLaborLayout.visibility = View.VISIBLE
                                binding.usageHeaderMaterialLayout.visibility = View.INVISIBLE
                                binding.usageFooterTv.text = getString(R.string.usage_footer_text, usageList.size, totalHours)
                            }
                            else {
                                binding.usageHeaderLaborLayout.visibility = View.INVISIBLE
                                binding.usageHeaderMaterialLayout.visibility = View.VISIBLE
                                binding.usageFooterTv.text = getString(R.string.usage_history_footer_material, totalHours, unit)
                            }

                            (adapter as UsageHistoryAdapter).notifyDataSetChanged()

                            hideProgressView()

                        }
                    }

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
                params["woItemID"] = woItemID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "usage"
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

}
