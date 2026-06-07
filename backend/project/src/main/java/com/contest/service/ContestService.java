package com.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contest.common.PageResult;
import com.contest.dto.contest.AdminContestItemResponse;
import com.contest.dto.contest.AdminContestQueryRequest;
import com.contest.dto.contest.ContestDetailResponse;
import com.contest.dto.contest.ContestListItemResponse;
import com.contest.dto.contest.ContestQueryRequest;
import com.contest.dto.contest.ContestSaveRequest;
import com.contest.entity.Contest;
import com.contest.entity.Enrollment;
import com.contest.exception.BusinessException;
import com.contest.mapper.ContestMapper;
import com.contest.mapper.EnrollmentMapper;
import com.contest.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {
    private final ContestMapper contestMapper;
    private final EnrollmentMapper enrollmentMapper;

    public PageResult<ContestListItemResponse> pagePublicContests(ContestQueryRequest request) {
        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1L : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 12L : request.getPageSize();
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<Contest>()
                .eq(Contest::getStatus, "published")
                .orderByDesc(Contest::getCreateTime);

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Contest::getName, request.getKeyword().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Contest::getCategory, request.getCategory().trim());
        }
        if (StringUtils.hasText(request.getProgressStatus())) {
            String progressStatus = request.getProgressStatus().trim();
            if ("upcoming".equals(progressStatus)) {
                wrapper.gt(Contest::getRegistrationStart, now);
            } else if ("registering".equals(progressStatus)) {
                wrapper.le(Contest::getRegistrationStart, now)
                        .ge(Contest::getRegistrationEnd, now);
            } else if ("ended".equals(progressStatus)) {
                wrapper.lt(Contest::getRegistrationEnd, now);
            }
        }

        Page<Contest> page = contestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ContestListItemResponse> records = page.getRecords()
                .stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), pageNum, pageSize, records);
    }

    public ContestDetailResponse getPublicContestDetail(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null || !"published".equals(contest.getStatus())) {
            throw new BusinessException("竞赛不存在或未上架");
        }
        return toDetailResponse(contest);
    }

    public PageResult<AdminContestItemResponse> pageAdminContests(AdminContestQueryRequest request) {
        requireAdmin();
        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1L : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10L : request.getPageSize();

        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<Contest>()
                .orderByDesc(Contest::getCreateTime);

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Contest::getName, request.getKeyword().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Contest::getCategory, request.getCategory().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Contest::getStatus, request.getStatus().trim());
        }

        Page<Contest> page = contestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdminContestItemResponse> records = page.getRecords().stream()
                .map(this::toAdminItemResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), pageNum, pageSize, records);
    }

    public ContestDetailResponse getAdminContestDetail(Long contestId) {
        requireAdmin();
        Contest contest = getContestOrThrow(contestId);
        return toDetailResponse(contest);
    }

    public Long createContest(ContestSaveRequest request) {
        Long adminId = requireAdmin();
        validateContestRequest(request);

        Contest contest = new Contest();
        applyContestRequest(contest, request);
        contest.setCreateBy(adminId);
        contest.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "not_started");
        contestMapper.insert(contest);
        return contest.getContestId();
    }

    public void updateContest(Long contestId, ContestSaveRequest request) {
        requireAdmin();
        validateContestRequest(request);

        Contest contest = getContestOrThrow(contestId);
        boolean hasEnrollment = hasEnrollmentRecord(contestId);
        if (hasEnrollment) {
            boolean restrictedChanged =
                    !safeEquals(contest.getRegistrationType(), request.getRegistrationType())
                            || !safeEquals(contest.getMinTeamSize(), request.getMinTeamSize())
                            || !safeEquals(contest.getMaxTeamSize(), request.getMaxTeamSize())
                            || !safeEquals(contest.getRegistrationStart(), request.getRegistrationStart())
                            || !safeEquals(contest.getRegistrationEnd(), request.getRegistrationEnd())
                            || !safeEquals(contest.getContestTime(), request.getContestTime());
            if (restrictedChanged) {
                throw new BusinessException("已有报名记录时，报名类型、人数范围和时间不可修改");
            }
        }

        applyContestRequest(contest, request);
        if (StringUtils.hasText(request.getStatus())) {
            contest.setStatus(request.getStatus().trim());
        }
        contestMapper.updateById(contest);
    }

    public void changeContestStatus(Long contestId, String status) {
        requireAdmin();
        Contest contest = getContestOrThrow(contestId);
        if (!"published".equals(status) && !"not_started".equals(status) && !"ended".equals(status)) {
            throw new BusinessException("竞赛状态不合法");
        }
        contest.setStatus(status);
        contestMapper.updateById(contest);
    }

    public void deleteContest(Long contestId) {
        requireAdmin();
        Contest contest = getContestOrThrow(contestId);
        if (contest.getContestTime() != null && !contest.getContestTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("仅可删除未开始的竞赛");
        }
        if (hasEnrollmentRecord(contestId)) {
            throw new BusinessException("已有报名记录，无法删除竞赛");
        }
        contestMapper.deleteById(contestId);
    }

    private ContestListItemResponse toListItemResponse(Contest contest) {
        ContestListItemResponse response = new ContestListItemResponse();
        response.setContestId(contest.getContestId());
        response.setName(contest.getName());
        response.setCategory(contest.getCategory());
        response.setLevel(contest.getLevel());
        response.setCoverImage(contest.getCoverImage());
        response.setOrganizer(contest.getOrganizer());
        response.setLocation(contest.getLocation());
        response.setRegistrationType(contest.getRegistrationType());
        response.setRegistrationStart(contest.getRegistrationStart());
        response.setRegistrationEnd(contest.getRegistrationEnd());
        response.setContestTime(contest.getContestTime());
        response.setProgressStatus(calculateProgressStatus(contest));
        return response;
    }

    private AdminContestItemResponse toAdminItemResponse(Contest contest) {
        AdminContestItemResponse response = new AdminContestItemResponse();
        response.setContestId(contest.getContestId());
        response.setName(contest.getName());
        response.setCategory(contest.getCategory());
        response.setLevel(contest.getLevel());
        response.setStatus(contest.getStatus());
        response.setRegistrationType(contest.getRegistrationType());
        response.setNeedReview(contest.getNeedReview());
        response.setRegistrationStart(contest.getRegistrationStart());
        response.setRegistrationEnd(contest.getRegistrationEnd());
        response.setContestTime(contest.getContestTime());
        response.setCreateTime(contest.getCreateTime());
        response.setProgressStatus(calculateProgressStatus(contest));
        return response;
    }

    private ContestDetailResponse toDetailResponse(Contest contest) {
        ContestDetailResponse response = new ContestDetailResponse();
        response.setContestId(contest.getContestId());
        response.setName(contest.getName());
        response.setCategory(contest.getCategory());
        response.setLevel(contest.getLevel());
        response.setRegistrationStart(contest.getRegistrationStart());
        response.setRegistrationEnd(contest.getRegistrationEnd());
        response.setContestTime(contest.getContestTime());
        response.setLocation(contest.getLocation());
        response.setOrganizer(contest.getOrganizer());
        response.setCoverImage(contest.getCoverImage());
        response.setAttachmentUrl(contest.getAttachmentUrl());
        response.setRegistrationType(contest.getRegistrationType());
        response.setMinTeamSize(contest.getMinTeamSize());
        response.setMaxTeamSize(contest.getMaxTeamSize());
        response.setNeedReview(contest.getNeedReview());
        response.setProgressStatus(calculateProgressStatus(contest));
        return response;
    }

    private String calculateProgressStatus(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        if (contest.getRegistrationStart() != null && contest.getRegistrationStart().isAfter(now)) {
            return "upcoming";
        }
        if (contest.getRegistrationEnd() != null && contest.getRegistrationEnd().isBefore(now)) {
            return "ended";
        }
        return "registering";
    }

    private void applyContestRequest(Contest contest, ContestSaveRequest request) {
        contest.setName(request.getName().trim());
        contest.setCategory(request.getCategory().trim());
        contest.setLevel(request.getLevel().trim());
        contest.setRegistrationStart(request.getRegistrationStart());
        contest.setRegistrationEnd(request.getRegistrationEnd());
        contest.setContestTime(request.getContestTime());
        contest.setLocation(trimToNull(request.getLocation()));
        contest.setOrganizer(trimToNull(request.getOrganizer()));
        contest.setCoverImage(trimToNull(request.getCoverImage()));
        contest.setAttachmentUrl(trimToNull(request.getAttachmentUrl()));
        contest.setRegistrationType(request.getRegistrationType().trim());
        contest.setMinTeamSize(request.getMinTeamSize());
        contest.setMaxTeamSize(request.getMaxTeamSize());
        contest.setNeedReview(request.getNeedReview());
    }

    private void validateContestRequest(ContestSaveRequest request) {
        if (!request.getRegistrationStart().isBefore(request.getRegistrationEnd())) {
            throw new BusinessException("报名开始时间必须早于报名结束时间");
        }
        if (request.getContestTime().isBefore(request.getRegistrationEnd())) {
            throw new BusinessException("竞赛时间不能早于报名结束时间");
        }
        if (!"individual".equals(request.getRegistrationType())
                && !"team".equals(request.getRegistrationType())
                && !"both".equals(request.getRegistrationType())) {
            throw new BusinessException("报名类型不合法");
        }
        if (("team".equals(request.getRegistrationType()) || "both".equals(request.getRegistrationType()))
                && request.getMinTeamSize() != null
                && request.getMaxTeamSize() != null
                && request.getMinTeamSize() > request.getMaxTeamSize()) {
            throw new BusinessException("最小团队人数不能大于最大团队人数");
        }
        if ("individual".equals(request.getRegistrationType())) {
            request.setMinTeamSize(null);
            request.setMaxTeamSize(null);
        }
    }

    private Contest getContestOrThrow(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        return contest;
    }

    private boolean hasEnrollmentRecord(Long contestId) {
        return enrollmentMapper.selectCount(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getContestId, contestId)
        ) > 0;
    }

    private Long requireAdmin() {
        if (!"admin".equals(UserContext.getRole())) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private boolean safeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
