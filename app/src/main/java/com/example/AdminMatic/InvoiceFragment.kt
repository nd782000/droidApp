package com.example.AdminMatic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.AdminMatic.R


class InvoiceFragment : Fragment(), StackDelegate {


    private  var invoice: Invoice? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var  stackFragment: StackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            invoice = it.getParcelable("invoice")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_invoice, container, false)
        myView = inflater.inflate(R.layout.fragment_invoice, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.invoice)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("invoice = ${invoice!!.title}")


        pgsBar = view.findViewById(R.id.progress_bar)

        stackFragment = StackFragment(3,invoice!!.ID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.invoice_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

    }



    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        val directions = InvoiceFragmentDirections.navigateInvoiceToLead(_lead)
        myView.findNavController().navigate(directions)

    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
        val directions = InvoiceFragmentDirections.navigateInvoiceToContract(_contract)
        myView.findNavController().navigate(directions)
    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")
        val directions = InvoiceFragmentDirections.navigateInvoiceToWorkOrder(_workOrder)
        myView.findNavController().navigate(directions)
    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")
        invoice = _invoice
    }


}