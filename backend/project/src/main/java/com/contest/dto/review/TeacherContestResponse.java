package com.contest.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherContestResponse {
    private Long contestId;
    private String contestName;
    private String category;
    private String level;
    private String status;
}
