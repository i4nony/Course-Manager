package com.wumii.wechat.result;

import java.util.List;

public class WeChatGroupInfo {
    private String name;
    private String owner;
    private List<String> otherTeachers;
    private int memberCount;

    public WeChatGroupInfo(String name, String owner, List<String> otherTeachers, int memberCount) {
        this.name = name;
        this.owner = owner;
        this.otherTeachers = otherTeachers;
        this.memberCount = memberCount;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getOtherTeachers() {
        return otherTeachers;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
