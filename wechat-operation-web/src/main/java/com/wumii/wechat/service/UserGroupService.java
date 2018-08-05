package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.UserGroupDao;
import com.wumii.wechat.entity.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Profiling
@Service
public class UserGroupService {
    @Autowired
    private UserGroupDao userGroupDao;

    public int countByGroupWxid(String groupWxid) {
        return userGroupDao.countByGroupWxid(groupWxid);
    }

    public List<UserGroup> findTeachsInThisGroup(String groupWxid, List<String> userWxids) {
        return userGroupDao.findByGroupWxidAndUserWxidIn(groupWxid, userWxids);
    }
}
