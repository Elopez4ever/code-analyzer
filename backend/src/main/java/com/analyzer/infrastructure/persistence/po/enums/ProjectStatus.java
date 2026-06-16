package com.analyzer.infrastructure.persistence.po.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectStatus {
    PARSING(0),
    READY(1),
    FAILED(-1),
    DELETED(-2)
    ;


    @EnumValue
    private final int value;
}
