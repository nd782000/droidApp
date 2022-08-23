package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso




class LicensesAdapter(list: MutableList<License>, private val context: Context) : RecyclerView.Adapter<LicenseViewHolder>() {

    var filterList:MutableList<License> = emptyList<License>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LicenseViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {

        val license: License = filterList[position]


        val licenseStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_license_icon_iv)

        //TODO: figure out preloading on these
        when (license.status) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_badge_star_gray)
                .into(licenseStatusImageView)
            else -> Picasso.with(context)
                .load(R.drawable.ic_badge_star)
                .into(licenseStatusImageView)
        }


        holder.bind(license, context)



    }

    override fun getItemCount(): Int{
        print("getItemCount = ${filterList.size}")
        return filterList.size
    }


}

class LicenseViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.license_list_item, parent, false)) {

    private var mNameView: TextView? = null
    private var mIDView: TextView? = null
    private var mExpirationView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_license_name_tv)
        mIDView = itemView.findViewById(R.id.list_license_number_tv)
        mExpirationView = itemView.findViewById(R.id.list_license_expiration_tv)
    }

    fun bind(license: License, context: Context) {
        mNameView!!.text = license.name
        mIDView!!.text = context.getString(R.string.license_number, license.number)

        if (license.status == "0" || license.status == "1") { // Expired or near expired status

            var typeColor = context.getColor(R.color.red)
            if (license.status == "1") { // Near expired
                typeColor = context.getColor(R.color.orange)
            }

            val spannable: Spannable = SpannableString(context.getString(R.string.license_expires, license.expiration))
            val endPos1 = spannable.length
            val startPos1 = spannable.length - 8

            val colorStateList = ColorStateList(
                arrayOf(intArrayOf()),
                intArrayOf(typeColor)
            )
            val textAppearanceSpan =
                TextAppearanceSpan(null, Typeface.NORMAL, -1, colorStateList, null)
            spannable.setSpan(
                textAppearanceSpan,
                startPos1,
                endPos1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mExpirationView!!.text = spannable
        }
        else {
            mExpirationView!!.text = context.getString(R.string.license_expires, license.expiration)
        }


    }
}