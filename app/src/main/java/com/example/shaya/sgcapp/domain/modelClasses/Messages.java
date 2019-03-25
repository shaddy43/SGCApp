package com.example.shaya.sgcapp.domain.modelClasses;

public class Messages {

    private String message, from, type, msgKey, keyVersion;

    public Messages() {
    }

    public Messages(String message, String from, String type, String msgKey, String keyVersion) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.msgKey = msgKey;
        this.keyVersion = keyVersion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }
}

