package com.contest.dto.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentReviewItemResponse {
    private Long enrollmentId;
    private Long contestId;
    private String contestName;
    private Long userId;
    private String studentName;
    private String studentNo;
    private String status;
    private String reviewComment;
    private LocalDateTime registrationTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
    private Long teamId;
}
