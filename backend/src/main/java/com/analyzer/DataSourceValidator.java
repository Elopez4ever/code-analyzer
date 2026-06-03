package com.analyzer;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
public class DataSourceValidator {
    private final DataSource dataSource;

    @Autowired
    public DataSourceValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void validate() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("数据库连接成功: {}", conn.getMetaData().getURL());
        } catch (Exception e) {
            log.error("数据库连接失败", e);
            throw new RuntimeException("无法连接数据库", e);
        }
    }
}