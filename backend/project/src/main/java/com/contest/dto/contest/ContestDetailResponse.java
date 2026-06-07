package com.contest.dto.contest;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContestDetailResponse {
    private Long contestId;
    private String name;
    private String category;
    private String level;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime contestTime;
    private String location;
    private String organizer;
    private String coverImage;
    private String attachmentUrl;
    private String registrationType;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Boolean needReview;
    private String progressStatus;
}
