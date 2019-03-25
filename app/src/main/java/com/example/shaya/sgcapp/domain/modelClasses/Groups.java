package com.example.shaya.sgcapp.domain.modelClasses;

public class Groups {

    private String groupKey;

    private String Name;
    private String Total_Members;
    private String Group_Pic;

    public Groups() {
    }

    public Groups(String groupName, String totalMembers, String group_Pic) {
        Name = groupName;
        Total_Members = totalMembers;
        Group_Pic = group_Pic;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTotal_Members() {
        return Total_Members;
    }

    public void setTotal_Members(String total_Members) {
        Total_Members = total_Members;
    }

    public String getGroup_Pic() {
        return Group_Pic;
    }

    public void setGroup_Pic(String group_Pic) {
        Group_Pic = group_Pic;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
