package com.example.medmate;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class DetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewTitle;
    private TextView textViewDescription;
    private DatabaseReference databaseReference;
    private String dataId;
    private Data data;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.imageViewDetail);
        textViewTitle = findViewById(R.id.textViewDetailTitle);
        textViewDescription = findViewById(R.id.textViewDetailDescription);
        Button buttonEdit = findViewById(R.id.buttonEdit);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        FloatingActionButton buttonDownload = findViewById(R.id.buttonDownload);

        databaseReference = FirebaseDatabase.getInstance().getReference("data");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("dataId")) {
            dataId = intent.getStringExtra("dataId");
            if (dataId != null) {
                loadData();
            } else {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonEdit.setOnClickListener(view -> {
            if (data != null) {
                Intent intent1 = new Intent(DetailActivity.this, EditActivity.class);
                intent1.putExtra("dataId", dataId);
                intent1.putExtra("imageUrl", data.getImageUrl());
                intent1.putExtra("title", data.getTitle());
                intent1.putExtra("description", data.getDescription());
                startActivity(intent1);
            }
        });

        buttonDelete.setOnClickListener(view -> deleteData());

        buttonDownload.setOnClickListener(view -> {
            if (data != null && data.getImageUrl() != null) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(data.getImageUrl());

                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    downloadImage(imageUrl);
                }).addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Failed to get image download URL", Toast.LENGTH_SHORT).show());
            }
        });

    }

    private void loadData() {
        DatabaseReference dataRef = databaseReference.child(dataId);
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    data = dataSnapshot.getValue(Data.class);

                    if (data != null) {
                        textViewTitle.setText(data.getTitle());
                        textViewDescription.setText(data.getDescription());

                        RequestOptions requestOptions = new RequestOptions()
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background);

                        // Load gambar
                        Glide.with(DetailActivity.this)
                                .load(data.getImageUrl())
                                .apply(requestOptions)
                                .into(imageView);
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void deleteData() {
        DatabaseReference dataRef = databaseReference.child(dataId);
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    data = dataSnapshot.getValue(Data.class);
                    if (data != null && data.getImageUrl() != null) {
                        // Menghapus data dari Firebase Realtime Database
                        dataRef.removeValue().addOnSuccessListener(aVoid -> {
                            // Menghapus data gambar dari Firebase Storage
                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(data.getImageUrl());
                            imageRef.delete().addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(DetailActivity.this, "Data and image deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show());
                        }).addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Failed to delete data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void downloadImage(String imageUrl) {
        // MediaStore API untuk download and save the image
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        } else {
            Toast.makeText(this, "Failed to initialize download manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
