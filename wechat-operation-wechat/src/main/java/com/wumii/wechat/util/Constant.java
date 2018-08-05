package com.wumii.wechat.util;

import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;

import java.util.Base64;
import java.util.Set;

public final class Constant {

    public static final Set<String> SYSTEM_WXID
            = Sets.newHashSet("weixin","qqmail", "fmessage",
            "tmessage", "qmessage", "qqsync", "floatbottle",
            "lbsapp", "shakeapp", "medianote", "qqfriend",
            "newsapp", "blogapp", "facebookapp", "masssendapp",
            "feedsapp", "voipapp", "cardpackage", "voicevoipapp",
            "voiceinputapp", "officialaccounts", "linkedinplugin",
            "notifymessage", "appbrandcustomerservicemsg","pc_share",
            "notification_messages","helper_entry","filehelper");

    public static final String KEY_N = "B5791473FDFACCE426058401B6125A3D6FEDD76C7DD1B0426A73D8A4182B29EA6D05F4F5E8D99A4D3D1C3E5CF3C8CB3CDDF935643C94D38927881B144D04F310F13307D1AE63A100A2797A714C0D1E2A5A0EF779FC3D6F7D3C3396276BF27DA6D66E2696A6557EFD4B6190C726894D35CE559E147969BAC04AFEBB0E3A235B2C795AC6A9818E14A33A4468F8FF6ABE8A54A74180042BF0FD38427F70B681B9431A099E774618D455F14D1F75121577DAE66C3853A2AA9C4F0F9C221A66F64A46D5F68B0D50F22C7E4FA0D84048B2F9179F4B86442A2720C8FE27BC68C5C6384DCC336F97914F2788B905E5FE98C5BB754488B0F6B09421BB27BFF518EF0E9299";
    public static final String KEY_E = "010001";
    public static final int KEY_VERSION = 135;
    public static final String OS_VERSION = "iPad iPhone OS9.3.5";
    public static final int CLIENT_VERSION = 369492256;
    public static final String LANGUAGE = "zh_CN";

    public static final String EXCHANGE = ""; // TODO: 7/17/18 exchange

    private Constant() {}
}
