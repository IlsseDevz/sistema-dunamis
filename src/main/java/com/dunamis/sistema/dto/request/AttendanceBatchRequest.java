package com.dunamis.sistema.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceBatchRequest {

    @NotNull(message = "A data é obrigatória.")
    private LocalDate attendanceDate;

    private List<AttendanceItemRequest> items = new ArrayList<>();

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<AttendanceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<AttendanceItemRequest> items) {
        this.items = items;
    }

    public static class AttendanceItemRequest {
        private Long inscricaoId;
        private boolean present;
        private String observation;

        public Long getInscricaoId() {
            return inscricaoId;
        }

        public void setInscricaoId(Long inscricaoId) {
            this.inscricaoId = inscricaoId;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }

        public String getObservation() {
            return observation;
        }

        public void setObservation(String observation) {
            this.observation = observation;
        }
    }
}
