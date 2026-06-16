package com.analyzer.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.project")
public class AppConfigProperties {
    private String uploadPath;
    private long maxFileSize;
    private String topic;
}
