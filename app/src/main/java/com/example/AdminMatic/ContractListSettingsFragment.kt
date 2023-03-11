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
import com.AdminMatic.databinding.FragmentLeadListSettingsBinding


class ContractListSettingsFragment : Fragment(), AdapterView.OnItemSelectedListener, EmployeeCellClickListener {

    private var status = ""
    private var salesRep = ""

    private lateinit var statusAdapter: ArrayAdapter<String>

    //lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString("status")!!
            salesRep = it.getString("salesRep")!!
        }

        println("Status: $status")
        println("SalesRep: $salesRep")
    }

    private var _binding: FragmentContractListSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentContractListSettingsBinding.inflate(inflater, container, false)
        myView = binding.root

        //globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_list_settings)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("handleOnBackPressed")
                setFragmentResult("contractListSettings", bundleOf("status" to status, "salesRep" to salesRep))
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

        val statusArray = arrayOf(
            "No Filter",
            getString(R.string.contract_status_new),
            getString(R.string.contract_status_sent),
            getString(R.string.contract_status_awarded),
            getString(R.string.contract_status_scheduled),
            getString(R.string.declined),
            getString(R.string.waiting),
            getString(R.string.canceled),
        )
        statusAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            statusArray
        )
        binding.statusSpinner.adapter = statusAdapter
        binding.statusSpinner.onItemSelectedListener = this@ContractListSettingsFragment

        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@ContractListSettingsFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.salesRepSearchRv.addItemDecoration(itemDecoration)

            binding.salesRepSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.salesRepSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.salesRepSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as EmployeesAdapter).filter.filter(newText)
                    return false
                }

            })


            val closeButton: View? = binding.salesRepSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.salesRepSearch.setQuery("", false)
                salesRep = ""
                myView.hideKeyboard()
                binding.salesRepSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.salesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    //binding.salesRepSearch.setQuery(lead!!.custName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        binding.clearAllFiltersBtn.setOnClickListener {
            setFragmentResult("contractListSettings", bundleOf("status" to "", "salesRep" to ""))
            myView.findNavController().navigateUp()
        }


        // populate fields
        if (status.isNotBlank()) {
            binding.statusSpinner.setSelection(status.toInt() + 1)
        }

        if (salesRep.isNotBlank()) {
            for (i in 0 until GlobalVars.employeeList!!.size) {
                if (salesRep == GlobalVars.employeeList!![i].ID) {
                    binding.salesRepSearch.setQuery(GlobalVars.employeeList!![i].name, false)
                    binding.salesRepSearch.clearFocus()
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                    break
                }
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (parent!!.id) {
            R.id.status_spinner -> {

                if (position == 0) {
                    status = ""
                }
                else {
                    status = (position - 1).toString()
                }
            }
        }
    }

    override fun onEmployeeCellClickListener(data: Employee) {
        salesRep = data.ID
        binding.salesRepSearch.setQuery(data.name, false)
        binding.salesRepSearch.clearFocus()
        myView.hideKeyboard()
    }


}
