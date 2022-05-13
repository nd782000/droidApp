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





class ContractItemFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param2: String? = null

    private  var contractItem: ContractItem? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar


    private lateinit var contractItemSearch: SearchView
    private lateinit var hideQtySwitch: Switch
    private lateinit var taxableSwitch: Switch
    private lateinit var chargeTv: TextView
    private lateinit var qtyTv: TextView
    private lateinit var priceTv: TextView
    private lateinit var totalTv: TextView
    private lateinit var recycler: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractItem = it.getParcelable("contractItem")
            param2 = it.getString(ARG_PARAM2)
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

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_item_number, contractItem!!.ID)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract Item View")


        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE

        /*
        titleTv = myView.findViewById(R.id.contract_title_val_tv)
        chargeTv = myView.findViewById(R.id.contract_charge_val_tv)
        paymentTv = myView.findViewById(R.id.contract_payment_val_tv)
        salesRepTv = myView.findViewById(R.id.contract_sales_rep_val_tv)
        notesTv = myView.findViewById(R.id.contract_notes_val_tv)
        priceTv = myView.findViewById(R.id.contract_price_tv)
        recycler = myView.findViewById(R.id.contract_item_rv)

         */


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

        //chargeTv.text = chargeName


        // get items
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }


}