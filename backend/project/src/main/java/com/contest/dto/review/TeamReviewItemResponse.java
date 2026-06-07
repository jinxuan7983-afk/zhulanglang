package com.contest.dto.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamReviewItemResponse {
    private Long teamId;
    private Long contestId;
    private String contestName;
    private String teamName;
    private Long captainId;
    private String captainName;
    private String inviteCode;
    private String status;
    private String reviewComment;
    private String registrationMaterial;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
}
