package com.example.AdminMatic


import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

import android.media.MediaPlayer

import com.AdminMatic.R


class GlobalVars: Application() {

    companion object {
        var test:String = "testString"
        var loggedInEmployee:Employee? = null

        var thumbBase:String? = null
        var mediumBase:String? = null
        var rawBase:String? = null

        var employeeList:Array<Employee>? = null

        var globalWorkOrdersList:MutableList<WorkOrder>? = null
        var scheduleSpinnerPosition:Int = 2



        var deviceID:String? = null
    }


    override fun onCreate() {
        super.onCreate()
        // initialization code here

    }

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
    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    fun capitalize(str: String): String {
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

     fun resize(image: Drawable, context:Context): Drawable? {
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 25, 25, true)
        return BitmapDrawable(context.resources, bitmapResized)
    }


     fun menuIconWithText(
        r: Drawable,
        title: String
    ): CharSequence? {
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
        println("getTimeFromString")
        val formatterLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val time = LocalTime.parse(s, formatterLong)
        println("time from string = $time")
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

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            //Toast.makeText(context,
               // android.R.string.yes, Toast.LENGTH_SHORT).show()
        }


        builder.show()
    }



}