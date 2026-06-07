package com.contest.controller;

import com.contest.common.PageResult;
import com.contest.common.Result;
import com.contest.dto.contest.ContestDetailResponse;
import com.contest.dto.contest.ContestListItemResponse;
import com.contest.dto.contest.ContestQueryRequest;
import com.contest.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;

    @GetMapping
    public Result<PageResult<ContestListItemResponse>> pagePublicContests(ContestQueryRequest request) {
        return Result.success(contestService.pagePublicContests(request));
    }

    @GetMapping("/{contestId}")
    public Result<ContestDetailResponse> getPublicContestDetail(@PathVariable Long contestId) {
        return Result.success(contestService.getPublicContestDetail(contestId));
    }
}
