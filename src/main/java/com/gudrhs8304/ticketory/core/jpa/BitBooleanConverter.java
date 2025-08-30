package com.gudrhs8304.ticketory.core.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) // 필요한 필드에만 @Convert로 적용
public class BitBooleanConverter implements AttributeConverter<Boolean, Byte> {

    @Override
    public Byte convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) return null;  // 컬럼이 NULL 허용이면 그대로
        return (attribute ? (byte) 1 : (byte) 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Byte dbData) {
        if (dbData == null) return null;     // 컬럼이 NULL 허용이면 그대로
        return dbData != 0;
    }
}
