package com.dunamis.sistema.dto;

public class AdminDashboardStats {

    private long totalMembers;
    private long activeMembers;
    private long newConverts;
    private long visitors;
    private long baptized;
    private long notBaptized;
    private long totalStudents;

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(long activeMembers) {
        this.activeMembers = activeMembers;
    }

    public long getNewConverts() {
        return newConverts;
    }

    public void setNewConverts(long newConverts) {
        this.newConverts = newConverts;
    }

    public long getVisitors() {
        return visitors;
    }

    public void setVisitors(long visitors) {
        this.visitors = visitors;
    }

    public long getBaptized() {
        return baptized;
    }

    public void setBaptized(long baptized) {
        this.baptized = baptized;
    }

    public long getNotBaptized() {
        return notBaptized;
    }

    public void setNotBaptized(long notBaptized) {
        this.notBaptized = notBaptized;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }
}
