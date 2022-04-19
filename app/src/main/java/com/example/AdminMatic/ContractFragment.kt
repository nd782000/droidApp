package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

        stackFragment = StackFragment(1,contract!!.ID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.contract_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()



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