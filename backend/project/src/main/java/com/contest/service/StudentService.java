package com.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.dto.student.CreateTeamRequest;
import com.contest.dto.student.EnrollmentRecordResponse;
import com.contest.dto.student.JoinTeamRequest;
import com.contest.dto.student.PersonalEnrollRequest;
import com.contest.dto.student.ReviewTeamMemberRequest;
import com.contest.dto.student.SubmitTeamEnrollmentRequest;
import com.contest.dto.student.TeamDetailResponse;
import com.contest.dto.student.TeamMemberItemResponse;
import com.contest.dto.student.TeamResponse;
import com.contest.entity.Contest;
import com.contest.entity.Enrollment;
import com.contest.entity.Team;
import com.contest.entity.TeamMember;
import com.contest.entity.User;
import com.contest.exception.BusinessException;
import com.contest.mapper.ContestMapper;
import com.contest.mapper.EnrollmentMapper;
import com.contest.mapper.TeamMapper;
import com.contest.mapper.TeamMemberMapper;
import com.contest.mapper.UserMapper;
import com.contest.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final EnrollmentMapper enrollmentMapper;
    private final ContestMapper contestMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void personalEnroll(PersonalEnrollRequest request) {
        Long userId = getCurrentStudentUserId();
        Contest contest = getAvailableContest(request.getContestId());
        if (!"individual".equals(contest.getRegistrationType()) && !"both".equals(contest.getRegistrationType())) {
            throw new BusinessException("该竞赛不支持个人报名");
        }

        Enrollment existEnrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getUserId, userId)
                        .eq(Enrollment::getContestId, request.getContestId())
                        .ne(Enrollment::getStatus, "cancelled")
                        .last("limit 1")
        );
        if (existEnrollment != null) {
            throw new BusinessException("你已报名该竞赛");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setContestId(request.getContestId());
        enrollment.setStatus(Boolean.TRUE.equals(contest.getNeedReview()) ? "pending" : "approved");
        enrollmentMapper.insert(enrollment);
    }

    public List<EnrollmentRecordResponse> listMyEnrollments() {
        Long userId = getCurrentStudentUserId();
        List<Enrollment> enrollments = enrollmentMapper.selectList(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getUserId, userId)
                        .orderByDesc(Enrollment::getRegistrationTime)
        );

        return enrollments.stream().map(item -> {
            Contest contest = contestMapper.selectById(item.getContestId());
            EnrollmentRecordResponse response = new EnrollmentRecordResponse();
            response.setEnrollmentId(item.getEnrollmentId());
            response.setContestId(item.getContestId());
            response.setContestName(contest == null ? null : contest.getName());
            response.setTeamId(item.getTeamId());
            response.setStatus(item.getStatus());
            response.setReviewComment(item.getReviewComment());
            response.setRegistrationTime(item.getRegistrationTime());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamResponse createTeam(CreateTeamRequest request) {
        Long userId = getCurrentStudentUserId();
        Contest contest = getAvailableContest(request.getContestId());
        if (!"team".equals(contest.getRegistrationType()) && !"both".equals(contest.getRegistrationType())) {
            throw new BusinessException("该竞赛不支持团队报名");
        }

        validateNoTeamParticipation(userId, request.getContestId());

        Team team = new Team();
        team.setContestId(request.getContestId());
        team.setCaptainId(userId);
        team.setTeamName(request.getTeamName().trim());
        team.setInviteCode(generateInviteCode());
        team.setStatus("pending");
        teamMapper.insert(team);

        TeamMember captainMember = new TeamMember();
        captainMember.setTeamId(team.getTeamId());
        captainMember.setUserId(userId);
        captainMember.setStatus("approved");
        captainMember.setReviewTime(LocalDateTime.now());
        teamMemberMapper.insert(captainMember);

        return new TeamResponse(team.getTeamId(), team.getTeamName(), team.getInviteCode(), team.getStatus());
    }

    @Transactional(rollbackFor = Exception.class)
    public void joinTeam(JoinTeamRequest request) {
        Long userId = getCurrentStudentUserId();
        Team team = teamMapper.selectOne(
                new LambdaQueryWrapper<Team>()
                        .eq(Team::getInviteCode, request.getInviteCode().trim())
                        .last("limit 1")
        );
        if (team == null) {
            throw new BusinessException("邀请码无效");
        }
        if (team.getCaptainId().equals(userId)) {
            throw new BusinessException("你已是该团队队长");
        }

        Contest contest = getAvailableContest(team.getContestId());
        if (!"team".equals(contest.getRegistrationType()) && !"both".equals(contest.getRegistrationType())) {
            throw new BusinessException("该竞赛不支持团队报名");
        }

        validateNoTeamParticipation(userId, team.getContestId());

        long approvedCount = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, team.getTeamId())
                        .eq(TeamMember::getStatus, "approved")
        );
        if (contest.getMaxTeamSize() != null && approvedCount >= contest.getMaxTeamSize()) {
            throw new BusinessException("团队人数已满");
        }

        TeamMember existingApply = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, team.getTeamId())
                        .eq(TeamMember::getUserId, userId)
                        .last("limit 1")
        );
        if (existingApply != null) {
            throw new BusinessException("你已申请过该团队");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getTeamId());
        member.setUserId(userId);
        member.setStatus("applying");
        teamMemberMapper.insert(member);
    }

    public TeamDetailResponse getTeamDetail(Long teamId) {
        Long userId = getCurrentStudentUserId();
        Team team = getTeamOrThrow(teamId);
        validateTeamParticipant(teamId, userId);

        Contest contest = contestMapper.selectById(team.getContestId());
        User captain = userMapper.selectById(team.getCaptainId());
        List<TeamMember> memberList = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .orderByAsc(TeamMember::getApplyTime)
        );

        List<TeamMemberItemResponse> members = memberList.stream()
                .map(this::toTeamMemberItemResponse)
                .sorted(Comparator.comparing(
                        item -> !"approved".equals(item.getStatus())
                ))
                .collect(Collectors.toList());

        TeamDetailResponse response = new TeamDetailResponse();
        response.setTeamId(team.getTeamId());
        response.setContestId(team.getContestId());
        response.setContestName(contest == null ? null : contest.getName());
        response.setTeamName(team.getTeamName());
        response.setCaptainId(team.getCaptainId());
        response.setCaptainName(captain == null ? null : captain.getName());
        response.setInviteCode(team.getCaptainId().equals(userId) ? team.getInviteCode() : null);
        response.setStatus(team.getStatus());
        response.setRegistrationMaterial(team.getRegistrationMaterial());
        response.setReviewComment(team.getReviewComment());
        response.setSubmitTime(team.getSubmitTime());
        response.setApprovedMemberCount((int) memberList.stream().filter(item -> "approved".equals(item.getStatus())).count());
        response.setMinTeamSize(contest == null ? null : contest.getMinTeamSize());
        response.setMaxTeamSize(contest == null ? null : contest.getMaxTeamSize());
        response.setMembers(members);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewTeamMember(Long teamId, ReviewTeamMemberRequest request) {
        Long userId = getCurrentStudentUserId();
        Team team = getCaptainTeam(teamId, userId);
        TeamMember member = teamMemberMapper.selectById(request.getMemberId());
        if (member == null || !teamId.equals(member.getTeamId())) {
            throw new BusinessException("成员申请记录不存在");
        }
        if (!"applying".equals(member.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            Contest contest = contestMapper.selectById(team.getContestId());
            long approvedCount = teamMemberMapper.selectCount(
                    new LambdaQueryWrapper<TeamMember>()
                            .eq(TeamMember::getTeamId, teamId)
                            .eq(TeamMember::getStatus, "approved")
            );
            if (contest != null && contest.getMaxTeamSize() != null && approvedCount >= contest.getMaxTeamSize()) {
                throw new BusinessException("团队人数已满，无法再通过申请");
            }
            member.setStatus("approved");
        } else {
            member.setStatus("rejected");
        }
        member.setReviewTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitTeamEnrollment(Long teamId, SubmitTeamEnrollmentRequest request) {
        Long userId = getCurrentStudentUserId();
        Team team = getCaptainTeam(teamId, userId);
        Contest contest = getAvailableContest(team.getContestId());
        if (!"team".equals(contest.getRegistrationType()) && !"both".equals(contest.getRegistrationType())) {
            throw new BusinessException("该竞赛不支持团队报名");
        }
        if ("reviewing".equals(team.getStatus()) || "approved".equals(team.getStatus())) {
            throw new BusinessException("团队已提交报名，请勿重复提交");
        }

        int approvedMemberCount = (int) teamMemberMapper.selectCount(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getStatus, "approved")
        );
        if (contest.getMinTeamSize() != null && approvedMemberCount < contest.getMinTeamSize()) {
            throw new BusinessException("团队人数未达到最小要求");
        }
        if (contest.getMaxTeamSize() != null && approvedMemberCount > contest.getMaxTeamSize()) {
            throw new BusinessException("团队人数超过最大限制");
        }

        Enrollment existEnrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getTeamId, teamId)
                        .ne(Enrollment::getStatus, "cancelled")
                        .last("limit 1")
        );
        if (existEnrollment != null) {
            throw new BusinessException("团队已提交报名，请勿重复提交");
        }

        team.setRegistrationMaterial(StringUtils.hasText(request.getRegistrationMaterial())
                ? request.getRegistrationMaterial().trim()
                : null);
        team.setSubmitTime(LocalDateTime.now());
        team.setStatus(Boolean.TRUE.equals(contest.getNeedReview()) ? "reviewing" : "approved");
        teamMapper.updateById(team);

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setContestId(team.getContestId());
        enrollment.setTeamId(teamId);
        enrollment.setAttachmentUrl(team.getRegistrationMaterial());
        enrollment.setStatus(Boolean.TRUE.equals(contest.getNeedReview()) ? "pending" : "approved");
        enrollmentMapper.insert(enrollment);
    }

    private Long getCurrentStudentUserId() {
        if (!"student".equals(UserContext.getRole())) {
            throw new BusinessException(403, "仅学生可操作");
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    private Contest getAvailableContest(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null || !"published".equals(contest.getStatus())) {
            throw new BusinessException("竞赛不存在或未上架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (contest.getRegistrationStart() != null && contest.getRegistrationStart().isAfter(now)) {
            throw new BusinessException("报名尚未开始");
        }
        if (contest.getRegistrationEnd() != null && contest.getRegistrationEnd().isBefore(now)) {
            throw new BusinessException("报名已截止");
        }
        return contest;
    }

    private Team getTeamOrThrow(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }
        return team;
    }

    private Team getCaptainTeam(Long teamId, Long userId) {
        Team team = getTeamOrThrow(teamId);
        if (!userId.equals(team.getCaptainId())) {
            throw new BusinessException(403, "仅队长可操作");
        }
        return team;
    }

    private void validateTeamParticipant(Long teamId, Long userId) {
        Team team = getTeamOrThrow(teamId);
        if (userId.equals(team.getCaptainId())) {
            return;
        }

        TeamMember member = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, userId)
                        .in(TeamMember::getStatus, "applying", "approved")
                        .last("limit 1")
        );
        if (member == null) {
            throw new BusinessException(403, "无权查看该团队");
        }
    }

    private TeamMemberItemResponse toTeamMemberItemResponse(TeamMember member) {
        User user = userMapper.selectById(member.getUserId());
        TeamMemberItemResponse response = new TeamMemberItemResponse();
        response.setMemberId(member.getId());
        response.setUserId(member.getUserId());
        response.setName(user == null ? null : user.getName());
        response.setCollege(user == null ? null : user.getCollege());
        response.setPhone(user == null ? null : user.getPhone());
        response.setStatus(member.getStatus());
        response.setApplyTime(member.getApplyTime());
        response.setReviewTime(member.getReviewTime());
        return response;
    }

    private void validateNoTeamParticipation(Long userId, Long contestId) {
        Team captainTeam = teamMapper.selectOne(
                new LambdaQueryWrapper<Team>()
                        .eq(Team::getContestId, contestId)
                        .eq(Team::getCaptainId, userId)
                        .last("limit 1")
        );
        if (captainTeam != null) {
            throw new BusinessException("你已在该竞赛创建团队");
        }

        List<Team> contestTeams = teamMapper.selectList(
                new LambdaQueryWrapper<Team>().eq(Team::getContestId, contestId)
        );
        if (contestTeams.isEmpty()) {
            return;
        }

        List<Long> teamIds = contestTeams.stream().map(Team::getTeamId).collect(Collectors.toList());
        TeamMember joinedMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .in(TeamMember::getTeamId, teamIds)
                        .eq(TeamMember::getUserId, userId)
                        .in(TeamMember::getStatus, "applying", "approved")
                        .last("limit 1")
        );
        if (joinedMember != null) {
            throw new BusinessException("你已参与该竞赛的其他团队");
        }
    }

    private String generateInviteCode() {
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            String inviteCode = String.format("%06d", random.nextInt(1_000_000));
            Team exist = teamMapper.selectOne(
                    new LambdaQueryWrapper<Team>()
                            .eq(Team::getInviteCode, inviteCode)
                            .last("limit 1")
            );
            if (exist == null) {
                return inviteCode;
            }
        }
        throw new BusinessException("邀请码生成失败，请稍后重试");
    }
}
