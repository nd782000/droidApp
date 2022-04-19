package com.example.AdminMatic

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_upload.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    private var param2: String? = null

    private  var image: Image? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar
    lateinit var imageView:ImageView
    private lateinit var likeView:ImageView
    private lateinit var likesTextView:TextView
    private lateinit var custNameTextView:TextView
    private lateinit var detailsTextView:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getParcelable("image")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_image, container, false)
        myView = inflater.inflate(R.layout.fragment_image, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = image!!.name

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("image = ${image!!.ID}")


        pgsBar = view.findViewById(R.id.progress_bar)

        imageView = myView.findViewById(R.id.image_details_iv)


        println("image path = ${GlobalVars.mediumBase + image!!.fileName}")
        Picasso.with(context)
            .load(GlobalVars.mediumBase + image!!.fileName)
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(imageView)                        //Your image view object.

        likeView  = myView.findViewById(R.id.like_iv)
        println("image.likes = ${image!!.likes}")
        if (image!!.likes!! != "0"){
            likeView.visibility = View.VISIBLE
           // val likeIcon = resources.getDrawable(R.drawable.ic_liked,null)
            //likeView.background = likeIcon
        }

        likesTextView  = myView.findViewById(R.id.likes_tv)
        likesTextView.text = getString(R.string.x_likes, image!!.likes)

        custNameTextView  = myView.findViewById(R.id.image_customer_tv)
        custNameTextView.text = image!!.customerName

        detailsTextView  = myView.findViewById(R.id.image_details_tv)
        detailsTextView.text = image!!.description

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}