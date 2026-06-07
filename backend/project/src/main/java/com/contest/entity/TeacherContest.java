
package com.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teacher_contest")
public class TeacherContest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private Long contestId;
    private LocalDateTime createTime;
}
