package com.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.dto.review.EnrollmentReviewItemResponse;
import com.contest.dto.review.ReviewRequest;
import com.contest.dto.review.TeacherContestResponse;
import com.contest.dto.review.TeamReviewItemResponse;
import com.contest.entity.Contest;
import com.contest.entity.Enrollment;
import com.contest.entity.Team;
import com.contest.entity.TeacherContest;
import com.contest.entity.User;
import com.contest.exception.BusinessException;
import com.contest.mapper.ContestMapper;
import com.contest.mapper.EnrollmentMapper;
import com.contest.mapper.TeamMapper;
import com.contest.mapper.TeacherContestMapper;
import com.contest.mapper.UserMapper;
import com.contest.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final EnrollmentMapper enrollmentMapper;
    private final TeamMapper teamMapper;
    private final ContestMapper contestMapper;
    private final UserMapper userMapper;
    private final TeacherContestMapper teacherContestMapper;

    public List<TeacherContestResponse> listTeacherContests() {
        Long teacherId = requireRole("teacher");
        List<TeacherContest> relations = teacherContestMapper.selectList(
                new LambdaQueryWrapper<TeacherContest>()
                        .eq(TeacherContest::getTeacherId, teacherId)
                        .orderByDesc(TeacherContest::getCreateTime)
        );
        return relations.stream()
                .map(TeacherContest::getContestId)
                .distinct()
                .map(contestMapper::selectById)
                .filter(contest -> contest != null)
                .map(contest -> new TeacherContestResponse(
                        contest.getContestId(),
                        contest.getName(),
                        contest.getCategory(),
                        contest.getLevel(),
                        contest.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public List<EnrollmentReviewItemResponse> listTeacherEnrollments(Long contestId, String status) {
        Long teacherId = requireRole("teacher");
        validateTeacherContestPermission(teacherId, contestId);
        return listEnrollmentsInternal(contestId, status);
    }

    public List<TeamReviewItemResponse> listTeacherTeams(Long contestId, String status) {
        Long teacherId = requireRole("teacher");
        validateTeacherContestPermission(teacherId, contestId);
        return listTeamsInternal(contestId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewTeacherEnrollment(Long enrollmentId, ReviewRequest request) {
        Long teacherId = requireRole("teacher");
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        validateTeacherContestPermission(teacherId, enrollment.getContestId());
        reviewEnrollment(enrollment, teacherId, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewTeacherTeam(Long teamId, ReviewRequest request) {
        Long teacherId = requireRole("teacher");
        Team team = getTeamOrThrow(teamId);
        validateTeacherContestPermission(teacherId, team.getContestId());
        reviewTeam(team, teacherId, request);
    }

    public List<EnrollmentReviewItemResponse> listAdminEnrollments(Long contestId, String status) {
        requireRole("admin");
        return listEnrollmentsInternal(contestId, status);
    }

    public List<TeamReviewItemResponse> listAdminTeams(Long contestId, String status) {
        requireRole("admin");
        return listTeamsInternal(contestId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewAdminEnrollment(Long enrollmentId, ReviewRequest request) {
        Long adminId = requireRole("admin");
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        reviewEnrollment(enrollment, adminId, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewAdminTeam(Long teamId, ReviewRequest request) {
        Long adminId = requireRole("admin");
        Team team = getTeamOrThrow(teamId);
        reviewTeam(team, adminId, request);
    }

    private List<EnrollmentReviewItemResponse> listEnrollmentsInternal(Long contestId, String status) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .orderByDesc(Enrollment::getRegistrationTime);
        if (contestId != null) {
            wrapper.eq(Enrollment::getContestId, contestId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Enrollment::getStatus, status.trim());
        }
        return enrollmentMapper.selectList(wrapper).stream()
                .map(this::toEnrollmentReviewItem)
                .collect(Collectors.toList());
    }

    private List<TeamReviewItemResponse> listTeamsInternal(Long contestId, String status) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<Team>()
                .orderByDesc(Team::getSubmitTime)
                .orderByDesc(Team::getCreateTime);
        if (contestId != null) {
            wrapper.eq(Team::getContestId, contestId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Team::getStatus, status.trim());
        }
        return teamMapper.selectList(wrapper).stream()
                .map(this::toTeamReviewItem)
                .collect(Collectors.toList());
    }

    private void reviewEnrollment(Enrollment enrollment, Long reviewerId, ReviewRequest request) {
        if (!"pending".equals(enrollment.getStatus())) {
            throw new BusinessException("该报名已审核");
        }
        enrollment.setStatus(Boolean.TRUE.equals(request.getApproved()) ? "approved" : "rejected");
        enrollment.setReviewComment(normalizeComment(request.getReviewComment()));
        enrollment.setReviewerId(reviewerId);
        enrollment.setReviewTime(LocalDateTime.now());
        enrollmentMapper.updateById(enrollment);
    }

    private void reviewTeam(Team team, Long reviewerId, ReviewRequest request) {
        if (!"reviewing".equals(team.getStatus())) {
            throw new BusinessException("该团队报名未处于待审核状态");
        }
        String resultStatus = Boolean.TRUE.equals(request.getApproved()) ? "approved" : "rejected";
        team.setStatus(resultStatus);
        team.setReviewComment(normalizeComment(request.getReviewComment()));
        team.setReviewerId(reviewerId);
        team.setReviewTime(LocalDateTime.now());
        teamMapper.updateById(team);

        Enrollment enrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getTeamId, team.getTeamId())
                        .last("limit 1")
        );
        if (enrollment != null && "pending".equals(enrollment.getStatus())) {
            enrollment.setStatus(Boolean.TRUE.equals(request.getApproved()) ? "approved" : "rejected");
            enrollment.setReviewComment(normalizeComment(request.getReviewComment()));
            enrollment.setReviewerId(reviewerId);
            enrollment.setReviewTime(LocalDateTime.now());
            enrollmentMapper.updateById(enrollment);
        }
    }

    private void validateTeacherContestPermission(Long teacherId, Long contestId) {
        if (contestId == null) {
            throw new BusinessException("竞赛ID不能为空");
        }
        TeacherContest relation = teacherContestMapper.selectOne(
                new LambdaQueryWrapper<TeacherContest>()
                        .eq(TeacherContest::getTeacherId, teacherId)
                        .eq(TeacherContest::getContestId, contestId)
                        .last("limit 1")
        );
        if (relation == null) {
            throw new BusinessException(403, "无权操作该竞赛");
        }
    }

    private Long requireRole(String role) {
        if (!role.equals(UserContext.getRole())) {
            throw new BusinessException(403, "无权操作");
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    private Enrollment getEnrollmentOrThrow(Long enrollmentId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("报名记录不存在");
        }
        return enrollment;
    }

    private Team getTeamOrThrow(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }
        return team;
    }

    private EnrollmentReviewItemResponse toEnrollmentReviewItem(Enrollment enrollment) {
        Contest contest = contestMapper.selectById(enrollment.getContestId());
        User student = userMapper.selectById(enrollment.getUserId());
        EnrollmentReviewItemResponse response = new EnrollmentReviewItemResponse();
        response.setEnrollmentId(enrollment.getEnrollmentId());
        response.setContestId(enrollment.getContestId());
        response.setContestName(contest == null ? null : contest.getName());
        response.setUserId(enrollment.getUserId());
        response.setStudentName(student == null ? null : student.getName());
        response.setStudentNo(student == null ? null : student.getStudentNo());
        response.setStatus(enrollment.getStatus());
        response.setReviewComment(enrollment.getReviewComment());
        response.setRegistrationTime(enrollment.getRegistrationTime());
        response.setReviewTime(enrollment.getReviewTime());
        response.setReviewerId(enrollment.getReviewerId());
        response.setTeamId(enrollment.getTeamId());
        return response;
    }

    private TeamReviewItemResponse toTeamReviewItem(Team team) {
        Contest contest = contestMapper.selectById(team.getContestId());
        User captain = userMapper.selectById(team.getCaptainId());
        TeamReviewItemResponse response = new TeamReviewItemResponse();
        response.setTeamId(team.getTeamId());
        response.setContestId(team.getContestId());
        response.setContestName(contest == null ? null : contest.getName());
        response.setTeamName(team.getTeamName());
        response.setCaptainId(team.getCaptainId());
        response.setCaptainName(captain == null ? null : captain.getName());
        response.setInviteCode(team.getInviteCode());
        response.setStatus(team.getStatus());
        response.setReviewComment(team.getReviewComment());
        response.setRegistrationMaterial(team.getRegistrationMaterial());
        response.setSubmitTime(team.getSubmitTime());
        response.setReviewTime(team.getReviewTime());
        response.setReviewerId(team.getReviewerId());
        return response;
    }

    private String normalizeComment(String comment) {
        return StringUtils.hasText(comment) ? comment.trim() : null;
    }
}
