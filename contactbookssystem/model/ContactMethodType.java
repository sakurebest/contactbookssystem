package com.example.contactbookssystem.model;

public enum ContactMethodType {
    PHONE("电话"),
    EMAIL("邮箱"),
    WECHAT("微信"),
    QQ("QQ"),
    ADDRESS("地址"),
    WEIBO("微博"),
    OTHER("其他");

    private final String displayName;

    ContactMethodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}