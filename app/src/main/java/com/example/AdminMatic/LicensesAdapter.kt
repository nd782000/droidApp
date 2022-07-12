package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import kotlinx.android.synthetic.main.equipment_list_item.view.*
import java.util.*



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
        //TODO: make expired date text (not the full string) red
        mExpirationView!!.text = context.getString(R.string.license_expires, license.expiration)
    }
}