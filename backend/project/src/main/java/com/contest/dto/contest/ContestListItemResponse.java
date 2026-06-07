package com.contest.dto.contest;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContestListItemResponse {
    private Long contestId;
    private String name;
    private String category;
    private String level;
    private String coverImage;
    private String organizer;
    private String location;
    private String registrationType;
    private String progressStatus;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime contestTime;
}
