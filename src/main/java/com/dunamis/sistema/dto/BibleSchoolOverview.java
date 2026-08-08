package com.dunamis.sistema.dto;

public class BibleSchoolOverview {

    private TurmaSummary turma;
    private long totalDisciplinas;
    private long totalAlunos;

    public TurmaSummary getTurma() {
        return turma;
    }

    public void setTurma(TurmaSummary turma) {
        this.turma = turma;
    }

    public long getTotalDisciplinas() {
        return totalDisciplinas;
    }

    public void setTotalDisciplinas(long totalDisciplinas) {
        this.totalDisciplinas = totalDisciplinas;
    }

    public long getTotalAlunos() {
        return totalAlunos;
    }

    public void setTotalAlunos(long totalAlunos) {
        this.totalAlunos = totalAlunos;
    }

    public static class TurmaSummary {
        private Long id;
        private String name;
        private Integer year;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
