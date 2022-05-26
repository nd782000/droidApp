package com.example.AdminMatic

import android.app.DatePickerDialog
import android.content.Context
import com.AdminMatic.R
import java.util.*

class DatePickerHelper(
    context: Context,
    isSpinnerType: Boolean = false
) {
    private var dialog: DatePickerDialog
    private var callback: Callback? = null
    private val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        callback?.onDateSelected(year, month, dayOfMonth)
    }
    init {
        val style = if (isSpinnerType) R.style.SpinnerDatePickerDialog else 0
        val cal = Calendar.getInstance()
        dialog = DatePickerDialog(context, style, listener,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }
    fun showDialog(year: Int, month: Int, dayOfMonth: Int, callback: Callback?) {
        this.callback = callback
        dialog.updateDate(year, month, dayOfMonth)
        dialog.show()
    }
    interface Callback {
        fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
    }
}