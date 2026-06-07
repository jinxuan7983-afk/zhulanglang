package com.contest.dto.student;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewTeamMemberRequest {
    @NotNull(message = "成员记录ID不能为空")
    private Long memberId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
}
