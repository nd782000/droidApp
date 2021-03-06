package com.example.AdminMatic


import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import android.view.View
import com.AdminMatic.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
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
        var employeeList:Array<Employee>? = null

        var globalWorkOrdersList:MutableList<WorkOrder>? = null
        var globalLeadList:MutableList<Lead>? = null
        var scheduleSpinnerPosition:Int = 2

        var deviceID:String? = null

        var customerList: MutableList<Customer>? = null

        //Todo: Wire alternate formats into localization system whenever that's in place
        val dateFormatterPHP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") //format from the php
        val dateFormatterShort: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy") //format to display
        val dateFormatterMonthDay: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd") //format to display
        val dateFormatterShortDashes: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yy") //format to display
        val dateFormatterYYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") //format to display

        val moneyFormatter: NumberFormat = DecimalFormat("#,###,##0.00")
    }


    //private var listener: LogOut? = null




    /* helper functions */
    fun playSaveSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.save)
        mediaPlayer.start()
    }

    fun playErrorSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.error)
        mediaPlayer.start()
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

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            //Toast.makeText(context,
               // android.R.string.yes, Toast.LENGTH_SHORT).show()
        }


        builder.show()
    }
    
    
    fun checkPHPWarningsAndErrors(jsonObject: JSONObject, context:Context, myView_: View) {

        val gson = GsonBuilder().create()

        val warningArray: JSONArray = jsonObject.getJSONArray("warningArray")
        val errorArray: JSONArray = jsonObject.getJSONArray("errorArray")


        val warnings = gson.fromJson(warningArray.toString(), Array<String>::class.java)
        val errors = gson.fromJson(errorArray.toString(), Array<String>::class.java)

        println("warning size: ${warnings.size}")
        println("warning size: ${errors.size}")

        if (errors.isNotEmpty()) {
            playErrorSound(context)
            var errorString = ""
            errors.forEach {
                errorString += it
                errorString += "\n"
            }
            simpleAlert(context, context.getString(R.string.dialogue_php_error), errorString)
            //Logout
            val listener = if (context is LogOut) {
                context
            } else {
                throw ClassCastException(
                    "$context must implement LogOut"
                )
            }
            listener.logOut(myView_)
        }
        else if (warnings.isNotEmpty()) {
            playErrorSound(context)
            var warningString = ""
            warnings.forEach {
                warningString += it
                warningString += "\n"
            }
            simpleAlert(context, context.getString(R.string.dialogue_php_warning), warningString)
        }
    }


    /*
    private fun getCustomers(){
        println("getCustomers")

        //showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/customers.php"

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