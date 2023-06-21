package com.example.medmate;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class EditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageViewEdit;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private Uri imageUri;

    private String dataId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imageViewEdit = findViewById(R.id.imageViewEdit);
        Button buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference("data");
        storageReference = FirebaseStorage.getInstance().getReference("images");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("dataId")) {
            dataId = intent.getStringExtra("dataId");
            String imageUrl = intent.getStringExtra("imageUrl");
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");

            editTextTitle.setText(title);
            editTextDescription.setText(description);

            // Load gambar menggunakan Glide
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_launcher_background) // placeholder jika gambar belum dimuat
                            .error(R.drawable.ic_launcher_background)) // gambar error jika gagal memuat
                    .into(imageViewEdit);
        } else {
            Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonChooseImage.setOnClickListener(view -> openFileChooser());

        buttonUpdate.setOnClickListener(view -> updateData());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background);

            Glide.with(this)
                    .load(imageUri)
                    .apply(requestOptions)
                    .into(imageViewEdit);
        }
    }

    private void updateData() {
        String title = Objects.requireNonNull(editTextTitle.getText()).toString().trim();
        String description = Objects.requireNonNull(editTextDescription.getText()).toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataId == null) {
            Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference dataRef = databaseReference.child(dataId);
        dataRef.child("title").setValue(title);
        dataRef.child("description").setValue(description);

        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            UploadTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String newImageUrl = uri.toString();

                // Menghapus gambar sebelumnya jika ada
                deletePreviousImage(dataRef, newImageUrl);
            })).addOnFailureListener(e -> Toast.makeText(EditActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deletePreviousImage(DatabaseReference dataRef, String newImageUrl) {
        dataRef.child("imageUrl").get().addOnSuccessListener(dataSnapshot -> {
            String previousImageUrl = dataSnapshot.getValue(String.class);
            if (previousImageUrl != null && !previousImageUrl.equals(newImageUrl)) {
                // Hapus gambar sebelumnya jika berbeda dengan gambar baru
                StorageReference previousImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(previousImageUrl);
                previousImageRef.delete().addOnSuccessListener(aVoid -> {
                    // Upload URL gambar baru ke database
                    updateImageUrl(dataRef, newImageUrl);
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditActivity.this, "Failed to update data", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Jika gambar baru sama dengan gambar sebelumnya, langsung update URL gambar di database
                updateImageUrl(dataRef, newImageUrl);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(EditActivity.this, "Failed to update data", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateImageUrl(DatabaseReference dataRef, String imageUrl) {
        dataRef.child("imageUrl").setValue(imageUrl).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(EditActivity.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(EditActivity.this, "Failed to update data", Toast.LENGTH_SHORT).show();
        });
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
