package com.contest.dto.student;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateTeamRequest {
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    @NotBlank(message = "团队名称不能为空")
    private String teamName;
}
