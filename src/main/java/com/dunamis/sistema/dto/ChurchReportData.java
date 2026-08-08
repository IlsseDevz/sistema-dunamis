package com.dunamis.sistema.dto;

import java.util.ArrayList;
import java.util.List;

public class ChurchReportData {

    private String churchName;
    private String periodLabel;
    private long totalRegistered;
    private long activeMembers;
    private long newConverts;
    private long visitors;
    private long baptized;
    private long notBaptized;
    private long newRegistrationsInPeriod;
    private List<FunctionCount> functionDistribution = new ArrayList<>();

    public String getChurchName() {
        return churchName;
    }

    public void setChurchName(String churchName) {
        this.churchName = churchName;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public long getTotalRegistered() {
        return totalRegistered;
    }

    public void setTotalRegistered(long totalRegistered) {
        this.totalRegistered = totalRegistered;
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

    public long getNewRegistrationsInPeriod() {
        return newRegistrationsInPeriod;
    }

    public void setNewRegistrationsInPeriod(long newRegistrationsInPeriod) {
        this.newRegistrationsInPeriod = newRegistrationsInPeriod;
    }

    public List<FunctionCount> getFunctionDistribution() {
        return functionDistribution;
    }

    public void setFunctionDistribution(List<FunctionCount> functionDistribution) {
        this.functionDistribution = functionDistribution;
    }

    public static class FunctionCount {
        private String functionLabel;
        private long count;

        public FunctionCount(String functionLabel, long count) {
            this.functionLabel = functionLabel;
            this.count = count;
        }

        public String getFunctionLabel() {
            return functionLabel;
        }

        public void setFunctionLabel(String functionLabel) {
            this.functionLabel = functionLabel;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
