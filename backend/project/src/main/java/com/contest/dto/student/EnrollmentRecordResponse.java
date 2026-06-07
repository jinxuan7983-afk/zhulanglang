package com.contest.dto.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentRecordResponse {
    private Long enrollmentId;
    private Long contestId;
    private String contestName;
    private Long teamId;
    private String status;
    private String reviewComment;
    private LocalDateTime registrationTime;
}
