package com.contest.dto.contest;

import lombok.Data;

@Data
public class ContestQueryRequest {
    private String keyword;
    private String category;
    private String progressStatus;
    private Long pageNum = 1L;
    private Long pageSize = 12L;
}
