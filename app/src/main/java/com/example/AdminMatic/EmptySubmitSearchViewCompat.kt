package com.example.AdminMatic

import android.content.Context

import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import com.AdminMatic.R

// Extension of appcompat SearchView to allow empty queries to be submitted
// (so the keyboard can be closed with an empty query)

class EmptySubmitSearchViewCompat : SearchView {

    private var mSearchSrcTextView: SearchAutoComplete? = null
    var listener: OnQueryTextListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {}

    override fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        super.setOnQueryTextListener(listener)
        this.listener = listener
        mSearchSrcTextView = this.findViewById(R.id.search_src_text)
        mSearchSrcTextView!!.setOnEditorActionListener { _, _, _ ->
            listener?.onQueryTextSubmit(query.toString())
            true
        }
    }
}