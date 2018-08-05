package com.wumii.wechat.dao;

import com.wumii.wechat.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupDao extends JpaRepository<UserGroup, Long> {
    int countByGroupWxid(String groupWxId);
    List<UserGroup> findByGroupWxidAndUserWxidIn(String groupWxid, List<String> userWxIds);
    int deleteByGroupWxidAndUserWxid(String groupWxid, String userWxid);
}
