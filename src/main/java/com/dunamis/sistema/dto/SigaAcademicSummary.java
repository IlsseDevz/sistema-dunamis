package com.dunamis.sistema.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SigaAcademicSummary {

    private String turmaName;
    private Integer turmaYear;
    private int totalDisciplinas;
    private BigDecimal overallAverage;
    private double attendancePercentage;
    private long totalAttendanceRecords;
    private long presentCount;
    private long absentCount;
    private List<DisciplinaGradeSummary> disciplinaGrades = new ArrayList<>();

    public String getTurmaName() {
        return turmaName;
    }

    public void setTurmaName(String turmaName) {
        this.turmaName = turmaName;
    }

    public Integer getTurmaYear() {
        return turmaYear;
    }

    public void setTurmaYear(Integer turmaYear) {
        this.turmaYear = turmaYear;
    }

    public int getTotalDisciplinas() {
        return totalDisciplinas;
    }

    public void setTotalDisciplinas(int totalDisciplinas) {
        this.totalDisciplinas = totalDisciplinas;
    }

    public BigDecimal getOverallAverage() {
        return overallAverage;
    }

    public void setOverallAverage(BigDecimal overallAverage) {
        this.overallAverage = overallAverage;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public long getTotalAttendanceRecords() {
        return totalAttendanceRecords;
    }

    public void setTotalAttendanceRecords(long totalAttendanceRecords) {
        this.totalAttendanceRecords = totalAttendanceRecords;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public long getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(long absentCount) {
        this.absentCount = absentCount;
    }

    public List<DisciplinaGradeSummary> getDisciplinaGrades() {
        return disciplinaGrades;
    }

    public void setDisciplinaGrades(List<DisciplinaGradeSummary> disciplinaGrades) {
        this.disciplinaGrades = disciplinaGrades;
    }

    public static class DisciplinaGradeSummary {
        private Long disciplinaId;
        private String disciplinaName;
        private BigDecimal average;
        private List<GradeItem> grades = new ArrayList<>();

        public Long getDisciplinaId() {
            return disciplinaId;
        }

        public void setDisciplinaId(Long disciplinaId) {
            this.disciplinaId = disciplinaId;
        }

        public String getDisciplinaName() {
            return disciplinaName;
        }

        public void setDisciplinaName(String disciplinaName) {
            this.disciplinaName = disciplinaName;
        }

        public BigDecimal getAverage() {
            return average;
        }

        public void setAverage(BigDecimal average) {
            this.average = average;
        }

        public List<GradeItem> getGrades() {
            return grades;
        }

        public void setGrades(List<GradeItem> grades) {
            this.grades = grades;
        }
    }

    public static class GradeItem {
        private String type;
        private BigDecimal value;
        private String recordedAt;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public String getRecordedAt() {
            return recordedAt;
        }

        public void setRecordedAt(String recordedAt) {
            this.recordedAt = recordedAt;
        }
    }
}
