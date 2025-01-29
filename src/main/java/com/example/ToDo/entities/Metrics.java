package com.example.ToDo.entities;

public class Metrics {
    private long totalAverage;
    private long highAverage;
    private long mediumAverage;
    private long lowAverage;

    public Metrics(long totalAverage, long highAverage, long mediumAverage, long lowAverage){
        this.totalAverage= totalAverage;
        this.highAverage=highAverage;
        this.mediumAverage=mediumAverage;
        this.lowAverage=lowAverage;
    }

    public long getTotalAverage() {
        return totalAverage;
    }

    public void setTotalAverage(long totalAverage) {
        this.totalAverage = totalAverage;
    }

    public long getHighAverage() {
        return highAverage;
    }

    public void setHighAverage(long highAverage) {
        this.highAverage = highAverage;
    }

    public long getMediumAverage() {
        return mediumAverage;
    }

    public void setMediumAverage(long mediumAverage) {
        this.mediumAverage = mediumAverage;
    }

    public long getLowAverage() {
        return lowAverage;
    }

    public void setLowAverage(long lowAverage) {
        this.lowAverage = lowAverage;
    }
    
}
