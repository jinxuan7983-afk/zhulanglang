import pymysql, bcrypt

conn = pymysql.connect(host='localhost', user='root', password='123456', charset='utf8mb4')
cursor = conn.cursor()

# Create database
cursor.execute("CREATE DATABASE IF NOT EXISTS contest_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
cursor.execute("USE contest_db")

# Create tables one by one
tables = [
    """CREATE TABLE IF NOT EXISTS user (
        user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户编号',
        student_no VARCHAR(50) UNIQUE NOT NULL COMMENT '学号工号',
        email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
        password VARCHAR(255) NOT NULL COMMENT '密码',
        name VARCHAR(50) NOT NULL COMMENT '姓名',
        college VARCHAR(100) COMMENT '学院',
        phone VARCHAR(20) COMMENT '手机号',
        avatar VARCHAR(255) COMMENT '头像URL',
        role ENUM('student','teacher','admin') NOT NULL DEFAULT 'student' COMMENT '角色',
        status ENUM('normal','locked') DEFAULT 'normal' COMMENT '状态',
        lock_until DATETIME COMMENT '锁定截止时间',
        login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表'""",

    """CREATE TABLE IF NOT EXISTS contest (
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
        registration_type ENUM('individual','team','both') NOT NULL DEFAULT 'both' COMMENT '报名类型',
        min_team_size INT DEFAULT 2 COMMENT '最小团队人数',
        max_team_size INT DEFAULT 10 COMMENT '最大团队人数',
        need_review TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要审核',
        status ENUM('not_started','published','ended') NOT NULL DEFAULT 'not_started' COMMENT '竞赛状态',
        create_by BIGINT NOT NULL COMMENT '创建人编号',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
        FOREIGN KEY (create_by) REFERENCES user(user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表'""",

    """CREATE TABLE IF NOT EXISTS team (
        team_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队ID',
        contest_id BIGINT NOT NULL COMMENT '所属竞赛ID',
        captain_id BIGINT NOT NULL COMMENT '队长ID',
        team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
        invite_code VARCHAR(6) UNIQUE NOT NULL COMMENT '邀请码',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        status ENUM('pending','reviewing','approved','rejected') DEFAULT 'pending' COMMENT '团队报名状态',
        registration_material VARCHAR(255) COMMENT '报名材料URL',
        review_comment VARCHAR(500) COMMENT '审核意见',
        submit_time DATETIME COMMENT '提交报名时间',
        review_time DATETIME COMMENT '审核时间',
        reviewer_id BIGINT COMMENT '审核人ID',
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
        FOREIGN KEY (captain_id) REFERENCES user(user_id),
        FOREIGN KEY (reviewer_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表'""",

    """CREATE TABLE IF NOT EXISTS enrollment (
        enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名记录ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        contest_id BIGINT NOT NULL COMMENT '竞赛ID',
        team_id BIGINT COMMENT '团队ID',
        status ENUM('pending','approved','rejected','cancelled') DEFAULT 'pending' COMMENT '报名状态',
        review_comment VARCHAR(500) COMMENT '审核意见',
        registration_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
        review_time DATETIME COMMENT '审核时间',
        reviewer_id BIGINT COMMENT '审核人ID',
        attachment_url VARCHAR(255) COMMENT '附件URL',
        FOREIGN KEY (user_id) REFERENCES user(user_id),
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
        FOREIGN KEY (team_id) REFERENCES team(team_id),
        FOREIGN KEY (reviewer_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表'""",

    """CREATE TABLE IF NOT EXISTS team_member (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
        team_id BIGINT NOT NULL COMMENT '团队ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        status ENUM('applying','approved','rejected') DEFAULT 'applying' COMMENT '成员状态',
        apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请加入时间',
        review_time DATETIME COMMENT '审核时间',
        FOREIGN KEY (team_id) REFERENCES team(team_id),
        FOREIGN KEY (user_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表'""",

    """CREATE TABLE IF NOT EXISTS teacher_contest (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录编号',
        teacher_id BIGINT NOT NULL COMMENT '老师编号',
        contest_id BIGINT NOT NULL COMMENT '竞赛编号',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
        FOREIGN KEY (teacher_id) REFERENCES user(user_id),
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师指导表'""",

    """CREATE TABLE IF NOT EXISTS carousel (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
        title VARCHAR(200) COMMENT '标题',
        image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
        link_url VARCHAR(255) COMMENT '跳转链接',
        sort_order INT DEFAULT 0 COMMENT '排序',
        status TINYINT(1) DEFAULT 1 COMMENT '状态0禁用1启用',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表'"""
]

for sql in tables:
    try:
        cursor.execute(sql)
        print(f'OK: table created')
    except Exception as e:
        print(f'Error: {e}')

conn.commit()

# Generate passwords
admin_pw = bcrypt.hashpw('admin123'.encode(), bcrypt.gensalt()).decode()
teacher_pw = bcrypt.hashpw('teacher123'.encode(), bcrypt.gensalt()).decode()
student_pw = bcrypt.hashpw('123456'.encode(), bcrypt.gensalt()).decode()

# Insert default users
users = [
    ('admin001', 'admin@contest.com', admin_pw, '管理员', 'admin', None),
    ('teacher001', 'teacher@contest.com', teacher_pw, '李老师', 'teacher', '计算机学院'),
    ('2024001', 'student@contest.com', student_pw, '测试学生', 'student', '软件学院'),
]
for sno, email, pw, name, role, college in users:
    cursor.execute("SELECT COUNT(*) FROM user WHERE student_no=%s", (sno,))
    if cursor.fetchone()[0] == 0:
        cursor.execute("INSERT INTO user (student_no, email, password, name, role, college) VALUES (%s,%s,%s,%s,%s,%s)",
                       (sno, email, pw, name, role, college))
        print(f'Inserted {role}: {name}')

conn.commit()

# Insert sample contest by admin
cursor.execute("SELECT user_id FROM user WHERE student_no='admin001'")
admin_id = cursor.fetchone()[0]

cursor.execute("SELECT COUNT(*) FROM contest")
if cursor.fetchone()[0] == 0:
    from datetime import datetime, timedelta
    now = datetime.now()
    contests = [
        ('全国大学生数学建模竞赛', '学科竞赛', '国家级', now + timedelta(days=1), now + timedelta(days=15), now + timedelta(days=30), 
         '线上', '中国工业与应用数学学会', 'individual', 1, 1, True, 'published'),
        ('互联网+创新创业大赛', '创新创业', '国家级', now - timedelta(days=5), now + timedelta(days=20), now + timedelta(days=45),
         '福州理工学院', '教育部', 'both', 2, 10, True, 'published'),
        ('校园程序设计大赛', '学科竞赛', '校级', now - timedelta(days=10), now + timedelta(days=5), now + timedelta(days=14),
         '计算机学院', '福州理工学院', 'team', 2, 6, True, 'published'),
        ('大学生英语竞赛', '学科竞赛', '省级', now + timedelta(days=25), now + timedelta(days=40), now + timedelta(days=55),
         '线上', '全国大学英语竞赛组委会', 'individual', 1, 1, True, 'not_started'),
    ]
    for c in contests:
        cursor.execute("""INSERT INTO contest (name, category, level, registration_start, registration_end, contest_time,
            location, organizer, registration_type, min_team_size, max_team_size, need_review, status, create_by) 
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""", c + (admin_id,))
    print('Inserted 4 sample contests')

# Insert teacher_contest association (teacher 指导 IT 大赛)
cursor.execute("SELECT user_id FROM user WHERE student_no='teacher001'")
teacher_id = cursor.fetchone()[0]
cursor.execute("SELECT contest_id FROM contest WHERE name='校园程序设计大赛'")
row = cursor.fetchone()
if row:
    contest_id = row[0]
    cursor.execute("SELECT COUNT(*) FROM teacher_contest WHERE teacher_id=%s AND contest_id=%s", (teacher_id, contest_id))
    if cursor.fetchone()[0] == 0:
        cursor.execute("INSERT INTO teacher_contest (teacher_id, contest_id) VALUES (%s,%s)", (teacher_id, contest_id))
        print('Teacher associated with 校园程序设计大赛')

conn.commit()

# Show table counts
for t in ['user', 'contest', 'enrollment', 'team', 'team_member', 'teacher_contest', 'carousel']:
    cursor.execute(f"SELECT COUNT(*) FROM {t}")
    print(f'Table {t}: {cursor.fetchone()[0]} rows')

cursor.close()
conn.close()
print('\n=== Database initialization complete! ===')
print('Test accounts:')
print('  管理员: admin@contest.com / admin123')
print('  教师:   teacher@contest.com / teacher123')
print('  学生:   student@contest.com / 123456')
