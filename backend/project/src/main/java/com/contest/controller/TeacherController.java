package com.contest.controller;

import com.contest.common.Result;
import com.contest.dto.review.EnrollmentReviewItemResponse;
import com.contest.dto.review.ReviewRequest;
import com.contest.dto.review.TeacherContestResponse;
import com.contest.dto.review.TeamReviewItemResponse;
import com.contest.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {
    private final ReviewService reviewService;

    @GetMapping("/contests")
    public Result<List<TeacherContestResponse>> listTeacherContests() {
        return Result.success(reviewService.listTeacherContests());
    }

    @GetMapping("/enrollments")
    public Result<List<EnrollmentReviewItemResponse>> listTeacherEnrollments(@RequestParam Long contestId,
                                                                             @RequestParam(required = false) String status) {
        return Result.success(reviewService.listTeacherEnrollments(contestId, status));
    }

    @PostMapping("/enrollments/{enrollmentId}/review")
    public Result<Void> reviewTeacherEnrollment(@PathVariable Long enrollmentId,
                                                @Valid @RequestBody ReviewRequest request) {
        reviewService.reviewTeacherEnrollment(enrollmentId, request);
        return Result.success();
    }

    @GetMapping("/teams")
    public Result<List<TeamReviewItemResponse>> listTeacherTeams(@RequestParam Long contestId,
                                                                 @RequestParam(required = false) String status) {
        return Result.success(reviewService.listTeacherTeams(contestId, status));
    }

    @PostMapping("/teams/{teamId}/review")
    public Result<Void> reviewTeacherTeam(@PathVariable Long teamId,
                                          @Valid @RequestBody ReviewRequest request) {
        reviewService.reviewTeacherTeam(teamId, request);
        return Result.success();
    }
}
