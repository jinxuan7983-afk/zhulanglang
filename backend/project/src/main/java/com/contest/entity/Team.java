
package com.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long teamId;
    private Long contestId;
    private Long captainId;
    private String teamName;
    private String inviteCode;
    private LocalDateTime createTime;
    private String status;
    private String registrationMaterial;
    private String reviewComment;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
}
