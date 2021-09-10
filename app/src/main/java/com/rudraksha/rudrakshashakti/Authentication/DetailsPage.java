package com.rudraksha.rudrakshashakti.Authentication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rudraksha.rudrakshashakti.Common.MainActivity;
import com.rudraksha.rudrakshashakti.Common.ReconnectPage;
//import com.rudraksha.rudrakshashakti.Common.SplashScreen;
import com.rudraksha.rudrakshashakti.Utilities.InternetConnection;
import com.rudraksha.rudrakshashakti.Utilities.MyProgressDialog;
import com.rudraksha.rudrakshashakti.Utilities.Utilities;
import com.rudraksha.rudrakshashakti.databinding.ActivityDetailsPageBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class DetailsPage extends AppCompatActivity implements View.OnClickListener{

    ActivityDetailsPageBinding aBinding;

    FirebaseFirestore database;
    private Uri imageUri;
    private StorageReference storageReference;

    private MyProgressDialog myProgressDialog;

    FirebaseAuth mAuth;

    String name,dateOfBirth,state,city,Profile_Pic_Uri,uid,gender;


    private MyProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        reconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and storage
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseFirestore.getInstance();
        uid = mAuth.getUid();

        //Incorporating View Binding
        aBinding = ActivityDetailsPageBinding.inflate(getLayoutInflater());
        View view = aBinding.getRoot();
        //Setting content view to Root view
        setContentView(view);
        setListeners();


        //set states array list
        setStates();
        setGender();
    }

    /**Reconnects and also checks internet connection*/
    public void reconnect() {
        progressDialog = new MyProgressDialog();
        progressDialog.showDialog(this);
        if (InternetConnection.checkConnection(this)) {
            progressDialog.dismissDialog();

        } else {
            progressDialog.dismissDialog();
            Intent intent = new Intent(this, ReconnectPage.class);
            startActivity(intent);
        }
    }


    /**
     * set Gender Array adapter*/
    private void setGender() {
        aBinding.gender.setThreshold(0);
        final String[] genders = new String[]{"Male" , "Female" , "Other"};
        ArrayAdapter<String> gender = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, genders);
        aBinding.gender.setAdapter(gender);
    }

    /**
     * set State Array adapter*/
    private void setStates() {
        aBinding.inputStateOfBirth.setThreshold(1);
        final String[] states = new String[]{"Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat","Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madhya Pradesh","Maharashtra","Manipur","Meghalaya","Mizoram","Nagaland","Odisha","Punjab","Rajasthan","Sikkim","Tamil Nadu","Telangana","Tripura","Uttarakhand","Uttar Pradesh","West Bengal","Andaman and Nicobar Islands","Chandigarh","Dadra and Nagar Haveli","Daman and Diu","Delhi","Lakshadweep","Puducherry"};
        ArrayAdapter<String> state = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, states);
        aBinding.inputStateOfBirth.setAdapter(state);
    }


    private void setListeners() {
        aBinding.nextBtn.setOnClickListener(this);
        aBinding.backBtn.setOnClickListener(this);
        aBinding.chooseImage.setOnClickListener(this);
        aBinding.inputDateOfBirth.setOnClickListener(this);
        aBinding.inputStateOfBirth.setOnClickListener(this);
        aBinding.gender.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == aBinding.nextBtn) {
            next();
        } else if (view == aBinding.backBtn) {
            back();
        } else if (view == aBinding.chooseImage) {
            choosePicture();
        }else if (view == aBinding.inputDateOfBirth){
            selectDate();
        }else if(view == aBinding.inputStateOfBirth){
            selectState();
        }else if(view == aBinding.gender){
            selectGender();
        }
    }

    /**
     * It will save all users details and upload it to firestore*/
    private void next() {
//        getDetails();
//        if (name.equals("") || dateOfBirth.equals("") || gender.equals("") || state.equals("") || city.equals("")) {
//            Utilities.makeToast("Enter all Required details", getApplicationContext());
//        }else{
//
//            //compressAndUploadDetails();
//        }
        SendToHomeActivity();
    }

    /**
     * works as a back function */
    private void back() {
        Intent intent = getIntent();
        boolean fromSplashScreen = intent.getExtras().getBoolean("fromSplashScreen");
        if(fromSplashScreen){
            this.finish();
        }else{
            mAuth.signOut();
            this.finish();
        }
    }



    /**
     * It opens the images in gallery and allows to set profile image
     * */
    private void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }


    /**
     * On activity result it sets Profile image from gallery
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();
            aBinding.profilePic.setImageURI(imageUri);
        }
    }


    /**
     * It will upload the image to firebase storage and then it gets the storage url and calls the function to set users details in firestore
     * */
    private void compressAndUploadDetails(){
        myProgressDialog = new MyProgressDialog();
        if(imageUri != null){
           uploadImage();
        }else{
            Utilities.makeToast("Please Select a profile image", this);
        }
    }


    /**
     * Uploads the image to firebase after reducing the size*/
    private void uploadImage() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] final_profile_image = baos.toByteArray();
            myProgressDialog.showDialog(this);
            storageReference.child("UsersProfilePhoto/").child(uid+"_profile_pic").putBytes(final_profile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.child("UsersProfilePhoto/").child(uid+"_profile_pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Profile_Pic_Uri = uri.toString();
                            Utilities.makeToast("Upload image sucessfull!!", DetailsPage.this);
                            UploadInFirestore();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Utilities.makeToast("Sorry image can't be uploaded try again!!", DetailsPage.this);
                        }
                    });
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Get all the input values by users and store it in string*/
    private void getDetails(){
         name = aBinding.inputName.getText().toString();
         dateOfBirth = aBinding.inputDateOfBirth.getText().toString();
         gender = aBinding.gender.getText().toString();
         state = aBinding.inputStateOfBirth.getText().toString();
         city = aBinding.inputCityOfBirth.getText().toString();
    }



    /**
     * opens a date selector popup dialog*/
    private void selectDate(){
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsPage.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth+" / "+month+" / "+year;
                aBinding.inputDateOfBirth.setText(date);
            }
        }, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    /**
     * opens a state selector menu list*/
    private void selectState(){
        aBinding.inputStateOfBirth.showDropDown();
    }

    /**
     * opens a gender selector menu list*/
    private void selectGender(){aBinding.gender.showDropDown();}


    /**
     * Posts users details in firestore*/
    public void UploadInFirestore() {
        Map<String, String> data = new HashMap<>();
        data.put("uid",uid);
        data.put("name", name);
        data.put("photoUrl",Profile_Pic_Uri);
        data.put("dateOfBirth",dateOfBirth);
        data.put("gender", gender);
        data.put("state",state);
        data.put("city",city);
        String url = "https://us-central1-cosmic-solutions-7388c.cloudfunctions.net/usersDetail\n";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        boolean jsonResponse = true;
                        try {
                            jsonResponse = response.getBoolean("success");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("TAG", String.valueOf(jsonResponse));
                        if (jsonResponse) {
                            SendToHomeActivity();
                        }
                        myProgressDialog.dismissDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // enjoy your error status
            }
        });

        queue.add(request);
    }




    /**
     * send user to main home page*/
    private void SendToHomeActivity() {
//        SplashScreen.encrypt.putString("details_filled", "true");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }



}