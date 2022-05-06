package com.example.AdminMatic

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContractFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContractFragment : Fragment(), StackDelegate {
    // TODO: Rename and change types of parameters
    private var param2: String? = null

    private  var contract: Contract? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    private lateinit var customerBtn: Button
    private lateinit var titleTv: TextView
    private lateinit var chargeTv: TextView
    private lateinit var paymentTv: TextView
    private lateinit var salesRepTv: TextView
    private lateinit var notesTv: TextView

    private lateinit var  stackFragment: StackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable("contract")
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
        myView = inflater.inflate(R.layout.fragment_contract, container, false)

        globalVars = GlobalVars()

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract View")

        println("employee = ${contract!!.title}")


        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE

        stackFragment = StackFragment(1,contract!!.ID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.contract_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

        customerBtn = myView.findViewById(R.id.contract_customer_btn)
        customerBtn.text = getString(R.string.contract_customer_button_text, contract!!.custName, contract!!.addr)
        customerBtn.setOnClickListener{
            println("customer btn clicked")
            val customer = Customer(contract!!.customer!!)
            val directions = ContractFragmentDirections.navigateContractToCustomer(customer.ID)
            myView.findNavController().navigate(directions)

        }

        titleTv = myView.findViewById(R.id.contract_title_val_tv)
        chargeTv = myView.findViewById(R.id.contract_charge_val_tv)
        paymentTv = myView.findViewById(R.id.contract_payment_val_tv)
        salesRepTv = myView.findViewById(R.id.contract_sales_rep_val_tv)
        notesTv = myView.findViewById(R.id.contract_notes_val_tv)


        val chargeName:String
        when (contract!!.chargeType) {
            "1" -> {
                chargeName = getString(R.string.contract_charge_nc)
            }
            "2" -> {
                chargeName = getString(R.string.contract_charge_fl)
            }
            "3" -> {
                chargeName = getString(R.string.contract_charge_tm)
            }
            else -> {
                chargeName = ""
            }
        }

        titleTv.text = contract!!.title
        chargeTv.text = chargeName
        paymentTv.text = contract!!.paymentTermsID
        salesRepTv.text =contract!!.repName
        notesTv.text = contract!!.notes

    }

    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")


       // val directions = ContractFragmentDirections.navigateContractToLead(_lead)
      //  myView.findNavController().navigate(directions)

    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
       // val directions = ContractFragmentDirections.na(_lead)
       // myView.findNavController().navigate(directions)

        contract = _contract

    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")

        //val directions = ContractFragmentDirections.navigateContractToWorkOrder(_workOrder)
       // myView.findNavController().navigate(directions)

    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")

       // val directions = ContractFragmentDirections.navigateContractToInvoice(_invoice)
       // myView.findNavController().navigate(directions)
    }
}