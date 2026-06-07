package com.contest.dto.contest;

import lombok.Data;

@Data
public class AdminContestQueryRequest {
    private String keyword;
    private String category;
    private String status;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}
