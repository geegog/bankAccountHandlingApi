package com.swedbank.common.application.config;

import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.common.application.customizer.ModelMapperCustomizer;
import com.swedbank.common.domian.Money;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(List<ModelMapperCustomizer> customizers) {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Converter<Money, MoneyDto> moneyToDto = ctx -> {
            Money src = ctx.getSource();
            if (src == null) return null;
            return new MoneyDto(src.getAmount(), src.getCurrency());
        };

        Converter<MoneyDto, Money> dtoToMoney = ctx -> {
            MoneyDto src = ctx.getSource();
            if (src == null) return null;
            return Money.of(src.getAmount(), src.getCurrency());
        };

        modelMapper.addConverter(moneyToDto);
        modelMapper.addConverter(dtoToMoney);

        customizers.forEach(customizer -> customizer.customize(modelMapper));

        return modelMapper;
    }
}
