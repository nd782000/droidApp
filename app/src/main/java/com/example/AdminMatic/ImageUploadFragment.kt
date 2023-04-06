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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
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


private const val LOGGING_TAG = "AdminMatic"

private  var mode: String = "GALLERY"
private  var images: Array<Image> = arrayOf()
private  var customerID: String = ""
private  var customerName: String = ""
private  var woID: String = ""
private  var woItemID: String = ""
private  var leadID: String = ""
private  var taskID: String = ""
private  var taskDescription: String = ""
private  var employeeID: String = ""
private  var equipmentID: String = ""
private  var usageID: String = ""

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
    var selectedUris: MutableList<Uri> = mutableListOf()
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


            images = it.getParcelableArray("images")!! as Array<Image>
            customerID = it.getString("customerID")!!
            customerName = it.getString("customerName")!!
            woID = it.getString("woID")!!
            woItemID = it.getString("woItemID")!!
            leadID = it.getString("leadID")!!
            taskID = it.getString("taskID")!!
            taskDescription = it.getString("taskDescription")!!
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

            println("images.count = ${images.count()}")
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

            launchCamera()
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
                    //uploadImage()
                }
                "WOITEM" -> {
                    uploadImage()
                }
                "LEADTASK" -> {
                    if(taskID == "0"){
                        //create task
                        saveTask()
                    }else{
                        uploadImage()
                    }
                    //titleText = "Upload to Lead Task"
                }
                "CONTRACTTASK" -> {
                    //titleText = "Upload to Task"
                    uploadImage()
                }
                "TASK" -> {
                    //titleText = "Upload to Task"
                    uploadImage()
                }
                "EMPLOYEE" -> {
                    //titleText = "Upload to Employee"
                }
                "EQUIPMENT" -> {
                    //titleText = "Upload to Equipment"
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
            //cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )
       // println(" fotoapparat.getCurrentParameters() = ${fotoapparat.getCurrentParameters()}")


    }

    override fun onStart() {
        super.onStart()
        fotoapparat.start()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat.stop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageUpload")
    }


    private fun launchCamera(){
        println("launchCamera")

        permissionsGranted = permissionsDelegate.hasCameraPermission()

        if (permissionsGranted) {
             cameraView.visibility = View.VISIBLE
            captureBtn.visibility = View.VISIBLE
            binding.imageUploadPrepFooterCl.visibility = View.GONE

        } else {
            permissionsDelegate.requestCameraPermission()
        }

    }

    private fun takePicture(){


        val cl = LayoutInflater.from(this.context).inflate(R.layout.image_upload_image_list_item, binding.imageUploadPrepSelectedImagesLl, false) as ConstraintLayout
        cl.id = selectedUris.count()


        cameraView.visibility = View.GONE
        captureBtn.visibility = View.GONE
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


        BottomSheetImagePicker.Builder(getString(R.string.file_provider))


            .cameraButton(ButtonType.None)
            .galleryButton(ButtonType.None)//style of the camera link (Button in header, Image tile, None)

            .columnSize(R.dimen.columnSize)
            .multiSelect(1,20)//style of the gallery link

            .requestTag("multi")
            //tag can be used if multiple pickers are used
            .show(childFragmentManager)




    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        //toast("Result from tag: $tag")
        //selectedUris = uris
        //imageView.removeAllViews()
        var count:Int = selectedUris.count()
        uris.forEach { uri ->
            println("add uri to selected uri = $uri")

            selectedUris.add(uri)

            val cl = LayoutInflater.from(this.context).inflate(R.layout.image_upload_image_list_item, binding.imageUploadPrepSelectedImagesLl, false) as ConstraintLayout
            cl.id = count

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

            //cl.requestFocus()

            //image_upload_prep_selected_images_ll.fullScroll(View.FOCUS_DOWN)
            binding.imageUploadPrepSelectedImagesSv.fullScroll(View.FOCUS_DOWN)


            count += 1

            //val iv = LayoutInflater.from(this.context).inflate(R.layout.image_upload_item, image_upload_prep_selected_images_ll, false) as ImageView
           // image_upload_prep_selected_images_ll.addView(iv)
           // Glide.with(this).load(uri).into(iv)
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



private  fun saveTask(){
    //Lead Task

    println("save task")
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
                    if (selectedUris.size > 0) {
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

    private fun uploadImage() {
        println("uploadImage selectedUris count = ${selectedUris.count()}")
        //imageData?: return


        showProgressView()

        uploadedImageCount = 0


        //disable buttons

        var count = 0
        selectedUris.forEach { uri ->
            uri.path?.let { handleRotation(it) }
            println("uri# $count")
            println("views = ${binding.imageUploadPrepSelectedImagesLl.childCount}")
            val cl = binding.imageUploadPrepSelectedImagesLl.findViewById<ConstraintLayout>(count)
            val iv = cl.findViewById(R.id.image_upload_image_item_iv) as ImageView
            val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
            val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
            println("pb = $pb")
            iv.alpha = 0.5F
            pb.visibility = View.VISIBLE



            //create request
            val request = object : VolleyFileUploadRequest(
                POST,
                postURL,
                Response.Listener {

                    println("response is: ${it.headers}")
                    println("data is:${it.data}")

                    pb.visibility = View.INVISIBLE
                    tv.visibility = View.VISIBLE

                    uploadedImageCount += 1

                    if (uploadedImageCount == selectedUris.count()){
                        //upload complete
                        globalVars.playSaveSound(myView.context)
                        back()
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
                    params["pic"] = FileDataPart("droid_file.jpeg",  createImageData(uri)!!, "image/jpeg")
                    return params
                }



                override fun getParams(): Map<String, String> {
                    println("getParams")

                    /*
                    parameters = [
                    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!,
                    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                    "name":imageData.name,
                    "desc":imageData.description,
                    "tags":"",
                    "customer":imageData.customer,
                    "createdBy":createdBy,
                    "leadTask":imageData.leadTaskID,
                    "contractTask":imageData.contractTaskID,
                    "task":imageData.taskID,

                    "woID":imageData.woID,
                    "equipmentID":imageData.equipmentID,
                    "albumID":imageData.albumID,
                    "usageID":imageData.usageID,
                    "vendorID":imageData.vendorID,
                    "strikeID":imageData.strikeID
                    ] as! [String : String]
                     */
                    val params: MutableMap<String, String> = HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["name"] = "$mode Image"
                    params["desc"] = taskDescription
                    params["tags"] = ""
                    if(customerID != ""){
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

            count += 1

        }


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

        (activity as MainActivity?)!!.refreshImages()
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
