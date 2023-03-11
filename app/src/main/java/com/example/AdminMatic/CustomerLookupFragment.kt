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
import com.AdminMatic.databinding.FragmentCustomerLookupBinding


class CustomerLookupFragment : Fragment(), CustomerCellClickListener {

    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    private lateinit var adapter:CustomersAdapter

    private var _binding: FragmentCustomerLookupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()
        _binding = FragmentCustomerLookupBinding.inflate(inflater, container, false)
        myView = binding.root

        val emptyList:MutableList<Customer> = mutableListOf()
        adapter = CustomersAdapter(emptyList, this, true)
        //(activity as AppCompatActivity).supportActionBar?.title = "Customer List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.customer_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")

        binding.customerLookupAddCustomerBtn.setOnClickListener {
            val directions = CustomerLookupFragmentDirections.navigateToNewCustomer(null)
            myView.findNavController().navigate(directions)
        }

        showCustomers()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("customerList")
    }


    private fun showCustomers(){
        println("showCustomers")
        binding.customerLookupRv.apply {
            layoutManager = LinearLayoutManager(activity)



            if(GlobalVars.customerList != null) {
                adapter = activity?.let {
                    CustomersAdapter(GlobalVars.customerList!!, this@CustomerLookupFragment, true)
                }

                val itemDecoration: ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                binding.customerLookupRv.addItemDecoration(itemDecoration)



                //search listener
                binding.customerLookupSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        myView.hideKeyboard()
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {

                        if (newText.isNullOrBlank()) {
                            binding.customerLookupRv.visibility = View.INVISIBLE
                            binding.customerLookupTv.visibility = View.VISIBLE
                        }
                        else {
                            binding.customerLookupRv.visibility = View.VISIBLE
                            binding.customerLookupTv.visibility = View.INVISIBLE
                        }

                        println("onQueryTextChange = $newText")
                        (adapter as CustomersAdapter).filter.filter(newText)
                        return false
                    }

                })
            }
        }
    }


    override fun onCustomerCellClickListener(data:Customer) {
        println("Cell clicked with customer: ${data.sysname}")
        data.let {
            // val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            val directions = CustomerLookupFragmentDirections.navigateToCustomer(it.ID)
            myView.findNavController().navigate(directions)
        }
    }


}