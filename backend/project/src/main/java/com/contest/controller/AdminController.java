package com.contest.controller;

import com.contest.common.Result;
import com.contest.common.PageResult;
import com.contest.dto.contest.AdminContestItemResponse;
import com.contest.dto.contest.AdminContestQueryRequest;
import com.contest.dto.contest.ContestDetailResponse;
import com.contest.dto.contest.ContestSaveRequest;
import com.contest.dto.review.EnrollmentReviewItemResponse;
import com.contest.dto.review.ReviewRequest;
import com.contest.dto.review.TeamReviewItemResponse;
import com.contest.service.ContestService;
import com.contest.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ReviewService reviewService;
    private final ContestService contestService;

    @GetMapping("/enrollments")
    public Result<List<EnrollmentReviewItemResponse>> listAdminEnrollments(@RequestParam(required = false) Long contestId,
                                                                           @RequestParam(required = false) String status) {
        return Result.success(reviewService.listAdminEnrollments(contestId, status));
    }

    @PostMapping("/enrollments/{enrollmentId}/review")
    public Result<Void> reviewAdminEnrollment(@PathVariable Long enrollmentId,
                                              @Valid @RequestBody ReviewRequest request) {
        reviewService.reviewAdminEnrollment(enrollmentId, request);
        return Result.success();
    }

    @GetMapping("/teams")
    public Result<List<TeamReviewItemResponse>> listAdminTeams(@RequestParam(required = false) Long contestId,
                                                               @RequestParam(required = false) String status) {
        return Result.success(reviewService.listAdminTeams(contestId, status));
    }

    @PostMapping("/teams/{teamId}/review")
    public Result<Void> reviewAdminTeam(@PathVariable Long teamId,
                                        @Valid @RequestBody ReviewRequest request) {
        reviewService.reviewAdminTeam(teamId, request);
        return Result.success();
    }

    @GetMapping("/contests")
    public Result<PageResult<AdminContestItemResponse>> pageAdminContests(AdminContestQueryRequest request) {
        return Result.success(contestService.pageAdminContests(request));
    }

    @GetMapping("/contests/{contestId}")
    public Result<ContestDetailResponse> getAdminContestDetail(@PathVariable Long contestId) {
        return Result.success(contestService.getAdminContestDetail(contestId));
    }

    @PostMapping("/contests")
    public Result<Long> createContest(@Valid @RequestBody ContestSaveRequest request) {
        return Result.success(contestService.createContest(request));
    }

    @PutMapping("/contests/{contestId}")
    public Result<Void> updateContest(@PathVariable Long contestId,
                                      @Valid @RequestBody ContestSaveRequest request) {
        contestService.updateContest(contestId, request);
        return Result.success();
    }

    @PutMapping("/contests/{contestId}/status")
    public Result<Void> changeContestStatus(@PathVariable Long contestId,
                                            @RequestParam String status) {
        contestService.changeContestStatus(contestId, status);
        return Result.success();
    }

    @DeleteMapping("/contests/{contestId}")
    public Result<Void> deleteContest(@PathVariable Long contestId) {
        contestService.deleteContest(contestId);
        return Result.success();
    }
}
