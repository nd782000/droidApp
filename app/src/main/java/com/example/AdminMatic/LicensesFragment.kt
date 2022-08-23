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
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentLicensesBinding


class LicensesFragment : Fragment() {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    private var _binding: FragmentLicensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLicensesBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_licenses, employee!!.fname)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (employee!!.licenses == null) {
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



    }




}
