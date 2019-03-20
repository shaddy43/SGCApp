package com.example.shaya.sgcapp.Domain.ModelClasses;

public class AllUsers {

     private String userId;

     private String Phone_Number;
     private String Name;
     private String Profile_Pic;
     private String Status;

     private String onlineState;

    public AllUsers() {
    }

    public AllUsers(String name, String profile_Pic, String status) {
        Name = name;
        Profile_Pic = profile_Pic;
        Status = status;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }

    public void setPhone_Number(String phone_Number) {
        Phone_Number = phone_Number;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getProfile_Pic() {
        return Profile_Pic;
    }

    public void setProfile_Pic(String profile_Pic) {
        Profile_Pic = profile_Pic;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOnlineState() {
        return onlineState;
    }

    public void setOnlineState(String onlineState) {
        this.onlineState = onlineState;
    }
}
