package com.contest.dto.contest;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminContestItemResponse {
    private Long contestId;
    private String name;
    private String category;
    private String level;
    private String status;
    private String registrationType;
    private Boolean needReview;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime contestTime;
    private LocalDateTime createTime;
    private String progressStatus;
}
