package com.contest.dto.student;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class JoinTeamRequest {
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
