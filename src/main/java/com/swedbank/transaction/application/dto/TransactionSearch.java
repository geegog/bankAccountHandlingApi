package com.swedbank.transaction.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionSearch {

    @Min(1)
    @Max(100)
    private Integer size = 50;

    private Integer page = 0;

}
