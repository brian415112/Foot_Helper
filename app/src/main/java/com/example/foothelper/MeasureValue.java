package com.example.foothelper;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class MeasureValue {
    private ArrayList<Integer> Forefoot_pressure;
    private ArrayList<Integer> Midfoot_pressure;
    private ArrayList<Integer> Hindfoot_pressure;
    private Timestamp timestamp;

    public MeasureValue() {
        Forefoot_pressure = new ArrayList<>();
        Midfoot_pressure = new ArrayList<>();
        Hindfoot_pressure = new ArrayList<>();
    }

    public void add_Forefoot_pressure(int i){
        Forefoot_pressure.add(i);
    }

    public void add_Midfoot_pressure(int i){
        Midfoot_pressure.add(i);
    }

    public void add_Hindfoot_pressure(int i){
        Hindfoot_pressure.add(i);
    }

    public ArrayList<Integer> getForefoot_pressure() {
        return Forefoot_pressure;
    }

    public void setForefoot_pressure(ArrayList<Integer> forefoot_pressure) {
        Forefoot_pressure = forefoot_pressure;
    }

    public ArrayList<Integer> getHindfoot_pressure() {
        return Hindfoot_pressure;
    }

    public void setMidfoot_pressure(ArrayList<Integer> midfoot_pressure) {
        Midfoot_pressure = midfoot_pressure;
    }

    public ArrayList<Integer> getMidfoot_pressure() {
        return Midfoot_pressure;
    }

    public void setHindfoot_pressure(ArrayList<Integer> hindfoot_pressure) {
        Hindfoot_pressure = hindfoot_pressure;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
