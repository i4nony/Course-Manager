package com.wumii.wechat.dao;


import com.wumii.wechat.entity.WeChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WechatGroupDao extends JpaRepository<WeChatGroup, String> {
    List<WeChatGroup> findByDeletedIsFalseOrderByCreationTimeDesc();
}
