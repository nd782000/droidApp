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


class LicensesFragment : Fragment() {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_licenses, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_licenses, employee!!.fname)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (employee!!.licenses == null) {
            employee!!.licenses = emptyArray()
        }

        recyclerView = view.findViewById(R.id.licenses_recycler_view)
        val licenseList = employee!!.licenses!!.toMutableList()

        if (licenseList.size > 0) {

            recyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = activity?.let {
                    LicensesAdapter(
                        licenseList,
                        context
                    )
                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                recyclerView.addItemDecoration(itemDecoration)
                //(adapter as WoItemsAdapter).notifyDataSetChanged()
            }
        }
        else {
            val noLicensesTv : TextView = view.findViewById(R.id.licenses_no_licenses_tv)
            noLicensesTv.visibility = View.VISIBLE
        }



    }




}
