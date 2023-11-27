package com.example.AdminMatic

//import com.github.dhaval2404.imagepicker.ImagePicker

//import com.yongchun.library.view.ImageSelectorActivity
//import com.yongchun.library.view.ImageSelectorActivity.*

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageUploadBinding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import com.squareup.picasso.Picasso
import com.t2r2.volleyexample.FileDataPart
import com.t2r2.volleyexample.VolleyFileUploadRequest
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.*
import io.fotoapparat.view.CameraView
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.contracts.contract


class ImageCellData(_uri:Uri?, _imageData:Image?, _toBeSaved:Boolean) {
    var uri = _uri
    var imageData = _imageData
    var toBeSaved = _toBeSaved
}

//private var imageCellDataList:MutableList<ImageCellData> = mutableListOf()
private var imageCellMap = LinkedHashMap<ConstraintLayout, ImageCellData>()

private const val LOGGING_TAG = "AdminMatic"

private var mode: String = "GALLERY"
private var existingImages: Array<Image> = arrayOf()
private var customerID: String = ""
private var customerName: String = ""
private var woID: String = ""
private var woItemID: String = ""
private var contractItemID: String = ""
private var leadID: String = ""
private var taskID: String = ""
private var taskStatus: String = ""
private var taskDescription: String = ""
private var employeeID: String = ""
private var equipmentID: String = ""
private var usageID: String = ""
private var uncompressed = "0"

private var customerAllowImages: Boolean = true
private var queriedCustomer: Customer? = null


/*
private val mRetryPolicy: RetryPolicy = DefaultRetryPolicy(
    0,
    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
)

 */


//val REQUEST_CODE = 200
//private val cameraRequest = 1888


class ImageUploadFragment : Fragment(), CustomerCellClickListener, BottomSheetImagePicker.OnImagesSelectedListener{

    //private var mode: String? = null
    //private var customer: Customer? = null
    //lateinit var  pgsBar: ProgressBar
   // private lateinit var imageView: ImageView


    lateinit  var globalVars:GlobalVars






    // private var imageData: ByteArray? = null
    private val postURL: String = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/image.php"
   // private var selectedCustId: String? = null
    //var currentCameraUri :Uri? = null
    //var selectedUris: MutableList<Uri> = mutableListOf()
    var uploadedImageCount = 0



    // var albumID: String = ""


    lateinit var customerRecyclerView: RecyclerView
    private lateinit var customerSearchView:androidx.appcompat.widget.SearchView
    lateinit var adapter:CustomersAdapter


    lateinit var descriptionTxt:EditText

   // private var callBack:ImageUploadInterface? = null


    private lateinit var permissionsDelegate: PermissionsDelegate
    private lateinit var cameraView: CameraView

    private var permissionsGranted: Boolean = false
    //private var activeCamera: Camera = Camera()

    private lateinit var fotoapparat: Fotoapparat
    //private lateinit var cameraZoom: Zoom.VariableZoom

    //private var curZoom: Float = 0f

    private lateinit var captureBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            println("onCreate")
            mode = it.getString("mode")!!


            existingImages = it.getParcelableArray("images")!! as Array<Image>
            customerID = it.getString("customerID")!!
            customerName = it.getString("customerName")!!
            woID = it.getString("woID")!!
            woItemID = it.getString("woItemID")!!
            contractItemID = it.getString("contractItemID")!!
            leadID = it.getString("leadID")!!
            taskID = it.getString("taskID")!!
            taskDescription = it.getString("taskDescription")!!
            taskStatus = it.getString("taskStatus")!!
            employeeID = it.getString("employeeID")!!
            equipmentID = it.getString("equipmentID")!!
            usageID = it.getString("usageID")!!

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                reverseOrientationObserver = context?.let { it1 ->
                    ReverseOrientationObserver(it1) {
                        fotoapparat.stop()
                        fotoapparat.start()
                    }
                }
            }

             */

            println("images.count = ${existingImages.count()}")


        }
    }

    /*
    override fun onDestroy() {
        super.onDestroy()
        reverseOrientationObserver?.quit()
        reverseOrientationObserver = null
    }

     */

    private var _binding: FragmentImageUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentImageUploadBinding.inflate(inflater, container, false)
        myView = binding.root


        println("onCreateView")
        customerSearchView = myView.findViewById(R.id.image_upload_prep_customer_search)
        customerSearchView.setQuery(customerName, false)
        adapter = CustomersAdapter(GlobalVars.customerList!!, this, false)
        globalVars = GlobalVars()


        customerRecyclerView = myView.findViewById(R.id.customer_search_rv)
        customerRecyclerView.visibility = View.GONE

        descriptionTxt = myView.findViewById(R.id.image_upload_task_edit_txt)

        if (taskDescription != ""){
            descriptionTxt.text = Editable.Factory.getInstance().newEditable(taskDescription)
        }


      //  return inflater.inflate(R.layout.fragment_image_upload, container, false)

        return myView
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (customerID != "") {
            showProgressView()
            getAllowImages(customerID, false)
        }

        println("onViewCreated mode = $mode")
            var titleText = ""
        when (mode) {
            "GALLERY" -> {
                titleText = "Upload to Gallery"
                binding.imageUploadTaskEditTxt.visibility = View.GONE

                /*
                val params = binding.customerSearchRv.layoutParams as ConstraintLayout.LayoutParams
                params.bottomToBottom = binding.imageUploadPrepHeaderCl.id
                binding.customerSearchRv.requestLayout()
                 */

                binding.imageUploadPrepCustomerSearch.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToBottom = binding.imageUploadPrepHeaderCl.id
                }

                /*
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.frameLayout6)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                //constraintSet.connect(binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, binding.customerSearchRv.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.frameLayout6)

                 */

            }
            "CUSTOMER" -> {
                println("customer.ID = $customerID")
                titleText = "Upload to Customer"
            }
            "WO" -> {
                titleText = "Upload to W.O."
            }
            "WOITEM" -> {
                titleText = "Add Receipt Image"
                binding.imageUploadTaskEditTxt.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.imageUploadPrepHeaderCl)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.imageUploadPrepHeaderCl)
            }
            "LEADTASK" -> {
                titleText = "Upload to Lead Task"
            }
            "CONTRACTTASK" -> {
                titleText = "Upload to Contract Task"
            }
            "TASK" -> {
                titleText = "Upload to Task"
            }
            "EMPLOYEE" -> {
                titleText = "Upload to Employee"
            }
            "EQUIPMENT" -> {
                titleText = "Upload to Equipment"
                binding.imageUploadTaskEditTxt.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.imageUploadPrepHeaderCl)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.imageUploadPrepHeaderCl)
            }

        }

        if (mode != "GALLERY") {
            customerSearchView.visibility = View.GONE
        }

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = titleText

       // pgsBar = myView.findViewById(R.id.progress_bar)
       // hideProgressView()



        descriptionTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                taskDescription = descriptionTxt.text.toString()
            }
        })






        customerRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)




            if(GlobalVars.customerList != null) {
                adapter = activity?.let {


                    CustomersAdapter(
                        GlobalVars.customerList!!,
                        this@ImageUploadFragment,
                    false
                    )


                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                customerRecyclerView.addItemDecoration(itemDecoration)



                //(adapter as CustomersAdapter).notifyDataSetChanged()

                // Remember to CLEAR OUT old items before appending in the new ones


                //search listener
                customerSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        //customerRecyclerView.visibility = View.GONE
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        (adapter as CustomersAdapter).filter.filter(newText)
                        if(newText == ""){
                            customerRecyclerView.visibility = View.GONE
                            customerID = ""
                        }else{
                            customerRecyclerView.visibility = View.VISIBLE
                        }

                        return false
                    }

                })





            }
        }

        cameraView = myView.findViewById(R.id.cameraView)

        permissionsDelegate = PermissionsDelegate((activity as MainActivity?)!!)




        binding.cameraBtn.setOnClickListener{
            println("camera btn clicked")

            if (mode == "EQUIPMENT" && existingImages.isNotEmpty()) {
                globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.equipment_one_image))
            }
            else {
                launchCamera()
            }
        }


        captureBtn = view.findViewById(R.id.capture_btn)
        captureBtn.setOnClickListener{
            println("capture btn clicked")

            takePicture()
        }

        binding.galleryBtn.setOnClickListener{
            println("gallery btn clicked")

            launchGallery()
        }

        binding.uncompressedSwitch.setOnCheckedChangeListener { _, isChecked ->
            uncompressed = if (isChecked) {
                "1"
            } else {
                "0"
            }
        }

        binding.submitImagesBtn.setOnClickListener{
            println("submit btn clicked")

            if (!customerAllowImages && mode != "WOITEM") {
                globalVars.simpleAlert(myView.context,getString(R.string.no_image_collection),getString(R.string.no_image_collection_error))
                return@setOnClickListener
            }


            when (mode) {
                "GALLERY" -> {
                    uploadImage()
                }
                "CUSTOMER" -> {
                    uploadImage()
                }
                "WO" -> {

                }
                "WOITEM" -> {
                    uploadImage()
                }
                "LEADTASK" -> {
                    saveLeadTask()
                }
                "CONTRACTTASK" -> {
                    saveContractTask()
                }
                "TASK" -> {
                    saveTask()
                }
                "EMPLOYEE" -> {

                }
                "EQUIPMENT" -> {
                    uploadImage()
                }

            }


           //uploadImage()

        }

        //val lensPosition: LensPositionSelector

        //lensPosition = LensPosition.Back
        val configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                fixed(),
                autoFocus()
            )

        )


        fotoapparat = Fotoapparat(
            context = (activity as MainActivity?)!!,
            view = cameraView,
            focusView = binding.focusView,
            logger = logcat(),


            //lensPosition = lensPosition,
            cameraConfiguration = configuration,
            cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )
       // println(" fotoapparat.getCurrentParameters() = ${fotoapparat.getCurrentParameters()}")


        imageCellMap.clear()
        for (img in existingImages) {
            createImageCell(ImageCellData(null, img, false))
        }

    }

    override fun onStart() {
        super.onStart()
        fotoapparat.start()
    }

    override fun onStop() {
        super.onStop()
        print("stopping camera")
        fotoapparat.stop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageUpload")
    }


    private fun launchCamera(){
        println("launchCamera")

        permissionsGranted = permissionsDelegate.hasCameraPermission()

        if (permissionsGranted) {

            binding.cameraCl.visibility = View.VISIBLE
            //cameraView.visibility = View.VISIBLE
            //captureBtn.visibility = View.VISIBLE
            binding.imageUploadPrepFooterCl.visibility = View.GONE

        } else {
            permissionsDelegate.requestCameraPermission()
        }

    }

    private fun createImageCell(imageData:ImageCellData) {
        val cl = LayoutInflater.from(this.context).inflate(R.layout.image_upload_image_list_item, binding.imageUploadPrepSelectedImagesLl, false) as ConstraintLayout
        //cl.id = imageCellDataList.count()
        //imageCellDataList.add(imageData)
        imageCellMap[cl] = imageData

        val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
        pb.visibility = View.INVISIBLE

        val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
        tv.visibility = View.INVISIBLE

        val et = cl.findViewById(R.id.description_et) as EditText

        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                imageCellMap[cl]!!.toBeSaved = true
                if (imageCellMap[cl]!!.imageData != null) {
                    imageCellMap[cl]!!.imageData!!.description = s.toString()
                }
            }
        })

        if (imageData.imageData != null) {
            et.setText(imageData.imageData!!.description)
        }

        val deleteBtn = cl.findViewById(R.id.image_upload_image_item_delete_btn) as Button
        //deleteBtn.visibility = View.INVISIBLE
        //deleteBtn.alpha = 0.25f

        deleteBtn.setOnClickListener {
            println("delete")

            val imageDataToDelete = imageCellMap[cl]


            if (imageDataToDelete!!.imageData == null) {
                imageCellMap.remove(cl)
                binding.imageUploadPrepSelectedImagesLl.removeView(cl)
            }
            else {

                val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.delete_image_link_title))
                builder.setMessage(getString(R.string.delete_image_link_body))

                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    deleteImage(cl)
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }

                builder.show()


            }
        }

        binding.imageUploadPrepSelectedImagesLl.addView(cl)

        println("cl.findViewById(R.id.image_upload_image_item_iv) = ${cl.findViewById(R.id.image_upload_image_item_iv) as ImageView}")

        if (imageData.uri != null) {
            Glide.with(this).load(imageData.uri).into(cl.findViewById(R.id.image_upload_image_item_iv) as ImageView)
        }
        else {
            Picasso.with(context)
                .load(GlobalVars.thumbBase + imageData.imageData?.fileName)
                .placeholder(R.drawable.ic_images) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(cl.findViewById(R.id.image_upload_image_item_iv) as ImageView)
        }



        //cl.requestFocus()
        // var image_upload_prep_selected_images_sv = cl.findViewById(R.id.image_upload_prep_selected_images_sv) as ScrollView
        binding.imageUploadPrepSelectedImagesSv.fullScroll(View.FOCUS_DOWN)
    }

    private fun takePicture(){

        //val cl = LayoutInflater.from(this.context).inflate(R.layout.image_upload_image_list_item, binding.imageUploadPrepSelectedImagesLl, false) as ConstraintLayout
        //cl.id = selectedUris.count()


        //cameraView.visibility = View.GONE
        //captureBtn.visibility = View.GONE
        binding.cameraCl.visibility = View.GONE
        binding.imageUploadPrepFooterCl.visibility = View.VISIBLE


        val tsLong = System.currentTimeMillis()/1000

/*
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
       val output_file_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .toString() + File.separator + timeStamp + ".jpeg"


        try {
            val fos = FileOutputStream(pictureFile)
            var realImage = BitmapFactory.decodeByteArray(data, 0, data.length)
            val exif = ExifInterface(pictureFile.toString())
            Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION)!!)
            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("6", ignoreCase = true)) {
                realImage = rotate(realImage, 90)
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                    .equals("8", ignoreCase = true)
            ) {
                realImage = rotate(realImage, 270)
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                    .equals("3", ignoreCase = true)
            ) {
                realImage = rotate(realImage, 180)
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                    .equals("0", ignoreCase = true)
            ) {
                realImage = rotate(realImage, 90)
            }
            val bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            (findViewById(R.id.imageview) as ImageView).setImageBitmap(realImage)
            Log.d("Info", bo.toString() + "")
        } catch (e: FileNotFoundException) {
            Log.d("Info", "File not found: " + e.getMessage())
        } catch (e: IOException) {
            Log.d("TAG", "Error accessing file: " + e.message)
        }







        val pictureFile = File(output_file_name)
        if (pictureFile.exists()) {
            pictureFile.delete()
        }

        */





        val photoResult = fotoapparat
            .autoFocus()
            .takePicture()


        photoResult
            .saveToFile(File(
                (activity as MainActivity?)!!.getExternalFilesDir("photos"),
                "photo_$tsLong.jpg"
            ))

        println("photo name = photo_$tsLong.jpg")



        photoResult
            .toBitmap(scaled(scaleFactor = 0.25f))
            .whenAvailable { photo ->
                photo
                    ?.let {
                        Log.i(LOGGING_TAG, "New photo captured. Bitmap length: ${it.bitmap.byteCount}")
                        val uri = File(

                            (activity as MainActivity?)!!.getExternalFilesDir("photos"),
                            "photo_$tsLong.jpg"
                        ).toUri()

                        createImageCell(ImageCellData(uri, null, true))
/*

                        try {
                            val fos = FileOutputStream(File(

                                (activity as MainActivity?)!!.getExternalFilesDir("photos"),
                                "photo_$tsLong.jpg"
                            ))
                            var realImage = BitmapFactory.decodeByteArray(uri, 0, data.length)
                            val exif = ExifInterface(pictureFile.toString())
                            Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION)!!)
                            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("6", ignoreCase = true)) {
                                realImage = rotate(realImage, 90)
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                    .equals("8", ignoreCase = true)
                            ) {
                                realImage = rotate(realImage, 270)
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                    .equals("3", ignoreCase = true)
                            ) {
                                realImage = rotate(realImage, 180)
                            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                    .equals("0", ignoreCase = true)
                            ) {
                                realImage = rotate(realImage, 90)
                            }
                            val bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            fos.close()
                            (findViewById(R.id.imageview) as ImageView).setImageBitmap(realImage)
                            Log.d("Info", bo.toString() + "")
                        } catch (e: FileNotFoundException) {
                            Log.d("Info", "File not found: " + e.getMessage())
                        } catch (e: IOException) {
                            Log.d("TAG", "Error accessing file: " + e.message)
                        }


                        */

                        /*
                        selectedUris.add(uri)

                        println("selectedUris.count = ${selectedUris.count()}")




                        val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
                        pb.visibility = View.INVISIBLE

                        val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
                        tv.visibility = View.INVISIBLE


                        val deleteBtn = cl.findViewById(R.id.image_upload_image_item_delete_btn) as Button
                        //deleteBtn.visibility = View.INVISIBLE
                        //deleteBtn.alpha = 0.25f



                        deleteBtn.setOnClickListener {
                            println("delete")
                            selectedUris.removeAt(selectedUris.count() - 1)
                            binding.imageUploadPrepSelectedImagesLl.removeView(cl)
                        }




                        binding.imageUploadPrepSelectedImagesLl.addView(cl)

                        println("cl.findViewById(R.id.image_upload_image_item_iv) = ${cl.findViewById(R.id.image_upload_image_item_iv) as ImageView}")
                        Glide.with(this).load(uri).into(cl.findViewById(R.id.image_upload_image_item_iv) as ImageView)


                         */


                        //cl.requestFocus()
                       // var image_upload_prep_selected_images_sv = cl.findViewById(R.id.image_upload_prep_selected_images_sv) as ScrollView
                        binding.imageUploadPrepSelectedImagesSv.fullScroll(View.FOCUS_DOWN)


                    }
                    ?: Log.e(LOGGING_TAG, "Couldn't capture photo.")
            }







    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Untested WIP
    private fun handleRotation(imgPath: String) {
        val bMap = BitmapFactory.decodeFile(imgPath) ?: return
        try {
            val ei = ExifInterface(imgPath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED)

            val rotatedBitmap: Bitmap = when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> {
                    bMap
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotate(bMap, 90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotate(bMap, 180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotate(bMap, 270f)
                }
                else -> bMap
            }

            //Update the input file with the new bytes.
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(imgPath)
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) // bmp is your Bitmap instance
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        } catch (e: IOException) {
        }

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("onRequestPermissionsResult")
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            permissionsGranted = true
            fotoapparat.start()
            //adjustViewsVisibility()
            cameraView.visibility = View.VISIBLE
            captureBtn.visibility = View.VISIBLE

            binding.imageUploadPrepFooterCl.visibility = View.GONE
        }
    }






    private fun launchGallery() {
        println("launchGallery")


        var maxSelect = 20
        if (mode == "EQUIPMENT") {maxSelect = 1}

        BottomSheetImagePicker.Builder(getString(R.string.file_provider))


            .cameraButton(ButtonType.None)
            .galleryButton(ButtonType.None)//style of the camera link (Button in header, Image tile, None)

            .columnSize(R.dimen.columnSize)
            .multiSelect(1, maxSelect)//style of the gallery link

            .requestTag("multi")
            //tag can be used if multiple pickers are used
            .show(childFragmentManager)




    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        //toast("Result from tag: $tag")
        //selectedUris = uris
        //imageView.removeAllViews()
        uris.forEach { uri ->
            println("add uri to selected uri = $uri")

            createImageCell(ImageCellData(uri, null, true))


        }
    }






    @Throws(IOException::class)
    private fun createImageData(uri: Uri): ByteArray? {

            var imageData:ByteArray? = null
        // val inputStream = ((activity as AppCompatActivity).contentResolver
        val inputStream =  ((activity as AppCompatActivity).contentResolver.openInputStream(uri))

        inputStream?.buffered()?.use {

            imageData = it.readBytes()

        }

        return imageData
    }

    private fun deleteImage(cl: ConstraintLayout){
        //Lead Task

        println("save task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/imageLink.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        imageCellMap.remove(cl)
                        binding.imageUploadPrepSelectedImagesLl.removeView(cl)
                    }
                    hideProgressView()

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
                params["type"] = "1"
                params["ID"] = taskID
                params["imageID"] = imageCellMap[cl]!!.imageData!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveLeadTask() {
    println("Save Lead Task")
    showProgressView()

    var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/leadTask.php"
    val currentTimestamp = System.currentTimeMillis()
    println("urlString = ${"$urlString?cb=$currentTimestamp"}")
    urlString = "$urlString?cb=$currentTimestamp"
    val postRequest1: StringRequest = object : StringRequest(
        POST, urlString,
        Response.Listener { response -> // response
            println("Response $response")
            try {
                val parentObject = JSONObject(response)
                println("parentObject = $parentObject")
                if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                    val gson = GsonBuilder().create()


                    taskID = gson.fromJson(parentObject["leadTaskID"].toString() , String::class.java)

                    // If the lead tasks has no images, just pop back now
                    if (imageCellMap.isNotEmpty()) {
                        uploadImage()
                    }
                    else {
                        globalVars.playSaveSound(myView.context)
                        myView.findNavController().navigateUp()
                    }
                }


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
            params["leadID"] = leadID
            params["taskID"] = taskID
            params["taskDescription"] = taskDescription
            params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
            println("params = $params")
            return params
        }
    }
    postRequest1.tag = "imageUpload"
    VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveTask() {
        println("Save Task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/task.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()


                        taskID = gson.fromJson(parentObject["newID"].toString() , String::class.java)

                        // If the lead tasks has no images, just pop back now
                        if (imageCellMap.isNotEmpty()) {
                            uploadImage()
                        }
                        else {
                            globalVars.playSaveSound(myView.context)
                            myView.findNavController().navigateUp()
                        }
                    }


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
                if (taskID.isBlank() || taskID == "0") {
                    params["ID"] = "0"
                    params["status"] = "1"
                }
                else {
                    params["ID"] = taskID
                    params["status"] = taskStatus
                }
                params["task"] = taskDescription
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["woItemID"] = woItemID
                params["woID"] = woID


                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveContractTask() {
        println("Save Contract Task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contractTask.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()


                        taskID = gson.fromJson(parentObject["taskID"].toString() , String::class.java)

                        setFragmentResult("contractTaskItemListener", bundleOf("shouldRefreshContractItem" to true))

                        // If the lead tasks has no images, just pop back now
                        if (imageCellMap.isNotEmpty()) {
                            uploadImage()
                        }
                        else {
                            globalVars.playSaveSound(myView.context)
                            myView.findNavController().navigateUp()
                        }
                    }


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
                if (taskID.isBlank() || taskID == "0") {
                    params["taskID"] = "0"
                }
                else {
                    params["taskID"] = taskID
                }
                params["contractItemID"] = contractItemID
                params["taskDescription"] = taskDescription
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID

                /*
                "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!,
                "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                "taskID":"0",
                "contractItemID":self.contractItemID,
                "taskDescription": groupDescString as AnyObject,
                "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId) as AnyObject]

                "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!,
                "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                "taskID":self.contractTaskID,
                "contractItemID":self.contractItemID,
                "taskDescription": groupDescString as AnyObject,
                "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId) as AnyObject]
                */

                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun uploadImage() {
        //println("uploadImage selectedUris count = ${selectedUris.count()}")
        //imageData?: return

        // If equipment, failsafe that there's only one image
        /*
        if (mode == "EQUIPMENT") {
            println("checking to remove more than 1 image...")
            while (map.count() > 1) {

                imageCellDataList.removeAt(imageCellDataList.count()-1)

                //val selectedUrisNew = mutableListOf<Uri>()
                //selectedUrisNew.add(selectedUris.first())
                //selectedUris = selectedUrisNew
            }
        }

         */

        //showProgressView()

        uploadedImageCount = 0


        uploadUri(0)

    }

    private fun uploadUri(index:Int) {
        println("uploadUri")
        val clList = mutableListOf<ConstraintLayout>()
        for (entry in imageCellMap) {
            clList.add(entry.key)
        }

        val uri:Uri? = imageCellMap[clList[index]]!!.uri

        if (uri == null || !imageCellMap[clList[index]]!!.toBeSaved) {
            println("uri was either null or not marked to be uploaded")

            if (imageCellMap[clList[index]]!!.imageData != null) {
                // try the imagedata instead (will be updated if description was changed)
                updateExistingImage(index)
            }
            else {

                uploadedImageCount += 1

                if (uploadedImageCount == imageCellMap.count()) {
                    //upload complete
                    globalVars.playSaveSound(myView.context)
                    back()
                } else {
                    uploadUri(index + 1)
                }
            }
        }
        else {

            println("uploading an uri with index $index")
            uri.path?.let { handleRotation(it) }
            println("views = ${binding.imageUploadPrepSelectedImagesLl.childCount}")

            for (cl in binding.imageUploadPrepSelectedImagesLl.children) {
                println(cl.id)
            }

            val cl = clList[index]
            val iv = cl.findViewById(R.id.image_upload_image_item_iv) as ImageView
            val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
            val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
            val et = cl.findViewById(R.id.description_et) as EditText
            println("pb = $pb")
            iv.alpha = 0.5F
            pb.visibility = View.VISIBLE


            //create request
            val request = object : VolleyFileUploadRequest(
                POST,
                postURL,
                Response.Listener {

                    println("got image request back")

                    println("response is: ${it.headers}")
                    println("data is:${it.data}")

                    pb.visibility = View.INVISIBLE
                    tv.visibility = View.VISIBLE

                    uploadedImageCount += 1

                    if (uploadedImageCount == imageCellMap.count()) {
                        //upload complete
                        globalVars.playSaveSound(myView.context)
                        back()
                    } else {
                        uploadUri(index + 1)
                    }

                    //hideProgressView()
                    //back()
                },
                Response.ErrorListener {
                    //hideProgressView()
                    println("error is: $it")
                }

            ) {
                override fun getByteData(): MutableMap<String, FileDataPart> {
                    println("getByteData")
                    val params = HashMap<String, FileDataPart>()
                    // createImageData(uri)
                    params["pic"] = FileDataPart("droid_file.jpeg", createImageData(uri!!)!!, "image/jpeg")
                    return params
                }


                override fun getParams(): Map<String, String> {
                    println("getParams")

                    val params: MutableMap<String, String> = HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["name"] = "$mode Image"
                    params["desc"] = et.text.trim().toString()
                    params["tags"] = ""
                    params["noCompress"] = uncompressed
                    if (customerID != "") {
                        params["customer"] = customerID
                    }
                    params["createdBy"] = GlobalVars.loggedInEmployee!!.ID

                    when (mode) {
                        "LEADTASK" -> {
                            params["leadTask"] = taskID
                            params["contractTask"] = ""
                            params["task"] = ""
                        }

                        "CONTRACTTASK" -> {
                            params["leadTask"] = ""
                            params["contractTask"] = taskID
                            params["task"] = ""
                        }

                        "EQUIPMENT" -> {
                            params[equipmentID] = equipmentID
                            params["leadTask"] = ""
                            params["contractTask"] = ""
                        }

                        else -> {
                            params["leadTask"] = ""
                            params["contractTask"] = ""
                            params["task"] = taskID
                        }
                    }


                    params["woID"] = woID

                    if (mode == "WOITEM") {
                        params["usageID"] = usageID
                        params["name"] = "Receipt Image"
                    }


                    params["equipmentID"] = equipmentID
                    //params["albumID"] = albumID
                    //params["usageID"] = usageID
                    //params["vendorID"] = vendorID
                    //params["strikeID"] = strikeID
                    println("params is: $params")
                    return params
                }

            }

            request.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            //send request
            request.tag = "imageUpload"
            VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cache.clear()
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(request)

        }
    }

    private fun updateExistingImage(index:Int) {
        println("updateExistingImage")
        val clList = mutableListOf<ConstraintLayout>()
        for (entry in imageCellMap) {
            clList.add(entry.key)
        }

        val image:Image? = imageCellMap[clList[index]]!!.imageData

        if (image == null) {
            uploadedImageCount += 1

            if (uploadedImageCount == imageCellMap.count()) {
                //upload complete
                globalVars.playSaveSound(myView.context)
                back()
            } else {
                uploadUri(index + 1)
            }
        }

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/image.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response

                println("update image response: $response")

                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        uploadedImageCount += 1
                        if (uploadedImageCount == imageCellMap.count()) {
                            //upload complete
                            globalVars.playSaveSound(myView.context)
                            back()
                        } else {
                            uploadUri(index + 1)
                        }
                    }

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
                params["ID"] = image!!.ID
                params["createdBy"] = image.createdBy?:""
                params["equipmentID"] = image.equipmentID?:""
                params["task"] = image.taskID?:""
                params["name"] = image.name
                params["desc"] = image.description!!.trim()
                params["customer"] = image.customer?:""
                params["vendor"] = image.vendorID?:""
                params["tags"] = image.tags?:""
                params["noCompress"] = image.noCompress?:""
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("update image params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    private fun getAllowImages(newCustomerID:String, selectedFromRecycler:Boolean) {

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customer.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    val customerArray = gson.fromJson(parentObject.toString() ,CustomerArray::class.java)

                    val customer = customerArray.customers[0]

                    if (customer.allowImages == "0") {
                        customerAllowImages = false
                    }

                    if (selectedFromRecycler) {
                        if (customerAllowImages) {
                            customerID = newCustomerID
                            customerSearchView.setQuery(queriedCustomer!!.sysname, false)
                            customerRecyclerView.visibility = View.GONE

                            //val imm = (activity as MainActivity?).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            //imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
                            hideSoftKeyboard((activity as MainActivity?)!!)


                            println("selcted cust = ${queriedCustomer!!.ID}")
                        }
                        else {
                            globalVars.simpleAlert(myView.context,getString(R.string.no_image_collection),getString(R.string.no_image_collection_error))
                            customerAllowImages = true
                            customerID = ""
                            customerSearchView.setQuery("", false)
                            customerRecyclerView.visibility = View.GONE
                        }
                    }
                    hideProgressView()

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
                //params["ID"] = customer!!.ID
                params["ID"] = newCustomerID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onCustomerCellClickListener(data:Customer) {
        println("Cell clicked with customer: ${data.sysname}")
        data.let {
            // val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            queriedCustomer = it
            getAllowImages(it.ID, true)
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        if (activity.currentFocus == null){
            return
        }
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }


    fun back(){
        println("back")

        // Flag to refresh new/edit equipment if mode is equipment
        setFragmentResult("_refreshEquipment", bundleOf("_refreshEquipment" to (mode == "EQUIPMENT")))
        setFragmentResult("_refreshImages", bundleOf("_refreshImages" to true))

        //requireActivity().supportFragmentManager.popBackStack()
        myView.findNavController().navigateUp()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    /*
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImageUpload.
         */

       private const val IMAGE_PICK_CODE = 999

        @JvmStatic
        fun newInstanceFromCustomer(customer: Customer) =
            ImageUploadFragment().apply {
                println("newInstanceFromCustomer")


                arguments = Bundle().apply {
                    putString(mode, "CUSTOMER")
                    putParcelable("customer",customer)
                }
            }
    }

     */

}



/*
private sealed class Camera(
    val lensPosition: LensPositionSelector,
    val configuration: CameraConfiguration
) {

    object Back : Camera(
        lensPosition = back(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                continuousFocusPicture(),
                autoFocus()
            ),
            frameProcessor = {
                // Do something with the preview frame
            }
        )
    )

    object Front : Camera(
        lensPosition = front(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                fixed(),
                autoFocus()
            )
        )
    )
}

*/
