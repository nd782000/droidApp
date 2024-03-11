package com.example.AdminMatic


import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.provider.Settings.Global
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class GlobalVars: Application() {

    companion object {
        var test:String = "testString"
        var loggedInEmployee:Employee? = null
        var permissions:Permissions? = null

        var thumbBase:String? = null
        var mediumBase:String? = null
        var rawBase:String? = null

        var hearTypes:Array<HearType>? = null
        var departments:Array<Department>? = null
        var crews:Array<Crew>? = null
        var zones:Array<Zone>? = null
        var albums:Array<Album>? = null
        var vendorCategories:Array<VendorCategory>? = null
        var paymentTerms:Array<PaymentTerms>? = null
        var contactTypes:Array<ContactType>? = null
        var templates:Array<Template>? = null
        var defaultFields:DefaultFields? = null
        var salesTaxTypes:Array<SalesTaxType>? = null
        var depositTypes:Array<DepositType>? = null

        // Flagged when you get a server error and are kicked to the bug log view
        var shouldLogOut:Boolean = false

        var colorArray:Array<String> = arrayOf(
            "#C62D42",
            "#FE4C40",
            "#FF681F",
            "#FFAE42",
            "#B5B35C",
            "#ACBF60",
            "#5E8C31",
            "#3AA655",
            "#29AB87",
            "#008080",
            "#009DC4",
            "#00468C",
            "#0066FF",
            "#4F69C6",
            "#6B3FA0",
            "#C154C1",
            "#8E3179",
            "#DA3287",
            "#A55353",
            "#AF593E",
            "#664228",
            "#837050",
            "#353839",
            "#1B1B1B"
        )

        var states:Array<String> = arrayOf("Select a State",
            "AK - Alaska",
            "AL - Alabama",
            "AR - Arkansas",
            "AS - American Samoa",
            "AZ - Arizona",
            "CA - California",
            "CO - Colorado",
            "CT - Connecticut",
            "DC - District of Columbia",
            "DE - Delaware",
            "FL - Florida",
            "GA - Georgia",
            "GU - Guam",
            "HI - Hawaii",
            "IA - Iowa",
            "ID - Idaho",
            "IL - Illinois",
            "IN - Indiana",
            "KS - Kansas",
            "KY - Kentucky",
            "LA - Louisiana",
            "MA - Massachusetts",
            "MD - Maryland",
            "ME - Maine",
            "MI - Michigan",
            "MN - Minnesota",
            "MO - Missouri",
            "MS - Mississippi",
            "MT - Montana",
            "NC - North Carolina",
            "ND - North Dakota",
            "NE - Nebraska",
            "NH - New Hampshire",
            "NJ - New Jersey",
            "NM - New Mexico",
            "NV - Nevada",
            "NY - New York",
            "OH - Ohio",
            "OK - Oklahoma",
            "OR - Oregon",
            "PA - Pennsylvania",
            "PR - Puerto Rico",
            "RI - Rhode Island",
            "SC - South Carolina",
            "SD - South Dakota",
            "TN - Tennessee",
            "TX - Texas",
            "UT - Utah",
            "VA - Virginia",
            "VI - Virgin Islands",
            "VT - Vermont",
            "WA - Washington",
            "WI - Wisconsin",
            "WV - West Virginia",
            "WY - Wyoming")

        var statesShort:Array<String> = arrayOf("",
            "AK",
            "AL",
            "AR",
            "AS",
            "AZ",
            "CA",
            "CO",
            "CT",
            "DC",
            "DE",
            "FL",
            "GA",
            "GU",
            "HI",
            "IA",
            "ID",
            "IL",
            "IN",
            "KS",
            "KY",
            "LA",
            "MA",
            "MD",
            "ME",
            "MI",
            "MN",
            "MO",
            "MS",
            "MT",
            "NC",
            "ND",
            "NE",
            "NH",
            "NJ",
            "NM",
            "NV",
            "NY",
            "OH",
            "OK",
            "OR",
            "PA",
            "PR",
            "RI",
            "SC",
            "SD",
            "TN",
            "TX",
            "UT",
            "VA",
            "VI",
            "VT",
            "WA",
            "WI",
            "WV",
            "WY")

        var employeeList:Array<Employee>? = null


        var globalWorkOrdersList:MutableList<WorkOrder>? = null
        //var globalDayNote:String? = null
        var globalLeadList:MutableList<Lead>? = null
        //var globalMapScheduleEntryList:MutableList<MyScheduleEntry> = mutableListOf()
        var globalMyScheduleSections = mutableListOf<MyScheduleSection>()

        var deviceID:String? = null

        var phpVersion:String = "1-10"

        var customerList: MutableList<Customer>? = null

        //Todo: Wire alternate formats into localization system whenever that's in place
        val dateFormatterPHP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") //format from the php
        val dateFormatterShort: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy") //format to display
        val dateFormatterNoYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd") //format to display
        val dateFormatterMonthDay: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd") //format to display
        val dateFormatterShortDashes: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yy") //format to display
        val dateFormatterYYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") //format to display
        val dateFormatterWeekday: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MM/dd/yy") //format to display


        val dateFormatterHHMM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatterHMMA: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val dateFormatterHHMMSS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        val moneyFormatter: NumberFormat = DecimalFormat("#,###,##0.00")

        var contractSingle: EmailTemplate? = null
        var invoiceSingle: EmailTemplate? = null
    }


    //private var listener: LogOut? = null


    fun updateGlobalMySchedule(id:String, entryType:MyScheduleEntryType, newStatus:String) {

        for (sec in globalMyScheduleSections) {
            for (entry in sec.entries) {
                if (entry.refID == id && entry.entryType == entryType) {
                    entry.status = newStatus
                }
            }
            /*
            for (entry in sec.entriesFiltered) {
                if (entry.refID == id && entry.entryType == entryType) {
                    entry.status = newStatus

                    /*
                    // If it just got set to complete, remove it from the filtered list
                    if (entry.entryType == MyScheduleEntryType.service) {
                        if (entry.status == "2" || entry.status == "3" || entry.status == "4") {
                            sec.entries.remove(entry)
                        }
                    }
                    else {
                        if (entry.status == "3" || entry.status == "4") {
                            sec.entries.remove(entry)
                        }
                    }
                     */

                }
            }

             */
        }



    }


    /* helper functions */
    fun playSaveSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.save)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        mediaPlayer.start()
    }

    fun playErrorSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.error)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        mediaPlayer.start()
    }


    // Cleanly enable to disable a SearchView
    fun enableSearchView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                enableSearchView(child, enabled)
            }
        }
    }

    fun populateFields(context:Context?, parentObject:JSONObject) {
        thumbBase = parentObject.getString("thumbBase")
        mediumBase = parentObject.getString("mediumBase")
        rawBase = parentObject.getString("rawBase")

        val gson = GsonBuilder().create()
        try {
            val hearTypes: JSONArray = parentObject.getJSONArray("hearTypes")
            GlobalVars.hearTypes = gson.fromJson(hearTypes.toString(), Array<HearType>::class.java)
        }
        catch (e:JSONException) {
            println("Did not find hearTypes array")
        }

        try {
            val departments:JSONArray = parentObject.getJSONArray("departments")
            GlobalVars.departments = gson.fromJson(departments.toString() , Array<Department>::class.java)
        }
        catch (e:JSONException) {
            println("Did not find departments array")
        }

        try {
            val crews:JSONArray = parentObject.getJSONArray("crews")
            GlobalVars.crews = gson.fromJson(crews.toString() , Array<Crew>::class.java)
        }
        catch (e:JSONException) {
            println("Did not find crews array")
        }

        try {
            val zones: JSONArray = parentObject.getJSONArray("zones")
            GlobalVars.zones = gson.fromJson(zones.toString(), Array<Zone>::class.java)
            // Add "no zone" field
            val noZone = Zone("0", context!!.getString(R.string.no_zone))
            var zonesMutableList: MutableList<Zone>
            GlobalVars.zones.let {
                zonesMutableList = it!!.toMutableList()
            }
            zonesMutableList.add(0, noZone)
            GlobalVars.zones = zonesMutableList.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find zones array")
        }

        try {
            val albums:JSONArray = parentObject.getJSONArray("albums")
            GlobalVars.albums = gson.fromJson(albums.toString() , Array<Album>::class.java)
        }
        catch (e:JSONException) {
            println("Did not find albums array")
        }

        try {
            val vendorCategories:JSONArray = parentObject.getJSONArray("vendorCategories")
            GlobalVars.vendorCategories = gson.fromJson(vendorCategories.toString() , Array<VendorCategory>::class.java)
            // Add "no category" field
            val noVendorCategory = VendorCategory("0", context!!.getString(R.string.no_category), context.getString(R.string.no_category), "0", "1")
            var vendorCategoriesMutableList: MutableList<VendorCategory>
            GlobalVars.vendorCategories.let {
                vendorCategoriesMutableList = it!!.toMutableList()
            }
            vendorCategoriesMutableList.add(0, noVendorCategory)
            GlobalVars.vendorCategories = vendorCategoriesMutableList.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find vendorCategories array")
        }

        try {
            val paymentTerms:JSONArray = parentObject.getJSONArray("terms")
            GlobalVars.paymentTerms = gson.fromJson(paymentTerms.toString() , Array<PaymentTerms>::class.java)
            println("payment terms size: ${GlobalVars.paymentTerms!!.size}")
            // Add "no payment terms" field
            val noTerms = PaymentTerms("0", context!!.getString(R.string.no_payment_terms))
            var termsMutableList: MutableList<PaymentTerms>
            GlobalVars.paymentTerms.let {
                termsMutableList = it!!.toMutableList()
            }
            termsMutableList.add(0, noTerms)
            GlobalVars.paymentTerms = termsMutableList.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find terms array")
        }

        try {
            val contactTypes: JSONArray = parentObject.getJSONArray("contactTypes")
            GlobalVars.contactTypes = gson.fromJson(contactTypes.toString(), Array<ContactType>::class.java)

            val contactTypesFiltered = mutableListOf<ContactType>()

            // Remove jobSite, billing Addr or invoice Addr
            // (Code copied from iOS)
            GlobalVars.contactTypes.let {
                it!!.forEach { ct ->
                    if (ct.ID != "3" && ct.ID != "4" && ct.ID != "14") {
                        contactTypesFiltered.add(ct)
                    }
                }
            }

            GlobalVars.contactTypes = contactTypesFiltered.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find contactTypes array")
        }

        try {
            val contractSingleObject:JSONObject = parentObject.getJSONObject("contract-single")
            contractSingle = gson.fromJson(contractSingleObject.toString() , EmailTemplate::class.java)
        }
        catch (e:JSONException) {
            println("Did not find contract-single object")
        }

        try {
            val invoiceSingleObject:JSONObject = parentObject.getJSONObject("invoice-single")
            invoiceSingle = gson.fromJson(invoiceSingleObject.toString() , EmailTemplate::class.java)
        }
        catch (e:JSONException) {
            println("Did not find invoice-single object")
        }

        try {
            val templates: JSONArray = parentObject.getJSONArray("templates")
            GlobalVars.templates = gson.fromJson(templates.toString(), Array<Template>::class.java)
            println("TEMPLATES ARRAY: ${templates}")
        }
        catch (e:JSONException) {
            println("Did not find templates array")
        }

        try {
            val defaultFields:JSONObject = parentObject.getJSONObject("defaults")
            GlobalVars.defaultFields = gson.fromJson(defaultFields.toString() , DefaultFields::class.java)
        }
        catch (e:JSONException) {
            println("Did not find defaults object")
        }

        try {
            val salesTaxTypes:JSONArray = parentObject.getJSONArray("salesTax")
            GlobalVars.salesTaxTypes = gson.fromJson(salesTaxTypes.toString() , Array<SalesTaxType>::class.java)
            val tempMLTax = mutableListOf<SalesTaxType>()
            for (tax in GlobalVars.salesTaxTypes!!) {
                if (tax.active == "1") {
                    println("tax rate: ${tax.taxRate}")
                    tax.nameAndRate = myView.context.getString(R.string.tax_name_and_rate, tax.name, tax.taxRate.toFloat())
                    println(tax.nameAndRate)
                    tempMLTax.add(tax)
                }
            }
            GlobalVars.salesTaxTypes = tempMLTax.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find salesTax array")
        }

        try {
            val depositTypes: JSONArray = parentObject.getJSONArray("depositTypes")
            GlobalVars.depositTypes = gson.fromJson(depositTypes.toString(), Array<DepositType>::class.java)
            val noDepositTypeRequired = DepositType("0", myView.context.getString(R.string.no_deposit_required))
            val tempMLDep = GlobalVars.depositTypes!!.toMutableList()
            tempMLDep.add(0, noDepositTypeRequired)
            GlobalVars.depositTypes = tempMLDep.toTypedArray()
        }
        catch (e:JSONException) {
            println("Did not find depositTypes array")
        }
    }


    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


    // To Add Icons to Menu Items

     fun resize(image: Drawable, context:Context): Drawable {
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 25, 25, true)
        return BitmapDrawable(context.resources, bitmapResized)
    }


     fun menuIconWithText(
        r: Drawable,
        title: String
    ): CharSequence {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_CENTER)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    fun menuColor(
        colorHex: String
    ): CharSequence {
        //val r:Drawable = AppCompatResources.getDrawable(myView.context, R.drawable.ic_color_swatch)!!
        val r:Drawable = resize(AppCompatResources.getDrawable(myView.context, R.drawable.ic_color_swatch)!!, myView.context)

        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        r.setTint(Color.parseColor(colorHex))
        val sb = SpannableString("    ")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_CENTER)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    /*
    fun getDateFromString(s:String): Date?{
        println("getLocalDateFromString")
        val formatterLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = Date.parse(s, formatterLong)
        println("local date from string = $date")

        return date
    }
*/


    fun getLocalDateFromString(s:String): LocalDate?{
        println("getLocalDateFromString")
        val formatterLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = LocalDate.parse(s, formatterLong)
        println("local date from string = $date")

        return date
    }


    fun getTimeFromString(s:String): LocalTime?{
        println("getTimeFromString $s")
        val time:LocalTime?
        if (s != "0000-00-00 00:00:00"){
            val formatterLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            time = LocalTime.parse(s, formatterLong)
            println("time from string = $time")
        }else {
            time = null
        }

        return time
    }

    fun getDBDateStringFromDate(date:Date):String?{
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)

        return simpleDateFormat.format(date)
    }


    fun simpleAlert(context:Context,title:String, message:String? = null){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        if (message != null){
            builder.setMessage(message)
        }

        builder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
            //Toast.makeText(context,
               // android.R.string.yes, Toast.LENGTH_SHORT).show()
        }


        builder.show()
    }

    fun logOut(context:Context, myView_: View) {
        val listener = if (context is LogOut) {
            context
        } else {
            throw ClassCastException(
                "$context must implement LogOut"
            )
        }
        println("Attempting to log out")
        VolleyRequestQueue.getInstance(context.applicationContext).requestQueue.cancelAll { true }
        listener.logOut(myView_)
    }


    fun checkPHPWarningsAndErrors(jsonObject: JSONObject, context:Context, myView_: View, suppressWarnings:Boolean = false): Boolean {

        val gson = GsonBuilder().create()

        val warningArray: JSONArray = jsonObject.getJSONArray("warningArray")
        val errorArray: JSONArray = jsonObject.getJSONArray("errorArray")


        val warnings = gson.fromJson(warningArray.toString(), Array<String>::class.java)
        val errors = gson.fromJson(errorArray.toString(), Array<String>::class.java)

        println("warning size: ${warnings.size}")
        println("error size: ${errors.size}")


        if (errors.isNotEmpty()) {
            playErrorSound(context)
            shouldLogOut = true



            var errorString = ""
            errors.forEach {
                errorString += it
                errorString += "\n"
            }

            /*
            Toast.makeText(
                myView.context,
                errorString, Toast.LENGTH_LONG
            ).show()
             */

            println("Navigating to bug log fragment")

            val bundle = bundleOf("errorString" to errorString, "shouldLogOut" to true)
            myView_.findNavController().navigate(R.id.navigateToBugLog, bundle)
            return false
        }
        else if (warnings.isNotEmpty() && !suppressWarnings) {
            //playErrorSound(context)
            var warningString = ""
            warnings.forEach {
                warningString += it
                warningString += "\n"
            }
            simpleAlert(context, context.getString(R.string.dialogue_php_warning), warningString)

            /*
            val builder = AlertDialog.Builder(myView_.context)
            builder.setMessage(myView_.context.getString(R.string.adminmatic_warning))
            builder.setPositiveButton(myView_.context.getString(R.string.yes)) { _, _ ->
                val bundle = bundleOf("errorString" to warningString, "shouldLogOut" to false)
                myView.findNavController().navigate(R.id.navigateToBugLog, bundle)


            }
            builder.setNegativeButton(myView_.context.getString(R.string.not_now)) { _, _ ->

            }

            builder.show()

             */

            return true
        }

        return true
    }


    /*
    private fun getCustomers(){
        println("getCustomers")

        //showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customers.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
               // hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    //var customers: JSONObject = parentObject.getJSONObject("customers")
                    val customers: JSONArray = parentObject.getJSONArray("customers")
                    // println("customers = ${customers.toString()}")
                    // println("customers count = ${customers.length()}")

                    val gson = GsonBuilder().create()
                    customerList = gson.fromJson(customers.toString() , Array<Customer>::class.java).toMutableList()



                    /*
                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            CustomersAdapter(customersList,
                                it, this@CustomerListFragment)
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        recyclerView.addItemDecoration(itemDecoration)


                        // Setup refresh listener which triggers new data loading
                        swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                            // Make sure you call swipeContainer.setRefreshing(false)
                            // once the network request has completed successfully.
                            searchView.setQuery("", false);
                            searchView.clearFocus();
                            getCustomers()
                        }
                        // Configure the refreshing colors
                        swipeRefresh.setColorSchemeResources(
                            R.color.button,
                            R.color.black,
                            R.color.colorAccent,
                            R.color.colorPrimaryDark
                        )
                        (adapter as CustomersAdapter).notifyDataSetChanged();

                        // Remember to CLEAR OUT old items before appending in the new ones

                        // Now we call setRefreshing(false) to signal refresh has finished
                        customerSwipeContainer.isRefreshing = false;

                        Toast.makeText(activity,"${customersList.count()} Customers Loaded", Toast.LENGTH_SHORT).show()

                        //search listener
                        customers_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                println("onQueryTextChange = $newText")
                                (adapter as CustomersAdapter).filter.filter(newText)
                                return false
                            }

                        })
                    }

                    */
                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
                //Log.e("VOLLEY", error.toString())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

     */


}