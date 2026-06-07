package com.contest.controller;

import com.contest.common.Result;
import com.contest.dto.student.CreateTeamRequest;
import com.contest.dto.student.EnrollmentRecordResponse;
import com.contest.dto.student.JoinTeamRequest;
import com.contest.dto.student.PersonalEnrollRequest;
import com.contest.dto.student.ReviewTeamMemberRequest;
import com.contest.dto.student.SubmitTeamEnrollmentRequest;
import com.contest.dto.student.TeamDetailResponse;
import com.contest.dto.student.TeamResponse;
import com.contest.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @PostMapping("/enrollments/personal")
    public Result<Void> personalEnroll(@Valid @RequestBody PersonalEnrollRequest request) {
        studentService.personalEnroll(request);
        return Result.success();
    }

    @GetMapping("/enrollments")
    public Result<List<EnrollmentRecordResponse>> listMyEnrollments() {
        return Result.success(studentService.listMyEnrollments());
    }

    @PostMapping("/teams")
    public Result<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return Result.success(studentService.createTeam(request));
    }

    @PostMapping("/teams/join")
    public Result<Void> joinTeam(@Valid @RequestBody JoinTeamRequest request) {
        studentService.joinTeam(request);
        return Result.success();
    }

    @GetMapping("/teams/{teamId}")
    public Result<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(studentService.getTeamDetail(teamId));
    }

    @PostMapping("/teams/{teamId}/members/review")
    public Result<Void> reviewTeamMember(@PathVariable Long teamId,
                                         @Valid @RequestBody ReviewTeamMemberRequest request) {
        studentService.reviewTeamMember(teamId, request);
        return Result.success();
    }

    @PostMapping("/teams/{teamId}/submit")
    public Result<Void> submitTeamEnrollment(@PathVariable Long teamId,
                                             @RequestBody(required = false) SubmitTeamEnrollmentRequest request) {
        studentService.submitTeamEnrollment(teamId, request == null ? new SubmitTeamEnrollmentRequest() : request);
        return Result.success();
    }
}
