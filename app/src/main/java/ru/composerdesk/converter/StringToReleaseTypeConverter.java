package ru.composerdesk.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.composerdesk.model.ReleaseType;
@Component
public class StringToReleaseTypeConverter implements Converter<String, ReleaseType> {
    @Override
    public ReleaseType convert(String source) {
        return ReleaseType.valueOf(source.toUpperCase());
    }
}