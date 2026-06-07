package com.contest.dto.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberItemResponse {
    private Long memberId;
    private Long userId;
    private String name;
    private String college;
    private String phone;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
}
