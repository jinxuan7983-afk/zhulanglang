
package com.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String studentNo;
    private String email;
    private String password;
    private String name;
    private String college;
    private String phone;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime lockUntil;
    private Integer loginFailCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
