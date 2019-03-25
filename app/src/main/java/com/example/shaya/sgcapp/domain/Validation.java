package com.example.shaya.sgcapp.domain;

public class Validation {

    public boolean validateName(String name)
    {
        if(name.matches("^[\\p{L}\\s'.-]+$"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean validateNumber(String number)
    {
        if(number.matches("\\+92[0-9]*"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean validateGroupName(String groupName)
    {
        if(groupName.matches("[A-Za-z0-9\\s]+"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean validateStatus(String status)
    {
        if(status.matches("[a-zA-Z0-9\\s]\\*{0,100}"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
