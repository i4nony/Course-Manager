package com.wumii.wechat.dao;

import com.wumii.wechat.entity.AutoReplyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutoReplayTemplateDao extends JpaRepository<AutoReplyTemplate, Long> {
}
