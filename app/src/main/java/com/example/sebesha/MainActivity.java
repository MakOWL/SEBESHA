package com.example.sebesha;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button add_btn,historyBtn;
    private List<Device> deviceList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    Button estimateBtn;
    TextView estimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        estimateBtn = findViewById(R.id.estimate_btn);
        estimation = findViewById(R.id.estimatedBill);
        historyBtn = findViewById(R.id.history_button);
        historyBtn = findViewById(R.id.history_button);


        add_btn = findViewById(R.id.add_new);
        recyclerView = findViewById(R.id.deviceList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fillDeviceList();
        adapter = new deviceRecyclerView(deviceList, this);
        recyclerView.setAdapter(adapter);

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,addOrEditDevice.class);
                startActivity(intent);
            }
        });
        estimateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estimateBill();
            }
        });
    }

    private void estimateBill() {
        double totalBill = 0.0;
        double unitsConsumed = 0.0;
        for (Device device : deviceList) {
            unitsConsumed += device.getConsumption();
            //totalBill += device.getBill();

        }
        totalBill = unitsConsumed * 40.00;
        estimation.setText(String.format("Units consumed: %.5f\nTotal Bill: %.5f PKR", unitsConsumed, totalBill));
    }

    private void fillDeviceList() {
            // Read the data from Firebase and store it in the device list
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference devicesRef = database.getReference("Devices");

            devicesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    deviceList.clear(); // Clear the list to avoid duplication
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Device device = snapshot.getValue(Device.class);
                        if (device != null) {
                            device.setId(snapshot.getKey()); // Set the device ID
                            deviceList.add(device);
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load devices", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*private void fillDeviceList() {
        //Now read the data from fire base and store it in the device list
        // Ensure you're adding multiple devices to the list for testing
        deviceList.add(new Device("Bulb 1", 10.5 ));
        deviceList.add(new Device("Bulb 2", 15.0));
        deviceList.add(new Device("Fan 1", 50.0));
        deviceList.add(new Device("Fan 2", 50.0));
        deviceList.add(new Device("Fan 3", 50.0));
        deviceList.add(new Device("Fan 4", 50.0));
        deviceList.add(new Device("Fan 5", 50.0));
        // Add more devices as needed
    }*/

