package com.example.evoid

import android.app.Activity
import android.telephony.SmsManager
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class firestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()
    var location: locationDetails? = null
    fun getLoggedInUser(activity:Activity) {
        val navigationView : NavigationView = activity.findViewById(R.id.nav_view)
        val headerView : View = navigationView.getHeaderView(0)
        var image = headerView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileIconNav)
        val name = headerView.findViewById<TextView>(R.id.userDetails)

        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .get().addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                updateNavBar(activity, loggedInUser, name, image)
            }
    }

    fun updateNavBar(activity: Activity, user:User, name: TextView , image:de.hdodenhof.circleimageview.CircleImageView)
    {

        Glide
            .with(activity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.cybercrime)
            .into(image)
        name.text = user.firstName + " " + user.lastName


    }

    fun registerUser(activity:RegisterUser, userInfo:User)
    {
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "registered", Toast.LENGTH_SHORT).show()
            }
    }



    fun getCurrentUserId():String
    {
        val uid = Firebase.auth.currentUser!!.uid
        return uid


    }

    fun addContact(contactInfo:ContactsDetails, phoneId:String)
    {
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .collection(constants.ContactsDetails)
            .document(phoneId)
            .set(contactInfo, SetOptions.merge())
    }

    fun updateLocation(location: locationDetails)
    {
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .collection(constants.locationDetails)
            .document(getCurrentUserId())
            .set(location, SetOptions.merge())
    }

    fun getLocation()
    {
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .collection(constants.locationDetails)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document->
                location = document.toObject(locationDetails::class.java)!!
            }
    }


    fun getContacts(activity: Activity){
        getLocation()
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .collection(constants.ContactsDetails)
            .get()
            .addOnSuccessListener { results ->

                for (documents in results) {
                    val currContact = documents.get("contactPhoneNumber").toString()
                    val mySmsManager = SmsManager.getDefault()

                    mySmsManager.sendTextMessage(
                        currContact, null, constants.msg + "\n" +
                                "http://maps.google.com/maps?daddr=${location!!.lattitude.toDouble()},${location!!.longitude.toDouble()}",
                        null, null)
                    val handle = android.os.Handler()
                    handle.postDelayed({ print("")},500)
                }

                Toast.makeText(activity, "Location sent", Toast.LENGTH_SHORT).show()

            }

    }

    fun loadMyProfile(activity: Activity, img:CircleImageView, email: EditText, name:EditText, mobile:EditText)
    {
        var loggedInUser:User
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .get().addOnSuccessListener { document ->
                loggedInUser = document.toObject(User::class.java)!!
                Glide
                    .with(activity)
                    .load(loggedInUser.image)
                    .fitCenter()
                    .placeholder(R.drawable.placeholder3)
                    .into(img);
                email.setText(loggedInUser.emailId)
                name.setText(loggedInUser.firstName +" "+ loggedInUser.lastName)
                mobile.setText(loggedInUser.phoneNumber.toString())
            }

    }

    fun updateMyProfile(name:String, mobile: String)
    {

        var nameParts = name.split(" ")
        mFireStore.collection(constants.USERS)
            .document(getCurrentUserId())
            .update(mapOf(
                "firstName" to nameParts[0],
                "lastName" to nameParts[1],
                "phoneNumber" to mobile.toLong(),
            ))
    }

    fun updateFirestoreImage(imgUrl:String)
    {
        mFireStore.collection(constants.USERS).document(getCurrentUserId()).update(mapOf(
            "image" to imgUrl
        ))
    }


}



