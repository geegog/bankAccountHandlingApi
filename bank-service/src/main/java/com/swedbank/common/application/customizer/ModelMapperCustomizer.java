package com.swedbank.common.application.customizer;

import org.modelmapper.ModelMapper;

@FunctionalInterface
public interface ModelMapperCustomizer {
    void customize(ModelMapper modelMapper);
}
