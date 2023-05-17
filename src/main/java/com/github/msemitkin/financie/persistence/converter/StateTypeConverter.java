package com.github.msemitkin.financie.persistence.converter;

import com.github.msemitkin.financie.state.StateType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class StateTypeConverter implements AttributeConverter<StateType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(StateType attribute) {
        return attribute.getId();
    }

    @Override
    public StateType convertToEntityAttribute(Integer dbData) {
        return Arrays.stream(StateType.values())
            .filter(type -> type.getId() == dbData)
            .findFirst()
            .orElseThrow();
    }
}
