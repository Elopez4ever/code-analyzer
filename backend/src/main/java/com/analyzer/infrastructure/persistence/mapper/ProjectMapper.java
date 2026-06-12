package com.analyzer.infrastructure.persistence.mapper;

import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<ProjectPO> {
}
