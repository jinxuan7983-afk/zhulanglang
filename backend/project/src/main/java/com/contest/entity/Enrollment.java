
package com.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class Enrollment {
    @TableId(type = IdType.AUTO)
    private Long enrollmentId;
    private Long userId;
    private Long contestId;
    private Long teamId;
    private String status;
    private String reviewComment;
    private LocalDateTime registrationTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
    private String attachmentUrl;
}
