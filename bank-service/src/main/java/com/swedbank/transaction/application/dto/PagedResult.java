package com.swedbank.transaction.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PagedResult<T> {

    private List<T> content;

    private Integer totalPages;

    private Integer totalElements;

    private Integer numberOfElements;

    private Integer number;

    private Integer size;

    private Boolean last;

    private Boolean first;
}
