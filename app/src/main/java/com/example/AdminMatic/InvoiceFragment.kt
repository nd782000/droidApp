package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentInvoiceBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class InvoiceFragment : Fragment(), StackDelegate {


    private var invoice: Invoice? = null
    private var invoiceID: String? = "0"

    private var dataLoaded = false

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var  stackFragment: StackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            invoice = it.getParcelable("invoice")
            invoiceID = it.getString("invoiceID")
        }

        if (invoice != null) {
            invoiceID = invoice!!.ID
        }
    }

    private var _binding: FragmentInvoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_invoice, container, false)
        _binding = FragmentInvoiceBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.invoice_header, invoiceID)
        setHasOptionsMenu(true)
        return myView
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //println("invoice = ${invoice!!.title}")

        stackFragment = StackFragment(3,invoiceID!!,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.invoice_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

        if (!dataLoaded) {
            setFragmentResultListener("sent") { _, bundle ->
                if (bundle.getBoolean("sent")) {

                    if (invoice!!.invoiceStatus == "2") {
                        val builder = android.app.AlertDialog.Builder(com.example.AdminMatic.myView.context)
                        builder.setTitle(getString(R.string.update_invoice_status_title))
                        builder.setMessage(R.string.invoice_emails_sent_update)
                        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            changeInvoiceEmailed()
                        }
                        builder.setNegativeButton(R.string.no) { _, _ ->
                        }
                        builder.show()
                    }
                    else {
                        globalVars.simpleAlert(myView.context, "", getString(R.string.invoice_emails_sent))
                    }
                }
            }

            getInvoice()
        }
        else {
            layoutViews()
        }


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.invoice_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.send_invoice_item) {

            when (invoice!!.invoiceStatus) {
                "0" -> { // Syncing
                    globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.cant_send_invoice_title), getString(R.string.cant_send_invoice_syncing_body))
                }
                "1" -> {
                    globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.cant_send_invoice_title), getString(R.string.cant_send_invoice_not_final_body))
                }
                "3" -> {
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.invoice_already_sent_title))
                    builder.setMessage(getString(R.string.invoice_already_sent_body))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (GlobalVars.permissions!!.invoicesEdit == "1") {
                            val directions = InvoiceFragmentDirections.navigateToSendEmail(invoice!!.customer, invoice!!.custName, SendEmailType.Invoice.ordinal, invoiceID!!)
                            myView.findNavController().navigate(directions)
                        }
                        else {
                            globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices_edit))
                        }
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }

                    builder.show()
                }
                "4" -> {
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.invoice_already_paid_title))
                    builder.setMessage(getString(R.string.invoice_already_paid_body))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (GlobalVars.permissions!!.invoicesEdit == "1") {
                            val directions = InvoiceFragmentDirections.navigateToSendEmail(invoice!!.customer, invoice!!.custName, SendEmailType.Invoice.ordinal, invoiceID!!)
                            myView.findNavController().navigate(directions)
                        }
                        else {
                            globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices_edit))
                        }
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }

                    builder.show()
                }
                "5" -> {
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.invoice_voided_title))
                    builder.setMessage(getString(R.string.invoice_voided_body))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (GlobalVars.permissions!!.invoicesEdit == "1") {
                            val directions = InvoiceFragmentDirections.navigateToSendEmail(invoice!!.customer, invoice!!.custName, SendEmailType.Invoice.ordinal, invoiceID!!)
                            myView.findNavController().navigate(directions)
                        }
                        else {
                            globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices_edit))
                        }
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }

                    builder.show()
                }
                else -> {
                    if (GlobalVars.permissions!!.invoicesEdit == "1") {
                        val directions = InvoiceFragmentDirections.navigateToSendEmail(invoice!!.customer, invoice!!.custName, SendEmailType.Invoice.ordinal, invoiceID!!)
                        myView.findNavController().navigate(directions)
                    }
                    else {
                        globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices_edit))
                    }
                }
            }

            return true
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("invoice")
    }

    private fun getInvoice(){
        println("getInvoice")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/invoice.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()

                        invoice = gson.fromJson(parentObject.toString(), Invoice::class.java)

                        layoutViews()
                    }

                    hideProgressView()

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["invoiceID"] = invoiceID!!
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "invoice"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun layoutViews() {
        binding.invoiceTitleValTv.text = invoice!!.title
        binding.invoiceDateValTv.text = invoice!!.invoiceDate
        binding.invoiceChargeValTv.text = invoice!!.charge

        when (invoice!!.charge) {
            "1" -> {
                binding.invoiceChargeValTv.text = getString(R.string.wo_charge_nc)
            }
            "2" -> {
                binding.invoiceChargeValTv.text = getString(R.string.wo_charge_fl)
            }
            "3" -> {
                binding.invoiceChargeValTv.text = getString(R.string.wo_charge_tm)
            }
        }

        binding.invoiceSalesRepValTv.text = invoice!!.salesRepName
        binding.invoiceSubtotalTv.text = getString(
            R.string.invoice_subtotal,
            GlobalVars.moneyFormatter.format(invoice!!.subTotal!!.toDouble())
        )
        binding.invoiceTaxTv.text = getString(
            R.string.invoice_sales_tax,
            GlobalVars.moneyFormatter.format(invoice!!.taxTotal!!.toDouble())
        )
        binding.invoiceTotalTv.text = getString(
            R.string.dollar_sign,
            GlobalVars.moneyFormatter.format(invoice!!.total.toDouble())
        )


        binding.invoiceItemRv.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = activity?.let {
                InvoiceItemsAdapter(
                    invoice!!.items!!.toMutableList(),
                    context
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(
                    myView.context,
                    DividerItemDecoration.VERTICAL
                )
            binding.invoiceItemRv.addItemDecoration(itemDecoration)
            //(adapter as WoItemsAdapter).notifyDataSetChanged()
        }



        binding.invoiceCustomerBtn.setOnClickListener {
            println("customer btn clicked")
            val customer = Customer(invoice!!.customer)
            val directions =
                InvoiceFragmentDirections.navigateInvoiceToCustomer(customer.ID)
            myView.findNavController().navigate(directions)
        }
        //customerBtn.text = "${workOrder!!.custName} ${workOrder!!.custAddress}"
        binding.invoiceCustomerBtn.text = getString(R.string.customer_button, invoice!!.custName, "")

        setInvoiceStatusIcons()

    }

    private fun setInvoiceStatusIcons() {
        when (invoice!!.invoiceStatus) {
            "0" -> {
                Picasso.with(context).load(R.drawable.ic_sync)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_sync)
                    .into(binding.invoiceStatusTagIv)
            }
            "1" -> {
                Picasso.with(context).load(R.drawable.ic_pending)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_pending)
                    .into(binding.invoiceStatusTagIv)
            }
            "2" -> {
                Picasso.with(context).load(R.drawable.ic_awarded)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_final)
                    .into(binding.invoiceStatusTagIv)
            }
            "3" -> {
                Picasso.with(context).load(R.drawable.ic_awarded)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_sent)
                    .into(binding.invoiceStatusTagIv)
            }
            "4" -> {
                Picasso.with(context).load(R.drawable.ic_done)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_paid)
                    .into(binding.invoiceStatusTagIv)
            }
            "5" -> {
                Picasso.with(context).load(R.drawable.ic_canceled)
                    .into(binding.invoiceStatusIv)
                Picasso.with(context).load(R.drawable.ic_tag_void)
                    .into(binding.invoiceStatusTagIv)
            }

        }
    }

    private fun changeInvoiceEmailed() {
        println("changeInvoiceEmailed")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/changeInvoiceEmailed.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        invoice!!.invoiceStatus = "3"
                        setInvoiceStatusIcons()
                    }

                    hideProgressView()

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["invoiceID"] = invoiceID!!
                params["emailed"] = "1"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "invoice"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }




    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        val directions = InvoiceFragmentDirections.navigateInvoiceToLead(_lead.ID)
        myView.findNavController().navigate(directions)

    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
        val directions = InvoiceFragmentDirections.navigateInvoiceToContract(_contract.ID)
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