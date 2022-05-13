package com.example.AdminMatic

//import android.R
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.spinner_right_aligned.view.*


class EmpAdapter(context: Context, items: List<Employee>?) :
    ArrayAdapter<Employee?>(context, R.layout.support_simple_spinner_dropdown_item, items!!) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (position == 0) {
            initialSelection(true)
        } else getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (position == 0) {
            initialSelection(false)
        } else getCustomView(position, convertView, parent)
    }

    override fun getCount(): Int {
        return super.getCount() + 1 // Adjust for initial selection item
    }

    private fun initialSelection(dropdown: Boolean): View {
        // Just an example using a simple TextView. Create whatever default view
        // to suit your needs, inflating a separate layout if it's cleaner.
        val view = TextView(context)
        view.textAlignment = View.TEXT_ALIGNMENT_CENTER

        //android:textColor="@color/colorPrimary"
        //android:textSize="16sp"

        view.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            16F
        )

        view.text = context.getString(R.string.add_employee)
        //val spacing =
           // context.resources.getDimensionPixelSize(R.dimen.spacing_smaller)
       // view.setPadding(0, spacing, 0, spacing)
        if (dropdown) { // Hidden when the dropdown is opened
            view.height = 0
        }
        return view
    }


    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Distinguish "real" spinner items (that can be reused) from initial selection item
        var lPosition = position


        val row: View =
            if (convertView != null && convertView !is TextView) convertView else LayoutInflater.from(
                context
            ).inflate(com.AdminMatic.R.layout.spinner_right_aligned, parent, false)
        lPosition -= 1 // Adjust for initial selection item
        val employee: Employee? = getItem(lPosition)




        println("employee = ${employee.toString()}")

        //var txt = row.findViewById<TextView>
        row.textView.text = employee.toString()

       // row.foregroundGravity =

       // row.setBackgroundColor(Color.RED)

       // var nameText:TextView = TextView(myView.context)
       // nameText.text = employee!!.name




        // ... Resolve views & populate with data ...
        return row
    }
}
