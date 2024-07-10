package com.example.sebesha;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.logging.LogRecord;

public class deviceRecyclerView extends RecyclerView.Adapter<deviceRecyclerView.MyViewHolder> {
    private List<Device> deviceList;
    private Context context;
    private android.os.Handler timeHandler;
    private Runnable updateTimeRunnable;

    public deviceRecyclerView(List<Device> deviceList, Context context) {
        this.deviceList = deviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.once_device_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.unitConsumed.setText(String.format("%.5f", device.getConsumption()));
        holder.cost.setText(String.format("%.5f", device.getConsumption()*40));

        holder.deviceSwitch.setOnCheckedChangeListener(null);
        holder.deviceSwitch.setChecked("ON".equals(device.getStatus())); // Check the switch based on status

        holder.deviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            long currentTime = System.currentTimeMillis();

            if (isChecked) {
                // Device is being turned on
                if (device.getOnTime() == 0) {
                    device.setOnTime(currentTime);
                    Log.d("deviceRecyclerView", "Set onTime for device: " + device.getName() + " to: " + currentTime);
                }
                device.setStatus("ON"); // Update status to "ON"
                Toast.makeText(context,device.getName() + " was turned on",Toast.LENGTH_SHORT).show();
               /* timeHandler = new Handler();
                updateTimeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        calculateTimeConsumptionBill2(device);
                        timeHandler.postDelayed(this,5000);
                    }
                };
                timeHandler.post(updateTimeRunnable);*/
            } else {
                // Device is being turned off
                device.setOffTime(currentTime);
                Log.d("deviceRecyclerView", "Set offTime for device: " + device.getName() + " to: " + currentTime);
                calculateTimeConsumptionBill(device);
                device.setStatus("OFF"); // Update status to "OFF"
                Toast.makeText(context,device.getName() + " was turned off",Toast.LENGTH_SHORT).show();

                // Add the new consumption to the list
                device.addConsumption(device.getConsumption());
            }

            String deviceId = device.getId();
            if (deviceId != null) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference deviceRef = database.getReference("Devices").child(deviceId);

                Log.d("deviceRecyclerView", "Switch toggled for device: " + device.getName() + " to: " + isChecked);

                deviceRef.setValue(device).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("deviceRecyclerView", "Failed to update device: " + task.getException());
                        holder.deviceSwitch.setChecked(!isChecked);
                        Toast.makeText(context, "Failed to update device status. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("deviceRecyclerView", "Error updating device: " + e.getMessage());
                    holder.deviceSwitch.setChecked(!isChecked);
                    Toast.makeText(context, "Failed to update device status. Please try again.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.e("deviceRecyclerView", "Device ID is null for device: " + device.getName());
                Toast.makeText(context, "Device ID is missing. Unable to update status.", Toast.LENGTH_SHORT).show();
                holder.deviceSwitch.setChecked(!isChecked);
            }
        });

        holder.parentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDevice.class);
            intent.putExtra("did", device.getId());
            intent.putExtra("dname", device.getName());
            intent.putExtra("dpower", device.getPower());
            intent.putExtra("dconsumption", device.getConsumption());
            intent.putExtra("dbill", device.getConsumption()*40);
            intent.putExtra("donTime", device.getOnTime());
            intent.putExtra("doffTime", device.getOffTime());

            context.startActivity(intent);
        });


    }

    public void calculateTimeConsumptionBill(Device device) {
        long onTime = device.getOnTime();
        long offTime = device.getOffTime();
        long durationInMillis = offTime - onTime;
        double durationInHours = durationInMillis / (1000.0 * 60 * 60); // Convert milliseconds to hours

        device.setTotalTimeInHours(durationInHours);

        // Calculate energy consumption in kWh
        double powerInKilowatts = device.getPower() / 1000.0; // Convert watts to kilowatts
        double newEnergyConsumption = powerInKilowatts * durationInHours; // kWh
        double totalEnergyConsumption = device.getConsumption() + newEnergyConsumption; // Add new consumption to existing

        // Assuming a cost per kWh (this value should be adjusted as needed)
        double costPerKWh = 40.00;
        double newBill = totalEnergyConsumption * costPerKWh; // Calculate cost for new energy consumption
        double totalBill = device.getBill() + newBill; // Add new bill to the existing bill

        // Update the device with the new total values
        Log.d("Addition to Units", String.valueOf(newEnergyConsumption));
        Log.d("Addition to Units", String.valueOf(newBill));
        device.setConsumption(totalEnergyConsumption);
        device.setBill(totalBill);

        // Update Firebase with the new values
        String deviceId = device.getId();
        if (deviceId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference deviceRef = database.getReference("Devices").child(deviceId);
            deviceRef.child("totalTimeInHours").setValue(durationInHours);
            deviceRef.child("consumption").setValue(totalEnergyConsumption);
            deviceRef.child("bill").setValue(totalBill);
            deviceRef.child("onTimes").setValue(device.getOnTimes());
            deviceRef.child("offTimes").setValue(device.getOffTimes());
            deviceRef.child("consumptions").setValue(device.getConsumptions());
        }
    }

    public void calculateTimeConsumptionBill2(Device device) {
        long onTime = device.getOnTime();
        long offTime = device.getOnTime() + 450000;
        long durationInMillis = offTime - onTime;
        double durationInHours = durationInMillis / (1000.0 * 60 * 60); // Convert milliseconds to hours

        device.setTotalTimeInHours(durationInHours);

        // Calculate energy consumption in kWh
        double powerInKilowatts = device.getPower() / 1000.0; // Convert watts to kilowatts
        double newEnergyConsumption = powerInKilowatts * durationInHours; // kWh
        double totalEnergyConsumption = device.getConsumption() + newEnergyConsumption; // Add new consumption to existing

        // Assuming a cost per kWh (this value should be adjusted as needed)
        double costPerKWh = 40.00;
        double newBill = totalEnergyConsumption * costPerKWh; // Calculate cost for new energy consumption
        double totalBill = device.getBill() + newBill; // Add new bill to the existing bill

        // Update the device with the new total values
        Log.d("Addition to Units", String.valueOf(newEnergyConsumption));
        Log.d("Addition to Units", String.valueOf(newBill));
        device.setConsumption(totalEnergyConsumption);
        device.setBill(totalBill);

        // Update Firebase with the new values
        String deviceId = device.getId();
        if (deviceId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference deviceRef = database.getReference("Devices").child(deviceId);
            deviceRef.child("totalTimeInHours").setValue(durationInHours);
            deviceRef.child("consumption").setValue(totalEnergyConsumption);
            deviceRef.child("bill").setValue(totalBill);
            deviceRef.child("onTimes").setValue(device.getOnTimes());
            deviceRef.child("offTimes").setValue(device.getOffTimes());
            deviceRef.child("consumptions").setValue(device.getConsumptions());
        }
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Switch deviceSwitch;
        TextView deviceName, unitConsumed, cost;
        LinearLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceSwitch = itemView.findViewById(R.id.device_switch);
            deviceName = itemView.findViewById(R.id.deviceName_textView);
            unitConsumed = itemView.findViewById(R.id.units_TextView);
            cost = itemView.findViewById(R.id.bill_textView);
            parentLayout = itemView.findViewById(R.id.oneDeviceLayout);
        }
    }
}
