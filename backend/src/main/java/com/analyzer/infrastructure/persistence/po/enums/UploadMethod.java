package com.analyzer.infrastructure.persistence.po.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadMethod {
    GIT(0),
    ZIP(1);

    @EnumValue
    private final int value;
}
