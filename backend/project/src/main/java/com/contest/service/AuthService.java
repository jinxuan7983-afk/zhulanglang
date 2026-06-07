package com.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.dto.auth.LoginRequest;
import com.contest.dto.auth.LoginResponse;
import com.contest.dto.auth.RegisterRequest;
import com.contest.entity.User;
import com.contest.exception.BusinessException;
import com.contest.mapper.UserMapper;
import com.contest.util.JwtUtil;
import com.contest.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        User existByStudentNo = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentNo, request.getStudentNo())
        );
        if (existByStudentNo != null) {
            throw new BusinessException("学号已存在");
        }

        User existByEmail = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
        );
        if (existByEmail != null) {
            throw new BusinessException("邮箱已存在");
        }

        User user = new User();
        user.setStudentNo(request.getStudentNo());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setName(request.getName());
        user.setCollege(request.getCollege());
        user.setPhone(request.getPhone());
        user.setRole("student");
        user.setStatus("normal");
        user.setLoginFailCount(0);
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getStudentNo, request.getAccount())
                        .or()
                        .eq(User::getEmail, request.getAccount())
        );
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        if (isLocked(user)) {
            throw new BusinessException("账号已锁定，请15分钟后再试");
        }

        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            handleLoginFail(user);
            throw new BusinessException("密码错误");
        }

        resetLoginStatus(user);
        String token = jwtUtil.generateToken(user.getUserId(), user.getName(), user.getRole());
        return new LoginResponse(
                token,
                user.getUserId(),
                user.getName(),
                user.getRole(),
                user.getEmail(),
                user.getStudentNo()
        );
    }

    private boolean isLocked(User user) {
        return "locked".equals(user.getStatus())
                && user.getLockUntil() != null
                && user.getLockUntil().isAfter(LocalDateTime.now());
    }

    private void handleLoginFail(User user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        failCount++;
        user.setLoginFailCount(failCount);
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            user.setStatus("locked");
            user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }
        userMapper.updateById(user);
    }

    private void resetLoginStatus(User user) {
        user.setLoginFailCount(0);
        user.setStatus("normal");
        user.setLockUntil(null);
        userMapper.updateById(user);
    }
}
