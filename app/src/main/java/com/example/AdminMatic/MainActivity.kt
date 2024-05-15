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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import org.json.JSONException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
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
                    var title:String = "",
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
                    var subTotal:String? = "0",
                    var taxTotal:String? = "0",
                    var total:String? = "0",
                    var salesTax:String? = "",
                    var custTax:String? = "",
                    var custTerms:String? = "",
                    var lead:Lead? = null,
                    var contractDate:String? = "",
                    var depositType:String? = "",
                    @field:SerializedName(value="paymentTermsID", alternate= ["paymentTerms", "terms"])
                    var paymentTermsID:String? = "",
                    var termsDescription:String? = "",
                    var daysAged:String? = "0",
                    var repSignature:String? = "",
                    //var customerSignature:String? = "",
                    var customerSigned:String? = "",
                    var repSignaturePath:String? = "",
                    var customerSignaturePath:String? = "",
                    var custNameAndID:String? = "",
                    var items: Array<ContractItem>? = null,
                    var sugDepartment:String? = "",
                    var sugCrew:String? = "",
                    var sugInvoice:String? = "",
                    var sugRenew:String? = "",
                    var sugDeadline:String? = "",
                    var recSettings:ContractRecSettings? = null

                    ): Parcelable{
    override fun toString(): String {
        return title
    }
}

@Parcelize
data class ContractRecSettings(var contractID:String,
                               var startDate:String? = "",
                               var endDate:String? = "",
                               var frequency:String? = "",
                               var minDays:String? = "",
                               var prefDay:String? = "",
                               var fixedDate:String? = "",
                               var renew_prompt:String? = "0"

): Parcelable{
    override fun toString(): String {
        return ""
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
                @field:SerializedName(value="color", alternate= ["depColor"])
                var color: String? = "",
                var dep: String? = "",
                var depName: String? = "",
                var subcolor: String? = "",
                var crewHead: String? = "",
                var emps: Array<Employee>? = null,
                var equipment: Array<Equipment>? = null,

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

    var taxID: String? = "",
    var termsID: String? = "",

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
data class Document(

    var ID: String,
    var name: String,
    var fname: String,
    var fsize: String,
    var custID: String? = "0",
    var custName: String? = "",
    var dateAdded: String = "",
    var empName: String? = "",
    var type: String = ""

    ): Parcelable{
    override fun toString(): String {
        return name
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
                      var emps: Array<Employee>? = null,
                      var active: String = "1"

): Parcelable{
    override fun toString(): String {
        return  name
    }
}


@Parcelize
data class HearType(
    var ID: String,
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
                     var type:String,
                     var active:String,

                     val make:String? = "",
                     val model:String? = "",
                     val serial:String? = "",
                     var crew:String? = "",
                     var crewName:String? = "",
                     var typeName:String? = "",
                     var fuelType:String? = "",
                     var fuelTypeName:String? = "",
                     var engineType:String? = "",
                     var engineTypeName:String? = "",
                     //val mileage:String? = "",
                     var usage:String? = "0",
                     @field:SerializedName(value="dealer", alternate= ["vendorID"])
                     var dealer:String? = "",
                     var dealerName:String? = "",
                     var purchaseDate:String? = "",
                     var purchasePrice:String? = "",
                     var weight:String? = "",
                     val description:String? = "",
                     val pic:String? = "",
                     val picURL:String? = "",
                     var crewColor:String? = "",
                     var plannerShow:String? = "",
                     var usageType: String? = "",
                     //var usageTypeID: Int = 0,
                     val image:Image? = null
): Parcelable{
    override fun toString(): String {
        return name
    }
}





@Parcelize

data class Employee(val ID: String,
                    val name: String,
                    val salutation: String?,
                    @field:SerializedName(value="fName", alternate= ["fname"])
                    val fname: String?,
                    @field:SerializedName("lName", alternate= ["lname"])
                    val lname:String?,
                    val middleName:String?,
                    val dob:String?,
                    val pic:String?,
                    val username: String,
                    val level: String?,
                    val levelName: String?,
                    val hasSignature: String?,
                    val payRate: String?,
                    val phone: String?,
                    val mobile: String?,
                    val email: String?,
                    val appScore: String?,
                    val address: String?,
                    val address2: String?,
                    val address3: String?,
                    val address4: String?,
                    var crewName: String?,
                    var crewColor: String?,
                    val city: String?,
                    val state: String?,
                    val zip: String?,
                    val depID: String?,
                    val lang: String?,
                    val active: String,
                    val salesRep: String?,
                    val payType: String?,
                    val taxStatus: String?,
                    @field:SerializedName("dependents", alternate= ["dependants"])
                    val dependents: String?,
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
    var addedByName: String = "",
    var status: String? = "",
    var equipmentID: String? = "",
    var equipmentName: String? = "",
    var frequency: String? = "",
    var instructions: String? = "",
    var createDate: String? = "",
    var completionDate: String? = "",
    var completionMileage: String? = "",
    var completedBy: String? = "",
    var completedByName: String? = "",
    var completionNotes: String? = "",
    var warningOffset: String? = "",
    @field:SerializedName("currentValue", alternate= ["completeValue"])
    var currentValue: String? = "",
    var targetDate: String? = "",
    var nextValue: String? = "",
    var nextDate: String? = "",
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
data class EquipmentField(
    @field:SerializedName("name", alternate= ["questionText"])
    var name:String,
    var ID:String,
    var sort:String = "0"
): Parcelable{
    override fun toString(): String {
        return  name
    }
}


@Parcelize
data class Image(val ID: String,
                 val name: String,
                 val fileName:String,
                 val width:String,
                 val height:String,

                 var description:String?,
                 var customer:String?,
                 var customerName:String?,
                 val woID:String?,
                 val album:String?,
                 val leadTaskID:String?,
                 val contractTaskID:String?,
                 var taskID:String?,
                 var equipmentID:String?,
                 val usageID:String?,
                 @field:SerializedName("vendorID", alternate= ["vendor"])
                 var vendorID:String?,
                 val strikeID:String?,
                 val dateAdded:String?,
                 val createdBy:String?,
                 val createdByName:String?,
                 var noCompress:String?,
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
data class Tag(
    val name: String,
): Parcelable{
    override fun toString(): String {
        return name
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
                   var taxTotal:String? = "",
                   var items:Array<InvoiceItem>? = null
): Parcelable{
    override fun toString(): String {
        return title ?: ID
    }
}



@Parcelize
data class Item(
    var ID: String,
    @field:SerializedName("name", alternate= ["item"])
    var name: String,
    @field:SerializedName("fullName", alternate= ["fullname"])
    var fullName: String,
    var typeName:String?,
    var unitName: String? = "",
    var type:String? = "",
    val remQty:String? = "",
    var price:String? = "",
    var unit:String? = "",
    var salesDescription:String? = "",
    var purchaseDescription:String? = "",
    var estNotes:String? = "",
    @field:SerializedName("taxable", alternate= ["tax"])
    var taxable:String? = "0",
    var salesTaxID:String? = "",
    var subcontractor: String = "0",
    var parentID: String? = "0",
    var cost: String? = "",
    var partNum: String? = "",
    var purchaseUnit: String? = "",
    var purchaseUnitName: String? = "",
    var estUnit: String? = "",
    var estUnitName: String? = "",
    var minQty: String? = "",
    var reorderMin: String? = "",
    var reorderMax: String? = "",
    var onHand: String? = "",
    var asOf: String? = "",
    var depID: String? = "",
    var itemTerms: String? = "",
    var totalValue: String? = "",
    var active: String = "1",
    var synced: String = "0",
    var vendors: Array<Vendor>? = null,
    var workOrders: Array<WorkOrder>? = null
): Parcelable{
    override fun toString(): String {
        return name
    }
}

@Parcelize
data class UnitType(
    var unitID: String,
    val unitName: String,
    val unitShort: String,
): Parcelable{
    override fun toString(): String {
        return unitName
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
data class PlannedDate(var workOrderID: String?,
                       var leadID: String?,
                       var serviceID: String?,
                       var crewID: String?,
                       var plannedDate: String?,
                       var firm: String?,
                       var startTime: String?,
                       var endTime: String?,
                       var daySort: String?,
                       var additional: String?

): Parcelable{
    override fun toString(): String {
        return plannedDate ?: "NULL"
    }
}

@Parcelize
data class EmailTemplate(
    var title: String?,
    var subject: String?,
    var emailContent: String?,
    var portal: String?,
    var defaultVal: String?
): Parcelable{
    override fun toString(): String {
        return title ?: "NULL"
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
                var taskTranslated: String?,

                var images: Array<Image>? = null

): Parcelable{
    override fun toString(): String {
        return task ?: ID
    }
}



@Parcelize
data class Template(var ID:String,
                    var departmentID: String? = "",
                    var name: String,
                    var description: String? = "",
                    var active: String? = "",
                    var parentID: String? = "",
                    var createDate: String? = "",
                    var createdBy: String? = "",
                    var customTerms: String? = "",
                    var chargeType: String? = "",
                    var paymentTerms: String? = "",
                    var depositType: String? = "",
                    var invoiceType: String? = "",
                    var mainCrew: String? = "",
                    var renew: String? = "",
                    var depName: String? = "",
                    var recSettings: ContractRecSettings? = null
): Parcelable{
    override fun toString(): String {
        return  name
    }
}


@Parcelize
data class DefaultFields(var recRangeMS:String,
                         var recRangeDS:String,
                         var recRangeME:String,
                         var recRangeDE:String,
                         var recFreq:String,
                         var recMin:String,
                         var recPref:String,
                         var defCharge:String,
                         var defPayTerms:String,
                         var defDeposit:String,
                         var defInvoice:String,
                         var defTaxRate:String
): Parcelable{
    override fun toString(): String {
        return ""
    }
}

@Parcelize
data class SalesTaxType(var ID:String,
                        @field:SerializedName("listID", alternate= ["ListID"])
                        var listID:String,
                        @field:SerializedName("name", alternate= ["Name"])
                        var name:String,
                        @field:SerializedName("description", alternate= ["Description"])
                        var description:String,
                        @field:SerializedName("taxRate", alternate= ["TaxRate"])
                        var taxRate:String,
                        var active:String,
                        var nameAndRate:String? = ""
): Parcelable{
    override fun toString(): String {
        return name
    }
}

@Parcelize
data class DepositType(var ID:String,
                       var name:String,
                       var depositTerms:String? = "",
                       var fixed:String? = "",
                       var perc:String? = "",
                       var fixedAmount:String? = "",
                       var paymentDays:String? = ""
): Parcelable{
    override fun toString(): String {
        return name
    }
}

@Parcelize
data class Album(var ID:String,
                 var name: String,
                 var created: String? = "",
                 var creator: String? = "",
                 var description: String? = "",
                 var coverImage: String? = "",
                 var onWeb: String? = "",
                 var featured: String? = ""
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
                 var total_only: String,
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
                 @field:SerializedName("chargeType", alternate= ["charge"])
                 var chargeType: String? = "",
                 var override: String? = "",
                 var pic: String? = "",
                 var del: String? = "",
                 var custName: String? = "",
                 var woStatus: String? = "",
                 var hasReceipt: String? = "",
                 var addedByName: String? = "",
                 var addedNice: String? = "",

                 var locked: Boolean = false,
                 var receipt:Image? = null,
                 var editsMade:Boolean = false,
                 var startDateTime: LocalDateTime? = null,
                 var stopDateTime: LocalDateTime? = null,

                 var progressViewVisible: Boolean = false
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
data class Vendor(
    /*                
    val ID: String,
                  val name: String = "",
                  val mainAddr:String? = "",
                  var lng:String? = "",
                  var lat:String? = "",
                  val website:String? = "",
                  val mainPhone:String? = "",
                  var balance:String? = "",
                  val cost:String? = "",
                  val price:String? = "",
                  val prefered:String? = "",
                  var itemString:String? = "",
                  var active:String? = "",
                  var fname: String? = "",
                  var mname: String? = "",
                  var lname: String? = "",
                  var companyName: String? = "",
                  var salutation: String? = "",

                  var phone: String? = "",
                  var email: String? = "",

                  var street1: String? = "",
                  var street2: String? = "",
                  var street3: String? = "",
                  var street4: String? = "",
                  var city: String? = "",
                  var zip: String? = "",
                  var state: String? = "",

                  var billStreet1: String? = "",
                  var billStreet2: String? = "",
                  var billStreet3: String? = "",
                  var billStreet4: String? = "",
                  var billCity: String? = "",
                  var billZip: String? = "",
                  var billState: String? = "",

                  var documentType: String? = "",
                  var paymentTermsID: String? = "",
                  var category: String? = "",
                  
     */



            var ID: String,
            var name: String,

            var active: String = "1",

            var itemString: String? = "",

            var fname: String? = "",
            var mname: String? = "",
            var lname: String? = "",
            var companyName: String? = "",
            var salutation: String? = "",

            var mainAddr: String? = "",
            var lng: String? = "",
            var lat: String? = "",
            var mainPhone: String? = "",
            var mainEmail: String? = "",
            var website: String? = "",

            var balance: String? = "",
            var minQty: String? = "",

            var cost: String? = "",
            var price: String? = "",
            @field:SerializedName(value="preferred", alternate= ["prefered"])
            var preferred: String? = "",

            var addr1: String? = "",
            var addr2: String? = "",
            var addr3: String? = "",
            var addr4: String? = "",
            var city: String? = "",
            var zip: String? = "",
            var state: String? = "",

            var baddr1: String? = "",
            var baddr2: String? = "",
            var baddr3: String? = "",
            var baddr4: String? = "",
            var bcity: String? = "",
            var bzip: String? = "",
            var bstate: String? = "",

            @field:SerializedName(value="category", alternate= ["type"])
            var category: String? = "",
            @field:SerializedName(value="ppreferredDocumentType", alternate= ["pref"])
            var preferredDocumentType: String? = "",
            var paymentTermsID: String? = "",
                  
                  
): Parcelable{
    override fun toString(): String {
        return name
    }
}


@Parcelize
data class VendorCategory(var ID: String,
                          var name: String,
                          var fullname: String,
                          var parentID: String,
                          var active: String

): Parcelable {
    override fun toString(): String {
        return name
    }
}



@Parcelize
data class WoItem(var ID:String,
                  var item: String,
                  var itemTranslated: String?,
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
                  @field:SerializedName(value="woID", alternate= ["workOrderID"])
                  var woID: String = "",
                  var woTitle: String = "",
                  var contractID: String = "",
                  var contractTitle: String = "",
                  var taxType: String = "",
                  var hideUnits: String = "",
                  var subcontractor: String = "",
                  var locked: String = "",
                  var printView: String = "",
                  var estValue: String = "",
                  var estUnit: String = "",
                  var estNotes: String = "",
                  var minQty: String? = "",
                  var tax: String? = "",

                  var tasks:Array<Task>? = arrayOf(),
                  var usage:Array<Usage>? = arrayOf(),
                  var vendors:Array<Vendor>? = arrayOf()
                          //usage
                          //vendors



): Parcelable{
    override fun toString(): String {
        return item
    }
}


@Parcelize
data class WorkOrder(@field:SerializedName(value="woID", alternate= ["ID"])
                     var woID: String = "0",
                     var status: String = "",
                     var title:String = "",
                     var titleTranslated:String? = null,
                     var progress:String = "",
                     //var totalPrice:String = "",

                     //@field:SerializedName(value="totalPriceRaw", alternate= ["price"])
                     //var totalPriceRaw:String = "",
                     //var totalCostRaw:String = "",

                     var total:String = "",
                     var totalCost:String = "",
                     var profit:String = "",
                     var profitAmount:String = "",
                     var mainEmail:String? = "",
                     var mainPhone:String? = "",
                     var statusName:String? = "",
                     var prompt:String? = "",
                     var recID:String? = "",
                     var customer:String? = "",
                     var custName:String? = "",
                     var custAddress:String? = "",
                     var allowImages:String? = "",
                     var date:String? = "",
                     var dateNice:String? = "",
                     var salesRep:String? = "",
                     @field:SerializedName(value="salesRepName", alternate= ["repName"])
                     var salesRepName:String? = "",
                     var notes:String? = "",
                     var charge:String? = "",
                     var chargeName:String? = "",
                     @field:SerializedName(value="invoiceType", alternate= ["invoice"])
                     var invoiceType:String? = "",
                     var invoiceID:String? = "0",
                     var ignoreExpired:String? = "0",
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
                     var urgent:String? = "0",
                     var archived:String? = "0",
                     var copied:String? = "0",
                     var skipped:String? = "0",
                     var paymentTermsID: String? = "",
                     var deadline: String? = "",
                     var contractID: String? = "0",
                     var salesTaxID: String? = "0",
                     //var renew: String? = "0",

                     var items:Array<WoItem>? = null,
                     var lead:Lead? = null,
                     var contract:Contract? = null,

                     var crews:Array<Crew>? = null,
                     var emps:MutableList<Employee> = mutableListOf(),
                     var recSettings:ContractRecSettings? = null

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
data class WorkOrderArray (
    var workOrders: Array<WorkOrder>?,
    var note: String?
    ) : Parcelable


enum class MyScheduleEntryType {
    workOrder, lead, service
}

@Parcelize
data class MyScheduleEntry(var refID: String = "0",
                           var title: String = "0",
                           var name: String? = "0",
                           var status: String? = "0",
                           var type: String? = "0",
                           var lng: String? = "0",
                           var lat: String? = "0",
                           var daySort: String? = "0",
                           var startTime: String? = "0",
                           var endTime: String? = "0",
                           var urgent: String? = "0",
                           var usage: String? = "0",
                           var usageType: String? = "0",
                           var serviceType: String? = "0",
                           var equipmentID: String? = "0",
                           var firm: String? = "0"

    ) : Parcelable {
    @IgnoredOnParcel
    var entryType:MyScheduleEntryType = MyScheduleEntryType.workOrder

    override fun toString(): String {
        return title
    }

    fun checkIfCompleted(): Boolean {
        if (entryType == MyScheduleEntryType.service) {
            if (status == "2" || status == "3" || status == "4") {
                return true
            }
        }
        else {
            if (status == "3" || status == "4") {
                return true
            }
        }
        return false
    }

}

@Parcelize
data class MyScheduleEntryArray(
    @field:SerializedName(value="entries", alternate= ["schedule"])
    var entries: Array<MyScheduleEntry>? = null,
    var note:String? = ""

) : Parcelable {
    override fun toString(): String {
        return "MyScheduleEntryArray"
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

    lateinit var pgsBar: ProgressBar
    lateinit var globalVars:GlobalVars
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result //this is the token retrieved
            println("Firebase Messaging Token: $token")
        }

        globalVars = GlobalVars()

        /*

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                // 2
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // 3
                val token = task.result?.token

                // 4
                val msg = getString(R.string.token_prefix, token)
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
            })

         */
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
            onBackPressed()
            return super.onOptionsItemSelected(item)
        }

        if (id == R.id.home_item) {
            home()
            return super.onOptionsItemSelected(item)
        }
        if (id == R.id.logout_item) {
            logOut(myView)
            return super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)

    }

    /*
    override fun onSupportNavigateUp(): Boolean {
        println("on support navigate up")
        if (GlobalVars.shouldLogOut) {
            GlobalVars.shouldLogOut = false

            globalVars.logOut(myView.context, myView)
            return true
        }
        else {
            return super.onSupportNavigateUp()
        }
    }
     */

    override fun onBackPressed() {
        println("on back pressed")
        if (GlobalVars.shouldLogOut) {
            //GlobalVars.shouldLogOut = false

            globalVars.logOut(myView.context, myView)

        }
        else {
            super.onBackPressed()
        }
    }

    /*
    fun back() {
        println("back")
        // This allows back button checks like "Are you sure you want to exit without submitting" to work on the menu bar back as well
        //println(getVisibleFragment()!!.lay)

        if (GlobalVars.shouldLogOut) {
            GlobalVars.shouldLogOut = false

            globalVars.logOut(myView.context, myView)
        }
        else {
            super.onBackPressed()
        }
    }

     */

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

                        GlobalVars.shouldLogOut = false

                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                        val navController = navHostFragment.navController

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

                        hideProgressView()
                        navController.popBackStack(R.id.logInFragment, false)


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







}