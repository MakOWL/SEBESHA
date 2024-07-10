package com.example.sebesha;


import java.util.ArrayList;
import java.util.List;



import java.util.ArrayList;
import java.util.List;

public class Device {
    private String id;
    private String status; // "ON" or "OFF"
    private String name;
    private double power; // in watts
    private double consumption; // in units
    private double bill;

    private double totalTimeInHours; // total time in hours
    private long onTime, offTime; // store unix time for the number of seconds it was on
    private List<Long> onTimes; // list to store on times
    private List<Long> offTimes; // list to store off times
    private List<UsageRecord> usageHistory;
    private List<Double> consumptions;

    // Default constructor required for calls to DataSnapshot.getValue(Device.class)
    public Device() {
        this.status = "OFF";
        this.onTimes = new ArrayList<>();
        this.offTimes = new ArrayList<>();
        this.consumptions = new ArrayList<>();
        this.usageHistory = new ArrayList<>();
    }

    public Device(String name, double power) {
        this.status = "OFF";
        this.name = name;
        this.power = power;
        this.consumption = 0;
        this.bill = 0;
        this.onTime = 0;
        this.onTimes = new ArrayList<>();
        this.offTimes = new ArrayList<>();
        this.consumptions = new ArrayList<>();
        this.usageHistory = new ArrayList<>();
    }

    // Getters and Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTotalTimeInHours(double totalTimeInHours) {
        this.totalTimeInHours = totalTimeInHours;
    }

    public double getTotalTimeInHours() {
        return totalTimeInHours;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getConsumption() {
        return consumption;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (!this.status.equals(status)) {
            if (status.equals("ON")) {
                // Turning on the device
                this.onTime = System.currentTimeMillis();
                this.onTimes.add(this.onTime);
            } else if (status.equals("OFF")) {
                // Turning off the device and updating consumption
                this.offTime = System.currentTimeMillis();
                this.offTimes.add(this.offTime);
                this.consumption += (this.offTime - this.onTime) * this.power / 3600000.0; // convert milliseconds to hours
                this.onTime = 0;
            }
            this.status = status;
        }
    }


    public List<Double> getConsumptions() {
        return consumptions;
    }

    public void setConsumptions(List<Double> consumptions) {
        this.consumptions = consumptions;
    }

    public void addConsumption(double consumption) {
        this.consumptions.add(consumption);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public void resetConsumption() {
        this.consumption = 0;
    }

    public double getBill() {
        return bill;
    }

    public void setBill(double bill) {
        this.bill = bill;
    }

    public void setOnTime(long onTime) {
        this.onTime = onTime;
    }

    public long getOnTime() {
        return onTime;
    }

    public long getOffTime() {
        return offTime;
    }

    public void setOffTime(long offTime) {
        this.offTime = offTime;
    }

    public List<Long> getOnTimes() {
        return onTimes;
    }

    public void setOnTimes(List<Long> onTimes) {
        this.onTimes = onTimes;
    }

    public List<Long> getOffTimes() {
        return offTimes;
    }



    public void setOffTimes(List<Long> offTimes) {
        this.offTimes = offTimes;
    }

    // Method to simulate device usage for a certain period
    public void simulateUsage(long seconds) {
        if (status.equals("ON")) {
            this.consumption += (seconds * this.power) / 3600;
            this.onTime += seconds;
        }
    }

    @Override
    public String toString() {
        return "Device{" +
                "status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", power=" + power + "W" +
                ", consumption=" + consumption + " units" +
                ", onTime=" + onTime + " milliseconds" +
                ", onTimes=" + onTimes +
                ", offTimes=" + offTimes +
                '}';
    }

    public static class UsageRecord {
        public long onTime;
        public long offTime;
        public double consumption;
        public double bill;

        // Default constructor
        public UsageRecord() {}

        // Parameterized constructor
        public UsageRecord(long onTime, long offTime, double consumption, double bill) {
            this.onTime = onTime;
            this.offTime = offTime;
            this.consumption = consumption;
            this.bill = bill;
        }
    }

    public void addUsageRecord(long onTime, long offTime, double consumption, double bill) {
        this.usageHistory.add(new UsageRecord(onTime, offTime, consumption, bill));
    }

    public List<UsageRecord> getUsageHistory() {
        return usageHistory;
    }

    // Other existing methods in the Device class
}





