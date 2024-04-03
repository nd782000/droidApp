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
import com.AdminMatic.databinding.FragmentEditItemVendorListBinding
import com.AdminMatic.databinding.FragmentUsageBinding

interface EditItemVendorCellClickListener {
    fun onEditItemVendorCellClickListener(data:Vendor)
    fun onEditItemVendorCheckListener(data:Vendor)
}

class EditItemVendorListFragment : Fragment(), EditItemVendorCellClickListener {

    private  var item: Item? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item")
        }
    }

    private var _binding: FragmentEditItemVendorListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditItemVendorListBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.vendors_for_x, item!!.name)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.vendorsRv.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = activity?.let {
                EditItemVendorsAdapter(
                    item!!.vendors!!.toMutableList(), myView.context, this@EditItemVendorListFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.vendorsRv.addItemDecoration(itemDecoration)

            (adapter as EditItemVendorsAdapter).notifyDataSetChanged()

        }

        binding.addVendorBtn.setOnClickListener {
            val directions = EditItemVendorListFragmentDirections.navigateToEditItemVendorFragment(item!!, null)
            myView.findNavController().navigate(directions)
        }



    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("editItemVendorListFragment")
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun onEditItemVendorCellClickListener(data: Vendor) {
        val directions = EditItemVendorListFragmentDirections.navigateToEditItemVendorFragment(item!!, data)
        myView.findNavController().navigate(directions)
    }

    override fun onEditItemVendorCheckListener(data: Vendor) {
        println("check listener")
        for (v in item!!.vendors!!) {
            if (v == data) {
                v.preferred = "1"
            }
            else {
                v.preferred = "0"
            }
        }
        binding.vendorsRv.adapter?.notifyDataSetChanged()
    }

}
