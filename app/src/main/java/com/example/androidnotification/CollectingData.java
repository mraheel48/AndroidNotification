package com.example.androidnotification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.HashMap;

public class CollectingData extends AppCompatActivity {

    EditText edtName, edtContact, edtDescribing;
    CardView cardSave, cardLogout;
    ImageView imageView;
    Uri uri, imguri;
    Boolean st = false;

    String imageUri, stType, softwareStatu;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference mStorageRef;
    FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    Spinner spType;
    String[] typeList = {"Pizza", "Restaurant", "Shop"};

    Switch swSoftware;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collecting_data);

        edtName = findViewById(R.id.edtName);
        edtContact = findViewById(R.id.edtContact);
        edtDescribing = findViewById(R.id.edtDescribing);
        cardSave = findViewById(R.id.cartSave);
        imageView = findViewById(R.id.imgCrop);
        spType = findViewById(R.id.spType);
        swSoftware = findViewById(R.id.swSoftware);
        cardLogout = findViewById(R.id.cardLogout);

        //progress dialog
        progressDialog = new ProgressDialog(CollectingData.this);

        mAuth = FirebaseAuth.getInstance();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.startPickImageActivity(CollectingData.this);
            }
        });

        cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (st) {

                    //Toast.makeText(CollectingData.this, "Data save ", Toast.LENGTH_SHORT).show();
                    if (edtName.getText().toString().isEmpty()) {

                        edtName.setError("Required Field...");
                        edtName.requestFocus();
                        return;
                    }

                    if (edtContact.getText().toString().isEmpty()) {

                        edtContact.setError("Required Field...");
                        edtContact.requestFocus();
                        return;
                    }

                    if (edtDescribing.getText().toString().isEmpty()) {

                        edtDescribing.setError("Required Field...");
                        edtDescribing.requestFocus();
                        return;
                    }


                    dataupload();


                } else {

                    Toast.makeText(CollectingData.this, "Plz select the img first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //spinner code
        ArrayAdapter apCourse = ArrayAdapter.createFromResource(
                this,
                R.array.type,
                R.layout.color_spinner_layout
        );
        apCourse.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spType.setAdapter(apCourse);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                stType = typeList[parent.getSelectedItemPosition()];

                //Toast.makeText(CollectingData.this, stType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
                finish();
                startActivity(new Intent(CollectingData.this, Login.class));
            }
        });

    }

    private void dataupload() {

        if (swSoftware.isChecked()) {

            softwareStatu = "Yes";

        } else {

            softwareStatu = "No";
        }

        //Toast.makeText(this, "Data is upload", Toast.LENGTH_SHORT).show();

        mStorageRef = FirebaseStorage.getInstance().getReference().child("HotelImage");

        progressDialog.setMessage("Uploading Image ... ");
        progressDialog.show();
        progressDialog.setCancelable(false);

        final StorageReference filepath = mStorageRef.child(imguri.getLastPathSegment() + ".jpg");
        final UploadTask uploadTask = filepath.putFile(imguri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()) {

                            throw task.getException();
                        }

                        imageUri = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        imageUri = task.getResult().toString();

                        HashMap<String, Object> data = new HashMap<>();

                        data.put("uid", mAuth.getCurrentUser().getUid());
                        data.put("img", imageUri);
                        data.put("name", edtName.getText().toString().trim());
                        data.put("contact", edtContact.getText().toString().trim());
                        data.put("description", edtDescribing.getText().toString().trim());
                        data.put("type", stType);
                        data.put("status", softwareStatu);
                        data.put("date", new Timestamp(new Date()));

                        db.collection("CollectingData").document()
                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                progressDialog.dismiss();
                                Toast.makeText(CollectingData.this, "Data is Save", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CollectingData.this, CollectingData.class));

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                Toast.makeText(CollectingData.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(CollectingData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            Uri ImageUri = CropImage.getPickImageResultUri(this, data);

            if (CropImage.isReadExternalStoragePermissionsRequired(this, ImageUri)) {

                uri = ImageUri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            0);
                }

            } else {

                startCrop(ImageUri);

            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                imageView.setImageURI(result.getUri());
                imguri = result.getUri();
                Toast.makeText(this, "Image Upload Successfully !!!", Toast.LENGTH_SHORT).show();
                st = true;
            }
        }

    }

    private void startCrop(Uri imageUri) {

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    public void onBackPressed() {
        //super.onBackPressed();

        AlertDialog.Builder builder = new AlertDialog.Builder(CollectingData.this);

        builder.setCancelable(false);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //super.onBackPressed();
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
