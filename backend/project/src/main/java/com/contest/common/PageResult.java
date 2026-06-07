package com.contest.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<T> records;
}
