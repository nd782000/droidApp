package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.AdminMatic.R
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.count
import kotlin.collections.mutableListOf
import kotlin.collections.set


interface  LogOut{
    fun logOut(view: View)
}

interface Callbacks{
    fun refreshWorkOrder()
    fun refreshImages()
    fun getImagesList():MutableList<Image>
    fun refreshWorkOrders()
    fun refreshLeads()
}

interface StackDelegate {
    //func displayAlert(_title:String)


    fun newLeadView(_lead:Lead)
    fun newContractView(_contract:Contract)
    fun newWorkOrderView(_workOrder:WorkOrder)
    fun newInvoiceView(_invoice:Invoice)

   // fun setLeadTasksWaiting(_leadTasksWaiting:String)
    //fun suggestNewContractFromLead()
    //fun suggestNewWorkOrderFromLead()
   // fun suggestNewWorkOrderFromContract()

}




//Data Classes


@Parcelize
data class Contact(
    var ID:String,
    var type: String,

    var sort: String? = "",

    var contactName: String? = "",
    var value: String? = "",
    var main: String? = "",
    var name: String? = "",
    var street1: String? = "",
    var street2: String? = "",
    var street3: String? = "",
    var street4: String? = "",
    var city: String? = "",
    var state: String? = "",
    var zip: String? = "",
    var zone: String? = "",
    var zoneName: String? = "",
    var color: String? = "",
    var lat: String? = "",
    var lng: String? = "",
    var fullAddress: String? = "",
    var preferred: String? = ""

): Parcelable{
    override fun toString(): String {
        return  ID
    }
}


@Parcelize
data class Contract(var ID: String = "0",
                    var status: String = "",
                    var title:String = "Contract",
                    var createdBy:String = "",
                    var statusName:String? = "",
                    var chargeType:String? = "",
                    var customer:String? = "",
                    var custName:String? = "",
                    var allowImages:String? = "",
                    var notes:String? = "",
                    var salesRep:String? = "",
                    var repName:String? = "",
                    var addr:String? = "",
                    var createDate:String? = "",
                    var subTotal:String? = "",
                    var taxTotal:String? = "",
                    var total:String? = "",
                    @field:SerializedName(value="paymentTermsID", alternate= ["paymentTerms", "terms"])
                    var paymentTermsID:String? = "",
                    var termsDescription:String? = "",
                    var daysAged:String? = "",
                    var repSignature:String? = "",
                    var customerSignature:String? = "",
                    var customerSigned:String? = "",
                    var repSignaturePath:String? = "",
                    var customerSignaturePath:String? = "",
                    var custNameAndID:String? = "",
                    var items: Array<ContractItem>? = null
): Parcelable{
    override fun toString(): String {
        return title
    }
}

@Parcelize
data class ContractTask(var ID:String,
                        var contractItemID: String,
                        var createdBy: String? = "",
                        var sort: String? = "",
                        var createDate: String? = "",
                        var taskDescription: String? = "",
                        var images: Array<Image>? = null

): Parcelable{
    override fun toString(): String {
        return  taskDescription ?: ID
    }
}


@Parcelize
data class ContractItem(var ID:String,
                        var name: String,
                        var chargeType: String,
                        var qty: String,
                        var contractID: String,
                        var itemID: String,
                        var tasks: Array<ContractTask>? = null,
                        var price: String? = "",
                        var createDate: String? = "",
                        var totalImages: String? = "",
                        var total: String? = "",
                        var type: String? = "",
                        var taxType: String? = "",
                        var subcontractor: String? = "",
                        var hideUnits: String? = "",
                        var contractTitle: String? = ""

): Parcelable{
    override fun toString(): String {
        return  name
    }
}


@Parcelize
data class Crew(var ID:String,
                var name: String,
                var status: String? = "",
                var color: String? = "",
                var crewHead: String? = "",
                var emps: Array<Employee>? = null
): Parcelable{
    override fun toString(): String {
        return  name
    }
}


data class EquipmentCrew(
                var name: String?,
                var color: String? = "",
                var equips: MutableList<Equipment> = mutableListOf()
)

data class SearchItem(
    var name: String = "",
    var index: Int = 0
)

@Parcelize
data class Customer(
    var ID: String,
    var sysname: String = "",
    var fullname: String = "",

    var parentID: String? = "",
    var parentName: String? = "",
    var fullName: String? = "",
    var mainAddr: String? = "",
    var balance: String? = "",
    var hear: String? = "",
    var active: String? = "",
    var fname: String? = "",
    var mname: String? = "",
    var lname: String? = "",
    var companyName: String? = "",
    var salutation: String? = "",
    var custNotes: String? = "",
    var servNotes: String? = "",
    var allowImages: String? = "",

    var propertySize: String? = "",
    var lawnSize: String? = "",
    var gardenSize: String? = "",
    var drivewaySize: String? = "",
    var floorSize: String? = "",

    var zone: String? = "",
    var sort: String? = "",

    var jobStreet1: String? = "",
    var jobStreet2: String? = "",
    var jobStreet3: String? = "",
    var jobStreet4: String? = "",
    var jobCity: String? = "",
    var jobZip: String? = "",
    var jobState: String? = "",

    var billStreet1: String? = "",
    var billStreet2: String? = "",
    var billStreet3: String? = "",
    var billStreet4: String? = "",
    var billCity: String? = "",
    var billZip: String? = "",
    var billState: String? = "",

    @field:SerializedName(value="phone", alternate= ["primaryPhone", "mainPhone"])
    var phone: String? = "",
    @field:SerializedName(value="email", alternate= ["primaryEmail", "mainEmail"])
    var email: String? = "",

    var lng: String? = "",
    var lat: String? = "",

    var contacts: Array<Contact> = arrayOf(),


    ): Parcelable{
    override fun toString(): String {
        return sysname
    }
}


@Parcelize
data class CustomerArray(var customers: Array<Customer>
): Parcelable{
    override fun toString(): String {
        return customers.count().toString()
    }
}

@Parcelize
data class VendorArray(var vendors: Array<Vendor>
): Parcelable{
    override fun toString(): String {
        return vendors.count().toString()
    }
}


@Parcelize
data class Department(var ID:String,
                      var name: String,
                      var status: String? = "",
                      var color: String? = "",
                      var depHead: String? = "",
                      var crews: Array<Crew>? = null,
                      var emps: Array<Employee>? = null

): Parcelable{
    override fun toString(): String {
        return  name
    }
}


@Parcelize
data class HearType(var ID:String,
                    var type: String,


                    ): Parcelable{
    override fun toString(): String {
        return type
    }
}



@Parcelize
data class Equipment(val ID: String,
                     val name: String,
                     var status:String,
                     val type:String,

                     val make:String?,
                     val model:String?,
                     val serial:String?,
                     val crew:String?,
                     val crewName:String?,
                     val typeName:String?,
                     val fuelType:String?,
                     val fuelTypeName:String?,
                     val engineType:String?,
                     val engineTypeName:String?,
                     val mileage:String?,
                     @field:SerializedName(value="dealer", alternate= ["vendorID"])
                     val dealer:String?,
                     val dealerName:String?,
                     val purchaseDate:String?,
                     val description:String?,
                     val pic:String?,
                     val crewColor:String?,
                     val plannerShow:String?,
                     val image:Image?
): Parcelable{
    override fun toString(): String {
        return name
    }
}





@Parcelize

data class Employee(val ID: String,
                    val name: String,
                    @field:SerializedName(value="fName", alternate= ["fname"])
                    val fname: String,
                    @field:SerializedName("lName", alternate= ["lname"])
                    val lname:String,
                    val pic:String,
                    val username: String,
                    val level: String,
                    val levelName: String,
                    val hasSignature: String,
                    val payRate: String,
                    val phone: String,
                    val mobile: String,
                    val email: String,
                    val appScore: String,
                    val dep: String,
                    val salesRep: String,
                    var sessionKey: String,
                    var companyUnique:String,
                    var licenses:Array<License>? = null
) : Parcelable{


    override fun toString(): String {
        return name
    }
}




@Parcelize
data class EquipmentService(
    var ID:String,
    var name: String,
    var type: String,

    var typeName: String? = "",
    var addedBy: String? = "",
    var status: String? = "",
    var equipmentID: String? = "",
    var frequency: String? = "",
    var instruction: String? = "",
    var createDate: String? = "",
    var completionDate: String? = "",
    var completionMileage: String? = "",
    var completedBy: String? = "",
    var completionNotes: String? = "",
    var currentValue: String? = "",
    var nextValue: String? = "",
    var serviceDue: Boolean? = false

): Parcelable{
    override fun toString(): String {
        return  name
    }
}




@Parcelize
data class InvoiceItem(var ID:String,
                       var item: String,
                       var charge: String? = "",
                       var act: String? = "",
                       var invoiceID: String? = "",
                       var price: String? = "",
                       var servicedDate: String? = "",
                       var itemID: String? = "",
                       var totalImages: String? = "",
                       var total: String? = "",
                       var type: String? = "",
                       var taxCode: String? = "",
                       var hideUnits: String? = "",
                       var custDesc: String? = ""


): Parcelable{
    override fun toString(): String {
        return  item
    }
}

@Parcelize
data class InspectionQuestion(
    /*
    var ID:String,
    var name: String,
    var answer: String? = ""

     */
    var questionID:String,
    var answer:String,
    var questionText:String

): Parcelable{
    override fun toString(): String {
        return  questionText
    }
}


@Parcelize
data class Image(val ID: String,
                 val name: String,
                 val fileName:String,
                 val width:String,
                 val height:String,

                 val description:String?,
                 val customer:String?,
                 val customerName:String?,
                 val woID:String?,
                 val album:String?,
                 val leadTaskID:String?,
                 val contractTaskID:String?,
                 val taskID:String?,
                 val equipmentID:String?,
                 val usageID:String?,
                 val vendorID:String?,
                 val strikeID:String?,
                 val dateAdded:String?,
                 val createdByName:String?,
                 val type:String?,
                 val tags:String?,
                 val index:String?,
                 var liked:String?,
                 var likes:String?
): Parcelable{
    override fun toString(): String {
        return customerName ?: ID
    }
}

@Parcelize
data class Invoice(var ID: String = "0",
                   var invoiceDate: String = "",
                   var total:String = "",
                   var invoiceStatus:String = "",
                   var customer:String = "",
                   var custName:String = "",

                   var title:String? = "Invoice",
                   var charge:String? = "",
                   var salesRepName:String? = "",
                   var notes:String? = "",
                   var subTotal:String? = "",
                   var taxTotal:String? = ""
): Parcelable{
    override fun toString(): String {
        return title ?: ID
    }
}



@Parcelize
data class Item(
    var ID: String,
    val name: String,
    val typeID:String?,
    val type:String?,
    val remQty:String?,
    val price:String?,
    val unit:String?,
    val salesDescription:String?,
    val tax:String?,
    var vendors: Array<Vendor>? = null,
    var workOrders: Array<WorkOrder>? = null
): Parcelable{
    override fun toString(): String {
        return name
    }
}






@Parcelize
data class Lead(var ID: String = "0",
                var statusID: String = "",
                var timeType:String = "",
                var createdBy:String = "",
                var statusName:String? = "",
                var customer:String? = "",
                var custName:String? = "",
                var allowImages:String? = "",
                var custTax:String? = "",
                var custTerms:String? = "",
                var zone:String? = "",
                var address:String? = "",
                var date:String? = "",
                var aptDate:String? = "",
                var dateNice:String? = "",
                var time:String? = "",
                var salesRep:String? = "",
                var repName:String? = "",
                var requestedByCust:String? = "",
                var urgent:String? = "",
                var description:String? = "Lead",
                var deadline:String? = "",
                var deadlineNice:String? = "",
                var daysAged:String? = "",
                var custNameAndID:String? = "",
                var custNameAndZone:String? = "",
                var lng:Double? = 0.00,
                var lat:Double? = 0.00,

                var tasks:Array<Task>? = null
): Parcelable{
    override fun toString(): String {
        return description ?: ID
    }
}



@Parcelize
data class License(
    var ID:String,
    var licenceID: String,
    var name: String,
    var expiration: String? = "",
    var number: String? = "",
    var status: String? = "",
    var issuer: String? = ""

): Parcelable{
    override fun toString(): String {
        return  name
    }
}



@Parcelize
data class Login(var attempt:String,
                 var lastAttempt: String?

): Parcelable






@Parcelize
data class Payroll(var ID: String?,
                   var startTime: String?,
                   var stopTime: String?,
                   var startTimeDate: Date?,
                   var stopTimeDate: Date?,
                   var startTimeShort: String?,
                   var stopTimeShort: String?,
                   var lunch: String?,
                   var date: Date?,
                   var shortDate: String?,
                   var total: String?,
                   var verified: String?,
                   var appCreatedBy: String?,
                   var createdBy: String?,
                   var notes: String?,
                   var status: String?,
                   var dayType: String?,
                   var dayTypeName: String?,
                   var overTime:String?,
                   var dayLong:String?,
                   var dayShort:String?

): Parcelable{
    override fun toString(): String {
        return ID ?: "NULL"
    }
}


@Parcelize
data class PayrollArray(var combinedTotal: String?,
        var regTotal: String?,
        var otTotal: String?,
        var regPay: String?,
        var otPay: String?,
        var totalPay: String?,
        var totalShifts: String?,
        var pending: String?,
        var payroll: Array<Payroll>? = null

): Parcelable{
    override fun toString(): String {
        return ID ?: "NULL"
    }
}



@Parcelize
data class Signature(
    var contractId:String,
    var type: String,
    var path: String
): Parcelable

@Parcelize
data class Shift(
    var ID:String,
    var empID: String,

    var startTime: String? = "",
    var endTime: String? = "",
    var status: String? = "",
    var comment: String? = "",
    var shiftQty: String? = ""

): Parcelable{
    override fun toString(): String {
        return  ID
    }
}



@Parcelize
data class Strike(
    var ID:String,
    var empID: String,

    var empName: String? = "",
    var hasSignature: String? = "",
    var type: String? = "",
    var note: String? = "",
    var woID: String? = "",
    var woTitle: String? = "",
    var customerID: String? = "",
    var custName: String? = "",
    var created: String? = "",
    var issuedByID: String? = "",
    var occurenceDate: String? = "",
    var occurenceDateNice: String? = "",
    var signed: String? = "",
    var signedDate: String? = "",
    var signedDateNice: String? = "",

    var images: Array<Image>? = null

): Parcelable{
    override fun toString(): String {
        return  empName ?: ID
    }
}




@Parcelize
data class Task(var ID:String,
                var sort: String,
                var status: String,

                var task: String? = "",

                var images: Array<Image>? = null

): Parcelable{
    override fun toString(): String {
        return task ?: ID
    }
}



@Parcelize
data class Template(var ID:String,
                var name: String,
                var description: String? = ""
): Parcelable{
    override fun toString(): String {
        return  name
    }
}






@Parcelize
data class Usage(var ID:String,
                 var woID: String,
                 var woItemID: String,
                 var type: String,
                 var addedBy: String,
                 var qty: String,

                 var empID: String? = "",
                 var depID: String? = "",
                 var start: String? = null,
                 var stop: String? = null,

                 var lunch: String? = null,
                 var empName: String? = "",
                 var unitPrice: String? = "",
                 var totalPrice: String? = "",
                 var vendor: String? = "",
                 var unitCost: String? = "",
                 var totalCost: String? = "",
                 var usageCharge: String? = "",
                 var override: String? = "",
                 var pic: String? = "",
                 var del: String? = "",
                 var custName: String? = "",
                 var woStatus: String? = "",
                 var hasReceipt: String? = "",


                 var locked: Boolean? = false,
                 var receipt:Image? = null
): Parcelable{
    override fun toString(): String {
        return  ID
    }


     fun getTime(s: String): String {

         val formatter =
             DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
         val dateTime = LocalDateTime.parse(s, formatter)

         val dtf: DateTimeFormatter =
             DateTimeFormatterBuilder().appendPattern("h:mm a").toFormatter()


        return dtf.format(dateTime.toLocalTime()).toString()

     }

}



@Parcelize
data class Vendor(val ID: String,
                  val name: String,
                  val mainAddr:String?,
                  var lng:String?,
                  var lat:String?,
                  val website:String?,
                  val mainPhone:String?,
                  var balance:String?,
                  val cost:String?,
                  val price:String?,
                  val prefered:String?,
                  val itemString:String?
): Parcelable{
    override fun toString(): String {
        return name
    }
}




@Parcelize
data class WoItem(var ID:String,
                  var item: String,
                  var itemID: String,

                  var type: String = "",
                  var sort: String = "",
                  var status: String = "",
                  var charge: String = "",
                  var total: String = "",
                  var est: String = "",
                  var empDesc: String = "",
                  var chargeName: String = "",
                  var act: String = "",
                  var price: String = "",
                  var totalCost: String = "",
                  var usageQty: String = "",
                  var remaining: String = "",
                  var extraUsage: String = "",
                  var unitName: String = "",
                  var woID: String = "",
                  var woTitle: String = "",
                  var contractID: String = "",
                  var contractTitle: String = "",
                  var taxType: String = "",
                  var hideUnits: String = "",
                  var subcontractor: String = "",
                  var locked: String = "",
                  var printView: String = "",

                  var tasks:Array<Task> = arrayOf(),
                  var usage:Array<Usage> = arrayOf(),
                  var vendors:Array<Vendor> = arrayOf()
                          //usage
                          //vendors



): Parcelable{
    override fun toString(): String {
        return item
    }
}


@Parcelize
data class WorkOrder(var woID: String = "0",
                     var status: String = "",
                     var title:String = "Work Order",
                     var progress:String = "",
                     var totalPrice:String = "",
                     var totalCost:String = "",
                     var totalPriceRaw:String = "",
                     var totalCostRaw:String = "",
                     var profitAmount:String = "",
                     var profit:String = "",

                     var statusName:String? = "",
                     var customer:String? = "",
                     var custName:String? = "",
                     var custAddress:String? = "",
                     var allowImages:String? = "",
                     var date:String? = "",
                     var dateNice:String? = "",
                     var salesRep:String? = "",
                     var salesRepName:String? = "",
                     var notes:String? = "",
                     var charge:String? = "",
                     var chargeName:String? = "",
                     @field:SerializedName(value="invoiceType", alternate= ["invoice"])
                     var invoiceType:String? = "",
                     var nextPlannedDate:String? = "",
                     var locked:String? = "",
                     @field:SerializedName(value="department", alternate= ["departmentID"])
                     var department:String? = "",
                     var crew:String? = "",
                     var crewName:String? = "",
                     var mainCrew:String? = "",
                     var remQty:String? = "",
                     var daySort:String? = "",
                     var lng:String? = "",
                     var lat:String? = "",
                     var urgent:String? = "",
                     var skipped:String? = "",

                     var items:Array<WoItem>? = null,
                     var lead:Lead? = null,
                     var contract:Contract? = null,

                     var crews:Array<Crew>? = null,
                    var emps:MutableList<Employee> = mutableListOf()

                     ) : Parcelable {

    override fun toString(): String {
        return title
    }

    fun setEmps(){
        println("setEmps")
        if (this.crews != null){
            for (crew in this.crews!!){
            print("crew")
            for (emp in crew.emps!!){
                print("emp name = ${emp.name}")
                this.emps.add(emp)
                }
            }
        }else{
            print("crews not set")
        }
    }
}

@Parcelize
data class Permissions(
    var leads: String? = "",
    var contracts: String? = "",
    var schedule: String? = "",
    var invoices: String? = "",
    var customers: String? = "",
    var employees: String? = "",
    var items: String? = "",
    var vendors: String? = "",
    var equipment: String? = "",
    var files: String? = "",
    var reports: String? = "",
    var settings: String? = "",
    var accounting: String? = "",
    var emails: String? = "",
    var terms: String? = "",
    var templates: String? = "",
    var zones: String? = "",
    var planner: String? = "",
    var home: String? = "",
    var payroll: String? = "",
    var crews: String? = "",
    var leadsEdit: String? = "",
    var contractsEdit: String? = "",
    var scheduleEdit: String? = "",
    var scheduleMoney: String? = "",
    var invoicesEdit: String? = "",
    var customersEdit: String? = "",
    var customersMoney: String? = "",
    var employeesEdit: String? = "",
    var payrollEdit: String? = "",
    var crewsEdit: String? = "",
    var itemsEdit: String? = "",
    var itemsMoney: String? = "",
    var vendorsEdit: String? = "",
    var vendorsMoney: String? = "",
    var equipmentEdit: String? = "",
    var filesEdit: String? = "",
    var usageApp: String? = "",
    var payrollApp: String? = "",
    var settingsEdit: String? = "",
    var accountingEdit: String? = "",
    var emailsEdit: String? = "",
    var templatesEdit: String? = "",
    var termsEdit: String? = "",
    var zonesEdit: String? = "",

    ) : Parcelable {

        override fun toString(): String {
        return "Permissions"
        }
}


@Parcelize
data class PaymentTerms(
    var ID: String,
    @field:SerializedName(value="name", alternate= ["Name"])
    var name: String,

    ) : Parcelable {

        override fun toString(): String {
            return "name"
        }
}

@Parcelize
data class ContactType(
    var ID: String,
    var name: String,

    ) : Parcelable {

    override fun toString(): String {
        return "name"
    }
}

@Parcelize
data class Zone(
    var ID: String,
    var name: String = "Name",

    ) : Parcelable {

    override fun toString(): String {
        return "name"
    }
}



//Extensions

fun Spinner.setSpinnerText(text: String) {
    for (i in 0 until this.adapter.count) {
        if (this.adapter.getItem(i).toString().contains(text)) {
            this.setSelection(i)
        }
    }
}


/*
class SpinnerInteractionListener : AdapterView.OnItemSelectedListener, View.OnTouchListener {
    var userSelect = false
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        userSelect = true
        return false
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        pos: Int,
        id: Long
    ) {
        if (userSelect) {
            // Your selection handling code here
            userSelect = false
        }
    }
}

*/


fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}


class VolleyRequestQueue constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleyRequestQueue? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleyRequestQueue(context).also {
                    INSTANCE = it
                }
            }
    }
    /*
    val imageLoader: ImageLoader by lazy {
        ImageLoader(requestQueue,
            object : ImageLoader.ImageCache {
                private val cache = LruCache<String, Bitmap>(20)
                override fun getBitmap(url: String): Bitmap {
                    return cache.get(url)
                }
                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            })
    }

     */
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}



class MainActivity : AppCompatActivity(), LogOut, Callbacks {

    lateinit var  pgsBar: ProgressBar
    private var workOrderListFragment: WorkOrderListFragment? = null
    private var leadListFragment: LeadListFragment? = null
    private var mapFragment: MapFragment? = null
     private var imageListFragment: ImageListFragment? = null
    //lateinit var  hostFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GlobalVarExample)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pgsBar = this.findViewById(R.id.progressBar)
        hideProgressView()

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setCustomView(R.layout.abs_layout)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        println("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu, menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == android.R.id.home) {
            back()
            return true
        }

        if (id == R.id.home_item) {
            home()
            return true
        }
        if (id == R.id.logout_item) {
            logOut(myView)
            return true
        }

        return super.onOptionsItemSelected(item)

    }

    fun back(){
        println("back")
        // This allows back button checks like "Are you sure you want to exit without submitting" to work on the menu bar back as well
        //println(getVisibleFragment()!!.lay)

        super.onBackPressed()
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navController = navHostFragment.navController
        //navController.navigateUp()
    }

    fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) return fragment
        }
        return null
    }

    private fun home(){
        println("home")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController


        navController.popBackStack(R.id.mainMenuFragment2, false)
    }

    // Used to close software keyboard when enter button is pressed on edit texts
    internal class DoneOnEditorActionListener : OnEditorActionListener {
        override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                v.clearFocus()
                return true
            }
            return false
        }
    }


    override fun logOut(view: View) {

        println("log out on main")


        val sharedPref = this.getSharedPreferences("pref", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {

            putString("loggedInEmpID","")
            putString("sessionKey","")
            putString("companyUnique","")
            apply()
        }

        if (GlobalVars.loggedInEmployee != null){
            showProgressView()


            var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/other/logout.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"
            // val queue = Volley.newRequestQueue(myView.context)
            val queue = Volley.newRequestQueue(view.rootView.context)

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response
                    //Log.d("Response", response)

                    println("Response $response")

                    try {

                        hideProgressView()
                        //val navController = Navigation.findNavController(view)

                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                        val navController = navHostFragment.navController

                        navController.popBackStack(R.id.logInFragment, false)

                        // Clear existing data
                        if (GlobalVars.globalWorkOrdersList != null) {
                            GlobalVars.globalWorkOrdersList!!.clear()
                        }
                        if (GlobalVars.globalLeadList != null) {
                            GlobalVars.globalLeadList!!.clear()
                        }
                        if (GlobalVars.customerList != null) {
                            GlobalVars.customerList!!.clear()
                        }

                        GlobalVars.loggedInEmployee = null
                        GlobalVars.permissions = null
                        GlobalVars.employeeList = null

                        /* Here 'response' is a String containing the response you received from the website... */
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

                    params["deviceID"] = GlobalVars.deviceID!!

                    println("params = $params")
                    return params
                }
            }
            queue.add(postRequest1)
        }else{

            val navController = Navigation.findNavController(view)
            navController.popBackStack(R.id.logInFragment, false)
        }




    }










    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }




    //callback methods


    override fun refreshLeads() {
        println("refreshWorkLeads")

        if(leadListFragment != null){
            println("fragments not null ")
            leadListFragment!!.getLeads()
        }
    }

    fun setWorkOrderList(_workOrderListFragment:WorkOrderListFragment?){
        println("setWorkOrderList")
        this.workOrderListFragment = _workOrderListFragment!!
    }

    fun setLeadList(_leadListFragment:LeadListFragment?){
        println("setWorkOrderList")
        this.leadListFragment = _leadListFragment!!
    }

    fun setMap(_mapFragment:MapFragment?){
        println("setMap")
        this.mapFragment = _mapFragment!!
    }

    fun setImageList(_imageListFragment:ImageListFragment?){
        println("setImageList")
        this.imageListFragment = _imageListFragment!!
    }


    fun updateMap(){
        println("updateMap")
        if(mapFragment != null){
            mapFragment!!.updateMap()
        }
    }

    override fun refreshWorkOrders() {
        println("refreshWorkOrders")

        if(workOrderListFragment != null){
            println("fragments not null ")
            workOrderListFragment!!.getWorkOrders()
        }
    }



    override fun refreshWorkOrder() {
        println("refreshWorkOrder")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
       // navHostFragment!!.childFragmentManager.fragments[0]

        val workOrderFragment = navHostFragment.childFragmentManager.fragments[0] as WorkOrderFragment
        //workOrderFragment.test()

       // var navController = Navigation.findNavController(view)
        //var workOrderFragment = navController
        //navController.popBackStack(R.id.logInFragment, false)
    }

    override fun getImagesList():MutableList<Image> {
        if(imageListFragment != null){
            println("fragments not null ")
            return imageListFragment!!.getImagesList()
        }
        return mutableListOf()
    }

    override fun refreshImages() {
        println("refresh images")

      //  val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navHostFragment: NavHostFragment? =
       // supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        //navHostFragment

        //Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container)
       // val fragments1 = supportFragmentManager.fragments[0].childFragmentManager.fragments


        //println("fragments1 = $fragments1")

        //supportFragmentManager.fragments[0].childFragmentManager.popBackStack()

       // val fragments2 = supportFragmentManager.fragments[0].childFragmentManager.fragments


        //println("fragments2 = $fragments2")

        //fragmentDemo.doSomething("some param")

         // val imageListFragment: ImageListFragment? = supportFragmentManager.findFragmentById(R.id.imageListFragment) as ImageListFragment?

       // val imageListFragment: ImageListFragment = supportFragmentManager.fragments[0].childFragmentManager.fragments[0] as ImageListFragment
        //firstFragment.MyMethod()

        if(imageListFragment != null){
            println("fragment's not null ")
            imageListFragment!!.refreshImages()
        }



        //navHostFragment.navController.popBackStack()
        //val imageListFragment = navHostFragment!!..fragments[0] as ImageListFragment
        //imageListFragment.getImages()

       // val imageUploadFragment = navHostFragment!!.childFragmentManager.fragments[0] as ImageUploadFragment
       // navHostFragment.navController.
        //imageListFragment.getImages()

        //val imageListFragment = navHostFragment!!.childFragmentManager.findFragmentById(R.id.imageListFragment) as ImageListFragment
        //imageListFragment.getImages()


       // val fm: FragmentManager = supportFragmentManager

//if you added fragment via layout xml

//if you added fragment via layout xml
       // val fragment: ImageListFragment =
           // fm.findFragmentById(R.id.imageListFragment) as ImageListFragment

       // val fragment: ImageListFragment? =

            //supportFragmentManager.findFragmentById(imageListFragment) as ImageListFragment?
        //fragment.specific_function_name()
        //fragment!!.getImages()


        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navHostFragment: NavHostFragment? =
       // supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        // navHostFragment!!.childFragmentManager.fragments[0]

        //val imageListFragment = navHostFragment!!.childFragmentManager.fragments[1] as ImageListFragment
        //val imageListFragment = navHostFragment!!.parentFragment as ImageListFragment
        //imageListFragment.getImages()
    }


    /*
    override fun onBackPressed() {

        println("onBackPressed")
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }
    */
}