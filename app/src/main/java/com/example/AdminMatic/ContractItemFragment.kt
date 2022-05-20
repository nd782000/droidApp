package com.example.AdminMatic

import android.opengl.Visibility
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_contract_list.*
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


interface ContractTaskCellClickListener {
    fun onContractTaskCellClickListener(data:ContractTask)
}


class ContractItemFragment : Fragment(), ContractTaskCellClickListener {
    private var addMode: Boolean? = null

    private  var contractItem: ContractItem? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar


    private lateinit var contractItemSearch: SearchView
    private lateinit var hideQtySwitch: Switch
    private lateinit var taxableSwitch: Switch
    private lateinit var chargeSpinner: Spinner
    private lateinit var qtyEt: EditText
    private lateinit var priceEt: EditText
    private lateinit var totalEt: EditText
    private lateinit var recycler: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractItem = it.getParcelable("contractItem")
            addMode = it.getBoolean("addMode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_contract, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_contract_item, container, false)

        globalVars = GlobalVars()

        if (addMode == true) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.add_item)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_item_number, contractItem!!.ID)
        }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract Item View")


        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE


        contractItemSearch = myView.findViewById(R.id.contract_item_search)
        hideQtySwitch = myView.findViewById(R.id.contract_item_hide_qty_switch)
        taxableSwitch = myView.findViewById(R.id.contract_item_taxable_switch)
        chargeSpinner = myView.findViewById(R.id.contract_item_charge_spinner)
        qtyEt = myView.findViewById(R.id.contract_item_qty_val_et)
        priceEt = myView.findViewById(R.id.contract_item_price_val_et)
        totalEt = myView.findViewById(R.id.contract_item_total_val_et)
        recycler = myView.findViewById(R.id.contract_item_tasks_rv)


        if (addMode == true) {
            recycler.visibility = View.GONE
        }
        else {
            contractItemSearch.isEnabled = false
            chargeSpinner.isEnabled = false
            qtyEt.isEnabled = false
            priceEt.isEnabled = false
            totalEt.isEnabled = false
            hideQtySwitch.isEnabled = false
            taxableSwitch.isEnabled = false


            if (contractItem!!.hideUnits == "1") {
                hideQtySwitch.isChecked = true
            }

            if (contractItem!!.taxType == "1") {
                taxableSwitch.isChecked = true
            }
            //contractItemSearch.setQuery(contractItem!!.name, false)

            recycler.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = activity?.let {
                    contractItem!!.tasks?.let { it1 ->
                        ContractTasksAdapter(
                            it1.toMutableList(),
                            context,
                            this@ContractItemFragment
                        )
                    }
                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                recycler.addItemDecoration(itemDecoration)
                (adapter as ContractTasksAdapter).notifyDataSetChanged()
            }

        }

        qtyEt.setText(contractItem!!.qty)
        priceEt.setText(contractItem!!.price)
        totalEt.setText(contractItem!!.total)




        val chargeName:String = when (contractItem!!.chargeType) {
            "1" -> {
                getString(R.string.contract_charge_nc)
            }
            "2" -> {
                getString(R.string.contract_charge_fl)
            }
            "3" -> {
                getString(R.string.contract_charge_tm)
            }
            else -> {
                ""
            }
        }

    }

    override fun onContractTaskCellClickListener(data:ContractTask) {
        println("Clicked on contract task #${data.ID}")
        //val directions = ContractFragmentDirections.navigateContractToContractItem(data)
        //myView.findNavController().navigate(directions)
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }


}