package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.WechatGroupDao;
import com.wumii.wechat.entity.UserGroup;
import com.wumii.wechat.entity.WeChatGroup;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.result.TeacherInfo;
import com.wumii.wechat.result.WeChatGroupInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Profiling
@Service
public class WeChatGroupService {
    @Autowired
    private WechatGroupDao wechatGroupDao;
    @Autowired
    private WeChatUserService weChatUserService;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private WeChatChatRoomService weChatChatRoomService;
    @Autowired
    private WeChatLoginService weChatLoginService;

    public List<WeChatGroupInfo> getAllGroups() {
        List<WeChatGroup> weChatGroups =  wechatGroupDao.findByDeletedIsFalseOrderByCreationTimeDesc();
        List<WeChatGroupInfo> weChatGroupInfos = new ArrayList<>();
        List<TeacherInfo> teacherInfos = teacherService.getTeachers();
        for(WeChatGroup weChatGroup : weChatGroups) {
            WeChatUser owner = weChatUserService.getOne(weChatGroup.getOwnerWxid());
            WeChatGroupInfo weChatGroupInfo =
                    new WeChatGroupInfo(weChatGroup.getName(),
                            owner.getNickName(),
                            getOtherTeacherNames(weChatGroup, teacherInfos),
                            userGroupService.countByGroupWxid(weChatGroup.getWxid()));
            weChatGroupInfos.add(weChatGroupInfo);
        }
        return weChatGroupInfos;
    }

    private List<String> getOtherTeacherNames(WeChatGroup weChatGroup, List<TeacherInfo> teacherInfos) {
        List<String> otherTeachersWxIds = teacherInfos.stream().filter(teacher -> teacher.getWxid() != weChatGroup.getOwnerWxid())
                .map(TeacherInfo::getWxid).collect(Collectors.toList());
        final List<String> resultWxIds = userGroupService.findTeachsInThisGroup(weChatGroup.getWxid(), otherTeachersWxIds).stream()
                .map(UserGroup::getUserWxid).collect(Collectors.toList());
        return teacherInfos.stream().filter(teacherInfo -> resultWxIds.contains(teacherInfo.getWxid()))
                .map(TeacherInfo::getNickName).collect(Collectors.toList());
    }

    @Transactional
    public boolean delete(String wxid) {
        WeChatGroup weChatGroup = wechatGroupDao.findOne(wxid);
        weChatGroup.setDeleted(true);
        return wechatGroupDao.save(weChatGroup) != null;
    }

    public WeChatGroup getOne(String groupWxid) {
        return wechatGroupDao.findOne(groupWxid);
    }

    public void createGroup(List<String> wxids, String teacherWxid) throws InterruptedException {
        weChatLoginService.setCurrentWxid(teacherWxid);
        weChatChatRoomService.createChatRoom(wxids);
    }

    public void inviteToGroup(List<String> wxids, String teacherWxid) throws InterruptedException {
        weChatLoginService.setCurrentWxid(teacherWxid);
        weChatChatRoomService.addMember(teacherWxid, wxids);
    }
}
