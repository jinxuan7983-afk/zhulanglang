package com.contest.dto.review;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ReviewRequest {
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private String reviewComment;
}
