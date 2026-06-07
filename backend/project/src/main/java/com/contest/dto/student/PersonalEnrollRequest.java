package com.contest.dto.student;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PersonalEnrollRequest {
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;
}
