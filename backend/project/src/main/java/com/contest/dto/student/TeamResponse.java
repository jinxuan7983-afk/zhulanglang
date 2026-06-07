package com.contest.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamResponse {
    private Long teamId;
    private String teamName;
    private String inviteCode;
    private String status;
}
