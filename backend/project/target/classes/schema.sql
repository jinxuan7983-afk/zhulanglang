
-- 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户编号',
    student_no VARCHAR(50) UNIQUE NOT NULL COMMENT '学号/工号',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    college VARCHAR(100) COMMENT '学院',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    role ENUM('student', 'teacher', 'admin') NOT NULL DEFAULT 'student' COMMENT '角色',
    status ENUM('normal', 'locked') DEFAULT 'normal' COMMENT '状态',
    lock_until DATETIME COMMENT '锁定截止时间',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 竞赛表
CREATE TABLE IF NOT EXISTS contest (
    contest_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '竞赛编号',
    name VARCHAR(200) NOT NULL COMMENT '竞赛名称',
    category VARCHAR(100) COMMENT '竞赛类别',
    level VARCHAR(50) COMMENT '竞赛级别',
    registration_start DATETIME NOT NULL COMMENT '报名开始时间',
    registration_end DATETIME NOT NULL COMMENT '报名结束时间',
    contest_time DATETIME NOT NULL COMMENT '竞赛时间',
    location VARCHAR(100) COMMENT '竞赛地点',
    organizer VARCHAR(100) COMMENT '主办方',
    cover_image VARCHAR(255) COMMENT '封面图URL',
    attachment_url VARCHAR(255) COMMENT '附件URL',
    registration_type ENUM('individual', 'team', 'both') NOT NULL DEFAULT 'both' COMMENT '报名类型',
    min_team_size INT DEFAULT 2 COMMENT '最小团队人数',
    max_team_size INT DEFAULT 10 COMMENT '最大团队人数',
    need_review TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要审核',
    status ENUM('not_started', 'published', 'ended') NOT NULL DEFAULT 'not_started' COMMENT '竞赛状态',
    create_by BIGINT NOT NULL COMMENT '创建人编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    FOREIGN KEY (create_by) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- 团队表
CREATE TABLE IF NOT EXISTS team (
    team_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队ID',
    contest_id BIGINT NOT NULL COMMENT '所属竞赛ID',
    captain_id BIGINT NOT NULL COMMENT '队长ID',
    team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
    invite_code VARCHAR(6) UNIQUE NOT NULL COMMENT '邀请码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    status ENUM('pending', 'reviewing', 'approved', 'rejected') DEFAULT 'pending' COMMENT '团队报名状态',
    registration_material VARCHAR(255) COMMENT '报名材料URL',
    review_comment VARCHAR(500) COMMENT '审核意见',
    submit_time DATETIME COMMENT '提交报名时间',
    review_time DATETIME COMMENT '审核时间',
    reviewer_id BIGINT COMMENT '审核人ID',
    FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    FOREIGN KEY (captain_id) REFERENCES user(user_id),
    FOREIGN KEY (reviewer_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 报名表
CREATE TABLE IF NOT EXISTS enrollment (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contest_id BIGINT NOT NULL COMMENT '竞赛ID',
    team_id BIGINT COMMENT '团队ID(个人赛为空)',
    status ENUM('pending', 'approved', 'rejected', 'cancelled') DEFAULT 'pending' COMMENT '报名状态',
    review_comment VARCHAR(500) COMMENT '审核意见',
    registration_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    review_time DATETIME COMMENT '审核时间',
    reviewer_id BIGINT COMMENT '审核人ID',
    attachment_url VARCHAR(255) COMMENT '附件URL',
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    FOREIGN KEY (team_id) REFERENCES team(team_id),
    FOREIGN KEY (reviewer_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- 团队成员表
CREATE TABLE IF NOT EXISTS team_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status ENUM('applying', 'approved', 'rejected') DEFAULT 'applying' COMMENT '成员状态',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请加入时间',
    review_time DATETIME COMMENT '审核时间',
    FOREIGN KEY (team_id) REFERENCES team(team_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- 教师指导表
CREATE TABLE IF NOT EXISTS teacher_contest (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录编号',
    teacher_id BIGINT NOT NULL COMMENT '老师编号',
    contest_id BIGINT NOT NULL COMMENT '竞赛编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
    FOREIGN KEY (teacher_id) REFERENCES user(user_id),
    FOREIGN KEY (contest_id) REFERENCES contest(contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师指导表';

-- 轮播图表
CREATE TABLE IF NOT EXISTS carousel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    title VARCHAR(200) COMMENT '标题',
    image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(255) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';
