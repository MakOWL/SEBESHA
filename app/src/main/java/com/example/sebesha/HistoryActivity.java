package com.example.sebesha;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button calculateButton;
    private TextView resultTextView;
    private TextView estimation;
    List<Device> deviceList = new ArrayList<>();
    private static final String TAG = "HistoryActivity";
    //private static final double RATE_PER_UNIT = 40.00; // Example rate per unit consumption

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        startDatePicker = findViewById(R.id.start_date_picker);
        endDatePicker = findViewById(R.id.end_date_picker);
        calculateButton = findViewById(R.id.calculate_button);
        resultTextView = findViewById(R.id.result_text_view);
        //estimation = findViewById(R.id.estimation_text_view);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startDate = getDateFromDatePicker(startDatePicker, true);
                long endDate = getDateFromDatePicker(endDatePicker, false);
                Log.d(TAG, "Start Date: " + startDate);
                Log.d(TAG, "End Date: " + endDate);

                readAllDataFromFirebase(new DataLoadedCallback() {
                    @Override
                    public void onDataLoaded() {
                        calculateEnergyConsumptionAndCost(startDate, endDate);
                    }
                });
            }
        });

        // Initial data load
        readAllDataFromFirebase(new DataLoadedCallback() {
            @Override
            public void onDataLoaded() {
                // Initial data load actions if needed
            }
        });
    }

    private void readAllDataFromFirebase(final DataLoadedCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Devices");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deviceList.clear();

                for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                    Device device = deviceSnapshot.getValue(Device.class);
                    if (device != null) {
                        deviceList.add(device);
                    }
                }

                Log.d(TAG, "Devices: " + deviceList);
                callback.onDataLoaded(); // Notify that data loading is complete
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting data from Firebase", databaseError.toException());
            }
        });
    }

    interface DataLoadedCallback {
        void onDataLoaded();
    }

    private long getStartOfDayInMillis(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDayInMillis(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    private long getDateFromDatePicker(DatePicker datePicker, boolean startOfDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        return startOfDay ? getStartOfDayInMillis(calendar) : getEndOfDayInMillis(calendar);
    }

    private String convertMillisToDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
    private void calculateEnergyConsumptionAndCost(long startDate, long endDate) {
        double totalEnergyConsumed = 0;

        Log.d(TAG, "Start Date (readable): " + convertMillisToDate(startDate));
        Log.d(TAG, "End Date (readable): " + convertMillisToDate(endDate));

        for (Device device : deviceList) {
            List<Long> onTimes = device.getOnTimes();
            List<Long> offTimes = device.getOffTimes();
            List<Double> consumptions = device.getConsumptions();

            for (int i = 0; i < onTimes.size(); i += 2) {
                long onTime = onTimes.get(i);
                long offTime = (i / 2 < offTimes.size()) ? offTimes.get(i / 2) : System.currentTimeMillis();
                double currentConsumption = (i / 2 < consumptions.size()) ? consumptions.get(i / 2) : 0;
                double previousConsumption = (i / 2 > 0 && i / 2 - 1 < consumptions.size()) ? consumptions.get(i / 2 - 1) : 0;

                Log.d(TAG, "Initial onTime: " + convertMillisToDate(onTime) + ", Initial offTime: " + convertMillisToDate(offTime) + ", Current consumption: " + currentConsumption);

                if (offTime <= startDate || onTime >= endDate) {
                    Log.d(TAG, "Skipping period outside of date range: onTime=" + convertMillisToDate(onTime) + ", offTime=" + convertMillisToDate(offTime));
                    continue;
                }

                long effectiveStartTime = Math.max(onTime, startDate);
                long effectiveEndTime = Math.min(offTime, endDate);

                if (effectiveStartTime < effectiveEndTime) {
                    long durationInMillis = effectiveEndTime - effectiveStartTime;
                    long totalPeriodDuration = offTime - onTime;
                    double energyConsumedInPeriod = (currentConsumption - previousConsumption) * ((double) durationInMillis / (double) totalPeriodDuration);

                    totalEnergyConsumed += energyConsumedInPeriod;
                    Log.d(TAG, "Energy consumed in period: " + energyConsumedInPeriod);
                } else {
                    Log.d(TAG, "Effective start time is not less than effective end time: effectiveStartTime=" + convertMillisToDate(effectiveStartTime) + ", effectiveEndTime=" + convertMillisToDate(effectiveEndTime));
                }
            }
        }

        double costPerKWh = 40.00;
        double totalCost = totalEnergyConsumed * costPerKWh;

        Log.d(TAG, "Total Energy Consumed: " + totalEnergyConsumed);
        Log.d(TAG, "Total Cost: " + totalCost);

        resultTextView.setText(String.format("Total Energy: %.5f kWh\nTotal Cost: PKR %.5f", totalEnergyConsumed, totalCost));
    }


}


