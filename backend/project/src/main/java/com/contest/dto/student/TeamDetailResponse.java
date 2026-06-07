package com.contest.dto.student;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamDetailResponse {
    private Long teamId;
    private Long contestId;
    private String contestName;
    private String teamName;
    private Long captainId;
    private String captainName;
    private String inviteCode;
    private String status;
    private String registrationMaterial;
    private String reviewComment;
    private LocalDateTime submitTime;
    private Integer approvedMemberCount;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private List<TeamMemberItemResponse> members;
}
