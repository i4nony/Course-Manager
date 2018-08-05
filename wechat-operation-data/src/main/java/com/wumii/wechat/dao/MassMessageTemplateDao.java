package com.wumii.wechat.dao;

import com.wumii.wechat.entity.MassMessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MassMessageTemplateDao extends JpaRepository<MassMessageTemplate, Long> {
}
