package com.analyzer.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ConfigurationProperties(prefix = "app.project")
@Getter
@Setter
public class AppConfigProperties {
    private String uploadPath;
    private List<String> allowedTypes;
}
