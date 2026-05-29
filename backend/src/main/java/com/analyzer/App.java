package com.analyzer;

import com.analyzer.common.config.AIClientProperties;
import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.common.config.ParserConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppConfigProperties.class, ParserConfigProperties.class, AIClientProperties.class})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
