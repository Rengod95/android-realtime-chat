package com.pages.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.Constants.AppConstants
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kau.kotlinchatapp.DashBoardActivity
import com.kau.kotlinchatapp.R
import com.kau.kotlinchatapp.databinding.FragmentGetUserDataBinding
import java.io.File


class GetUserDataFragment : Fragment() {
    private var image: Uri? = null
    private lateinit var username: String
    private lateinit var status: String
    private lateinit var imageUrl: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var binding: FragmentGetUserDataBinding

    private fun replaceFragment(fragment: Fragment):Unit{
        val fragmentManager = activity?.supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.replace(R.id.frame_layout, fragment)
        fragmentTransaction?.commitAllowingStateLoss()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference
        binding = FragmentGetUserDataBinding.inflate(inflater, container, false)


        binding.imgEditBtn.setOnClickListener {
            Toast.makeText(context,"????????? ??????",Toast.LENGTH_SHORT).show()
            selectGallery()
        }


        binding.submitBtn.setOnClickListener {
            if(checkData()){
                uploadData(username, status, image!!)
                val intent = Intent(context, DashBoardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        return binding.root
    }


    companion object{
        const val REVIEW_MIN_LENGTH = 10
        // ????????? ?????? ??????
        const val REQ_GALLERY = 1

        // API ????????? Parameter key???
        const val PARAM_KEY_IMAGE = "image"
        const val PARAM_KEY_PRODUCT_ID = "product_id"
        const val PARAM_KEY_REVIEW = "review_content"
        const val PARAM_KEY_RATING = "rating"
    }

    // ???????????? ??????????????? ?????? ??????
    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK){
            // ???????????? ????????? ImageView??? ????????????
            image = result.data?.data
            image?.let{

                // ?????? ???????????? ?????? ?????? ????????? ????????????

                // ???????????? ????????????
                Glide.with(binding.root)
                    .load(image)
                    .fitCenter()
                    .apply(RequestOptions().override(300,300))
                    .into(binding.profileImg)
            }
        }
    }


    // ???????????? ????????? ?????????
    private fun selectGallery(){
        val writePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

        //?????? ??????
        if(writePermission == PackageManager.PERMISSION_DENIED ||
            readPermission == PackageManager.PERMISSION_DENIED){
            // ?????? ??????
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)

        }else{
            // ????????? ?????? ?????? ????????? ??????
        }
        val intent = Intent(Intent.ACTION_PICK)
        // intent??? data??? type??? ????????? ???????????? ?????????
        intent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )

        imageResult.launch(intent)
    }


    private fun checkData(): Boolean {
        username = binding.nameTxt.text.toString().trim()
        status = binding.statusTxt.text.toString().trim()
        if (username.isEmpty()) {
            binding.nameTxt.error = "????????? ?????? ????????? ?????????."
            return false
        }
        if (status.isEmpty()) {
            binding.statusTxt.error = "?????? ???????????? ?????? ????????? ?????????."
            return false
        }
        if (image == null) {
            Toast.makeText(context, "????????? ???????????? ????????? ???????????? ?????????.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadData(name: String, status: String, image: Uri) = kotlin.run {
        storageReference.child(firebaseAuth.uid!! + AppConstants.PATH).putFile(image)
            .addOnCompleteListener {
                val task = it.result.storage.downloadUrl
                task.addOnCompleteListener { uri ->
                    imageUrl = uri.result.toString()
                    val map = mapOf(
                        "name" to name,
                        "status" to status,
                        "image" to imageUrl
                    )
                    databaseReference.child(firebaseAuth.uid!!).updateChildren(map)
                }


            }

    }






}