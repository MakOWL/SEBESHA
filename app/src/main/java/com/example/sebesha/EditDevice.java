package com.example.sebesha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EditDevice extends AppCompatActivity {

    private String did;
    private boolean dstatus; // true for on, false for off
    private String dname;
    private double dpower; // in watts
    private double dconsumption; // in units
    private double dbill;
    private long donTime;
    private long doffTime;
    ListView timeList;

    EditText nameEt, powerEt;
    TextView idTv, consumptionTv, billTv, timeTv, onTimeTv, offTimeTv;

    ArrayList<Long> onTimeList = new ArrayList<>();
    ArrayList<Long> offTimeList = new ArrayList<>();
    Button editBtn;
    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        nameEt = findViewById(R.id.dname_et);
        powerEt = findViewById(R.id.power_et);
        idTv = findViewById(R.id.id_tv);
        consumptionTv = findViewById(R.id.consumption_tv);
        billTv = findViewById(R.id.bill_tv);
       // timeTv = findViewById(R.id.time_tv);
        editBtn = findViewById(R.id.edit_btn);
        timeList = findViewById(R.id.timeDateList);

        Intent intent = getIntent();

        did = intent.getStringExtra("did");
        dname = intent.getStringExtra("dname");
        dpower = intent.getDoubleExtra("dpower", 0.0); // default to 0.0 if not provided
        dconsumption = intent.getDoubleExtra("dconsumption", 0.0); // default to 0.0 if not provided
        dbill = intent.getDoubleExtra("dbill", 0.0); // default to 0.0 if not provided
        donTime = intent.getLongExtra("donTime", 0);
        doffTime = intent.getLongExtra("doffTime", 0);

        // Set the retrieved data to the views
        idTv.setText(did);
        nameEt.setText(dname);
        powerEt.setText(String.valueOf(dpower));
        consumptionTv.setText(String.format("%.2f", dconsumption));
        billTv.setText(String.format("%.2f", dbill));
        //timeTv.setText(String.valueOf(donTime)); // Optionally format this if needed

        getTimeListFromFirebase(did);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameEt.getText().equals(dname) || powerEt.getText().equals(dpower)){
                    Toast.makeText(EditDevice.this,"You need to make changes ",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditDevice.this,"Name or Power of the device is changed ",Toast.LENGTH_SHORT).show();
                    String deviceId = did;
                    String newName,newPower;
                    newName = String.valueOf(nameEt.getText());
                    newPower = String.valueOf(powerEt.getText());
                    if (deviceId != null) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference deviceRef = database.getReference("Devices").child(deviceId);
                        deviceRef.child("name").setValue(newName);
                        deviceRef.child("Power").setValue(newPower);
                    }

                }

            }
        });
    }

    private void getTimeListFromFirebase(String id) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference().child("Devices").child(id);
        deviceRef.child("onTimes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int index = 0;
                    for (DataSnapshot timeSnapshot : snapshot.getChildren()) {
                        Long time = timeSnapshot.getValue(Long.class);
                        if (index % 2 == 0) {
                            onTimeList.add(time);
                        }
                        index++;
                    }
                    fetchOffTimes(id); // Call to fetch offTimes after onTimes are fetched
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void fetchOffTimes(String id) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference().child("Devices").child(id);

        // Fetch offTimes
        deviceRef.child("offTimes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot timeSnapshot : snapshot.getChildren()) {
                        Long time = timeSnapshot.getValue(Long.class);
                        offTimeList.add(time);
                    }
                    processAndDisplayData(); // Process and display data after offTimes are fetched
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void processAndDisplayData() {
        ArrayList<String> displayData = prepareDisplayData(onTimeList, offTimeList);
        // Initialize ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditDevice.this, android.R.layout.simple_list_item_1, displayData);
        timeList.setAdapter(adapter);
    }

    private ArrayList<String> prepareDisplayData(ArrayList<Long> onTimeList, ArrayList<Long> offTimeList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        ArrayList<String> displayData = new ArrayList<>();

        int size = Math.min(onTimeList.size(), offTimeList.size());
        for (int i = 0; i < size; i++) {
            Date onDate = new Date(onTimeList.get(i));
            Date offDate = new Date(offTimeList.get(i));
            String onTime = sdf.format(onDate);
            String offTime = sdf.format(offDate);

            String displayText = "On Time: " + onTime + " - Off Time: " + offTime;
            displayData.add(displayText);
        }

        // Handle cases where there are unmatched on times
        for (int i = size; i < onTimeList.size(); i++) {
            Date onDate = new Date(onTimeList.get(i));
            String onTime = sdf.format(onDate);
            String displayText = "On Time: " + onTime + " - Off Time: N/A";
            displayData.add(displayText);
        }

        // Handle cases where there are unmatched off times
        for (int i = size; i < offTimeList.size(); i++) {
            Date offDate = new Date(offTimeList.get(i));
            String offTime = sdf.format(offDate);
            String displayText = "On Time: N/A - Off Time: " + offTime;
            displayData.add(displayText);
        }

        return displayData;
    }
}
