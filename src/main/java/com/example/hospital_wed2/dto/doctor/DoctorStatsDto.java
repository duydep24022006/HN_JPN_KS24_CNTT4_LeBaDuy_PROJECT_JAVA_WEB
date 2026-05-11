package com.example.hospital_wed2.dto.doctor;

public class DoctorStatsDto {
    private long todayAppointments;
    private long pendingCount;
    private long confirmedCount;
    private long completedToday;
    private long totalPatients;
    private long totalCompleted;
    private long totalCancelled;

    // Getters & Setters
    public long getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(long todayAppointments) { this.todayAppointments = todayAppointments; }

    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }

    public long getConfirmedCount() { return confirmedCount; }
    public void setConfirmedCount(long confirmedCount) { this.confirmedCount = confirmedCount; }

    public long getCompletedToday() { return completedToday; }
    public void setCompletedToday(long completedToday) { this.completedToday = completedToday; }

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

    public long getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(long totalCompleted) { this.totalCompleted = totalCompleted; }

    public long getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(long totalCancelled) { this.totalCancelled = totalCancelled; }
}