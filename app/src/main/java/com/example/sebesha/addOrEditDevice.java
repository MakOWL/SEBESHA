package com.example.sebesha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addOrEditDevice extends AppCompatActivity {
    Button cancelButton, addButton;
    EditText nameEditText, powerEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_device);
        addButton = findViewById(R.id.addDevice_button);
        cancelButton = findViewById(R.id.cancel_button);
        nameEditText = findViewById(R.id.dname_editText);
        powerEditText = findViewById(R.id.power_editText);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDeviceToFirebase();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addOrEditDevice.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addDeviceToFirebase() {
        String name = nameEditText.getText().toString().trim();
        String powerString = powerEditText.getText().toString().trim();

        if (!name.isEmpty() && !powerString.isEmpty()) {
            try {
                double power = Double.parseDouble(powerString);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference devicesRef = database.getReference("Devices");

                // Generate a unique ID for each device
                String deviceId = devicesRef.push().getKey();

                // Create a device object
                Device device = new Device(name, power);
                device.setId(deviceId);

                // Save the device object to Firebase
                devicesRef.child(deviceId).setValue(device).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(addOrEditDevice.this, "Device added", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(addOrEditDevice.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(addOrEditDevice.this, "Failed to add device", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for power", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }
}