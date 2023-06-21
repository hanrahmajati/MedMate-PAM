package com.example.medmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    EditText edtNama, edtEmail, edtPassword, edtConfPassword;
    TextView txtMasuk;
    Button btnDaftar;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        txtMasuk = findViewById(R.id.txtMasuk);
        txtMasuk.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        edtNama = findViewById(R.id.edtNama);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfPassword = findViewById(R.id.edtConfPassword);
        btnDaftar = findViewById(R.id.btnDaftar);
        btnDaftar.setOnClickListener(view -> {
            String nama = edtNama.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            String confpassword = edtConfPassword.getText().toString();

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || confpassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Isi semua field", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confpassword)) {
                Toast.makeText(RegisterActivity.this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show();
            } else {
                String emailPath = email.replace(".", "_").replace("@", "_");
                databaseReference.child(emailPath).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(RegisterActivity.this, "Email ini sudah mempunyai akun", Toast.LENGTH_SHORT).show();
                        } else {
                            databaseReference.child(emailPath).child("nama").setValue(nama);
                            databaseReference.child(emailPath).child("email").setValue(email);
                            databaseReference.child(emailPath).child("password").setValue(password);

                            Toast.makeText(RegisterActivity.this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
