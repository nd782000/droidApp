package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContractListSettingsBinding
import com.AdminMatic.databinding.FragmentEmployeeListSettingsBinding
import com.AdminMatic.databinding.FragmentImageListSettingsBinding
import com.AdminMatic.databinding.FragmentLeadListSettingsBinding


class ImageListSettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var filterBy = 0
    private var orderBy = 0
    private var dates = 0
    private var searchByTag = false

    private lateinit var filterByAdapter: ArrayAdapter<String>
    private lateinit var orderByAdapter: ArrayAdapter<String>
    private lateinit var datesAdapter: ArrayAdapter<String>

    //lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterBy = it.getInt("filterBy")
            orderBy = it.getInt("orderBy")
            dates = it.getInt("dates")
            searchByTag = it.getBoolean("searchByTag")
        }
    }

    private var _binding: FragmentImageListSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentImageListSettingsBinding.inflate(inflater, container, false)
        myView = binding.root

        //globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_list_settings)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("handleOnBackPressed")
                setFragmentResult("imageListSettings", bundleOf("filterBy" to filterBy, "orderBy" to orderBy, "dates" to dates, "searchByTag" to searchByTag, "clearSearch" to false))
                myView.findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        return myView
    }

    /*
    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("documents")
    }

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filterByArray = arrayOf(
            getString(R.string.image_filter_all),
            getString(R.string.image_filter_my),
            getString(R.string.image_filter_portfolio),
            getString(R.string.image_filter_fieldnote),
            getString(R.string.image_filter_task)
        )
        filterByAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            filterByArray
        )
        binding.filterSpinner.adapter = filterByAdapter
        binding.filterSpinner.onItemSelectedListener = this@ImageListSettingsFragment

        val orderByArray = arrayOf(
            getString(R.string.image_order_newest),
            getString(R.string.image_order_oldest),
            getString(R.string.image_order_most_liked)
        )
        orderByAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            orderByArray
        )
        binding.orderSpinner.adapter = orderByAdapter
        binding.orderSpinner.onItemSelectedListener = this@ImageListSettingsFragment

        val datesArray = arrayOf(
            getString(R.string.image_dates_all),
            getString(R.string.image_dates_today),
            getString(R.string.image_dates_yesterday),
            getString(R.string.image_dates_this_week),
            getString(R.string.image_dates_last_week),
            getString(R.string.image_dates_this_month),
            getString(R.string.image_dates_last_month),
            getString(R.string.image_dates_this_year),
            getString(R.string.image_dates_last_year),

        )
        datesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            datesArray
        )
        binding.datesSpinner.adapter = datesAdapter
        binding.datesSpinner.onItemSelectedListener = this@ImageListSettingsFragment

        binding.searchByTagSwitch.setOnCheckedChangeListener { _, isChecked ->
            searchByTag = isChecked
        }

        binding.clearAllFiltersBtn.setOnClickListener {
            setFragmentResult("imageListSettings", bundleOf("filterBy" to 0, "orderBy" to 0, "dates" to 0, "searchByTag" to false, "clearSearch" to true))
            myView.findNavController().navigateUp()
        }


        // populate fields
        binding.filterSpinner.setSelection(filterBy)
        binding.orderSpinner.setSelection(orderBy)
        binding.datesSpinner.setSelection(dates)
        binding.searchByTagSwitch.isChecked = searchByTag
        binding.searchByTagSwitch.jumpDrawablesToCurrentState()


    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            R.id.filter_spinner -> {
                filterBy = position
            }
            R.id.order_spinner -> {
                orderBy = position
            }
            R.id.dates_spinner -> {
                dates = position
            }
        }
    }

}
