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

    private lateinit var woItem:WoItem
    private var unit:String = ""

    lateinit var globalVars:GlobalVars

    lateinit var myView:View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")!!
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

        layoutViews()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("usage")
    }

    private fun layoutViews() {

        binding.usageRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)

            val usageList = woItem.usage!!.toMutableList()

            var totalHours = 0.0
            usageList.forEach {
                totalHours += it.qty.toDouble()
            }

            if (woItem.type == "1") {
                binding.usageHeaderLaborLayout.visibility = View.VISIBLE
                binding.usageHeaderMaterialLayout.visibility = View.INVISIBLE

                unit = getString(R.string.hour)
            }
            else {
                binding.usageHeaderLaborLayout.visibility = View.INVISIBLE
                binding.usageHeaderMaterialLayout.visibility = View.VISIBLE

                unit = getString(R.string.unit)
            }

            binding.usageFooterTv.text = getString(R.string.usage_history_footer_material, totalHours, unit)


            adapter = activity?.let {
                UsageHistoryAdapter(usageList, myView.context, unit)
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.usageRecyclerView.addItemDecoration(itemDecoration)





            (adapter as UsageHistoryAdapter).notifyDataSetChanged()

            hideProgressView()

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
