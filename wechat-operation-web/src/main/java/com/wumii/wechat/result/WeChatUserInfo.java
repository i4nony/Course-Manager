package com.wumii.wechat.result;

public class WeChatUserInfo {
    private String wxid;
    private String nickName;

    public WeChatUserInfo(String wxid, String nickName) {
        this.wxid = wxid;
        this.nickName = nickName;
    }

    public String getWxid() {
        return wxid;
    }

    public String getNickName() {
        return nickName;
    }
}
