package com.example.medmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataAdapter.OnItemClickListener {

    private DataAdapter dataAdapter;
    private TextView txtSapaUser;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private List<Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        txtSapaUser = findViewById(R.id.txtSapaUser);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton btnTambahCatatan = findViewById(R.id.btnTambahCatatan);

        dataList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("data");

        dataAdapter = new DataAdapter(dataList);
        dataAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter);

        btnTambahCatatan.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            String email = currentUser.getEmail();
            assert email != null;
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(email);
            userRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nama = dataSnapshot.child("nama").getValue(String.class);
                        txtSapaUser.setText(nama+"!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        valueEventListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Data data = snapshot.getValue(Data.class);
                    if (data != null) {
                        data.setDataId(snapshot.getKey());
                        dataList.add(data);
                    }
                }

                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListener);
    }

    //@Override
    //protected void onStop() {
        //super.onStop();
        //databaseReference.removeEventListener(valueEventListener);
    //}

    @Override
    public void onItemClick(int position) {
        Data clickedData = dataList.get(position);
        String dataId = clickedData.getDataId();

        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("dataId", dataId);
        startActivity(intent);
    }
}
