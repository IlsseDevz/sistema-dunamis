package com.dunamis.sistema.dto;

import java.math.BigDecimal;

public class BibleSchoolReportData {

    private String churchName;
    private String bibleSchoolName;
    private String periodLabel;
    private int totalStudents;
    private long studentsPresent;
    private long studentsAbsent;
    private double attendancePercentage;
    private BigDecimal averageGrade;
    private long totalAttendanceRecords;
    private long presentRecords;
    private long absentRecords;

    public String getChurchName() {
        return churchName;
    }

    public void setChurchName(String churchName) {
        this.churchName = churchName;
    }

    public String getBibleSchoolName() {
        return bibleSchoolName;
    }

    public void setBibleSchoolName(String bibleSchoolName) {
        this.bibleSchoolName = bibleSchoolName;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getStudentsPresent() {
        return studentsPresent;
    }

    public void setStudentsPresent(long studentsPresent) {
        this.studentsPresent = studentsPresent;
    }

    public long getStudentsAbsent() {
        return studentsAbsent;
    }

    public void setStudentsAbsent(long studentsAbsent) {
        this.studentsAbsent = studentsAbsent;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public BigDecimal getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(BigDecimal averageGrade) {
        this.averageGrade = averageGrade;
    }

    public long getTotalAttendanceRecords() {
        return totalAttendanceRecords;
    }

    public void setTotalAttendanceRecords(long totalAttendanceRecords) {
        this.totalAttendanceRecords = totalAttendanceRecords;
    }

    public long getPresentRecords() {
        return presentRecords;
    }

    public void setPresentRecords(long presentRecords) {
        this.presentRecords = presentRecords;
    }

    public long getAbsentRecords() {
        return absentRecords;
    }

    public void setAbsentRecords(long absentRecords) {
        this.absentRecords = absentRecords;
    }
}
