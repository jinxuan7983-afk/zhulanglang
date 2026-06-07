
package com.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("contest")
public class Contest {
    @TableId(type = IdType.AUTO)
    private Long contestId;
    private String name;
    private String category;
    private String level;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime contestTime;
    private String location;
    private String organizer;
    private String coverImage;
    private String attachmentUrl;
    private String registrationType;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Boolean needReview;
    private String status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
