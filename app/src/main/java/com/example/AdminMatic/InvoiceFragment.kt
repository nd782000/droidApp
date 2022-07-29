package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


//TODO: Add "send invoice" functionality


class InvoiceFragment : Fragment(), StackDelegate {


    private  var invoice: Invoice? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar
    private lateinit var allCL: ConstraintLayout

    private lateinit var customerBtn: Button
    private lateinit var statusImageView: ImageView
    private lateinit var statusTagImageView: ImageView

    lateinit var titleTxt: TextView
    lateinit var dateTxt: TextView
    lateinit var chargeTypeTxt: TextView
    lateinit var salesRepTxt: TextView
    lateinit var itemsRecycler: RecyclerView
    lateinit var subtotalTxt: TextView
    lateinit var taxTxt: TextView
    lateinit var totalTxt: TextView


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
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.invoice_header, invoice!!.ID)
        setHasOptionsMenu(true)
        return myView
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        println("invoice = ${invoice!!.title}")


        pgsBar = view.findViewById(R.id.progress_bar)
        allCL = myView.findViewById(R.id.all_cl)
        stackFragment = StackFragment(3,invoice!!.ID,this)

        customerBtn = myView.findViewById(R.id.invoice_customer_btn)
        statusImageView = myView.findViewById(R.id.invoice_status_iv)
        statusTagImageView = myView.findViewById(R.id.invoice_status_tag_iv)

        titleTxt = view.findViewById(R.id.invoice_title_val_tv)
        dateTxt = view.findViewById(R.id.invoice_date_val_tv)
        chargeTypeTxt = view.findViewById(R.id.invoice_charge_val_tv)
        salesRepTxt = view.findViewById(R.id.invoice_sales_rep_val_tv)
        itemsRecycler = view.findViewById(R.id.invoice_item_rv)
        subtotalTxt = view.findViewById(R.id.invoice_subtotal_tv)
        taxTxt = view.findViewById(R.id.invoice_tax_tv)
        totalTxt = view.findViewById(R.id.invoice_total_tv)


        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.invoice_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

        getInvoice()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.invoice_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.send_invoice_item) {
            return true
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("invoice")
    }

    private fun getInvoice(){
        println("getWorkOrder")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/functions/get/invoice.php"
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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()

                    val savedStatus = invoice!!.invoiceStatus
                    invoice = gson.fromJson(parentObject.toString() , Invoice::class.java)
                    invoice!!.invoiceStatus = savedStatus



                    val itemJSON: JSONArray = parentObject.getJSONArray("items")
                    val itemList = gson.fromJson(itemJSON.toString() , Array<InvoiceItem>::class.java).toMutableList()
                    println("TASKJSON $itemJSON")
                    //println("Number of items: ${itemList.size}")



                    titleTxt.text = invoice!!.title
                    dateTxt.text = invoice!!.invoiceDate
                    chargeTypeTxt.text = invoice!!.charge

                    when (invoice!!.charge) {
                        "1" -> {
                            chargeTypeTxt.text = getString(R.string.wo_charge_nc)
                        }
                        "2" -> {
                            chargeTypeTxt.text = getString(R.string.wo_charge_fl)
                        }
                        "3" -> {
                            chargeTypeTxt.text = getString(R.string.wo_charge_tm)
                        }
                    }

                    when (invoice!!.invoiceStatus) {
                        "0" -> {
                            Picasso.with(context).load(R.drawable.ic_sync).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_sync).into(statusTagImageView)
                        }
                        "1" -> {
                            Picasso.with(context).load(R.drawable.ic_pending).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_pending).into(statusTagImageView)
                        }
                        "2" -> {
                            Picasso.with(context).load(R.drawable.ic_awarded).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_final).into(statusTagImageView)
                        }
                        "3" -> {
                            Picasso.with(context).load(R.drawable.ic_awarded).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_sent).into(statusTagImageView)
                        }
                        "4" -> {
                            Picasso.with(context).load(R.drawable.ic_done).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_paid).into(statusTagImageView)
                        }
                        "5" -> {
                            Picasso.with(context).load(R.drawable.ic_canceled).into(statusImageView)
                            Picasso.with(context).load(R.drawable.ic_tag_void).into(statusTagImageView)
                        }

                    }

                    salesRepTxt.text = invoice!!.salesRepName
                    subtotalTxt.text = getString(R.string.invoice_subtotal, GlobalVars.moneyFormatter.format(invoice!!.subTotal!!.toDouble()))
                    taxTxt.text = getString(R.string.invoice_sales_tax, GlobalVars.moneyFormatter.format(invoice!!.taxTotal!!.toDouble()))
                    totalTxt.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(invoice!!.total.toDouble()))


                    itemsRecycler.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            InvoiceItemsAdapter(
                                itemList,
                                context
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        itemsRecycler.addItemDecoration(itemDecoration)
                        //(adapter as WoItemsAdapter).notifyDataSetChanged()
                    }



                    customerBtn.setOnClickListener{
                        println("customer btn clicked")
                        val customer = Customer(invoice!!.customer)
                        val directions = InvoiceFragmentDirections.navigateInvoiceToCustomer(customer.ID)
                        myView.findNavController().navigate(directions)
                    }
                    //customerBtn.text = "${workOrder!!.custName} ${workOrder!!.custAddress}"
                    customerBtn.text = getString(R.string.customer_button, invoice!!.custName, "")


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
                params["invoiceID"] = invoice!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "invoice"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        allCL.visibility = View.VISIBLE
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        allCL.visibility = View.INVISIBLE
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