package com.contest.dto.contest;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ContestSaveRequest {
    @NotBlank(message = "竞赛名称不能为空")
    private String name;

    @NotBlank(message = "竞赛类别不能为空")
    private String category;

    @NotBlank(message = "竞赛级别不能为空")
    private String level;

    @NotNull(message = "报名开始时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationStart;

    @NotNull(message = "报名结束时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationEnd;

    @NotNull(message = "竞赛时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime contestTime;

    private String location;
    private String organizer;
    private String coverImage;
    private String attachmentUrl;

    @NotBlank(message = "报名类型不能为空")
    private String registrationType;

    private Integer minTeamSize;
    private Integer maxTeamSize;

    @NotNull(message = "是否需要审核不能为空")
    private Boolean needReview;

    private String status;
}
