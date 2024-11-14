package com.twillice.itmoislab1.converters;

import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.service.ChapterService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@ApplicationScoped
@FacesConverter(value = "chapterConverter", managed = true)
public class ChapterConverter implements Converter<Chapter> {
    @Inject
    private ChapterService chapterService;

    @Override
    public Chapter getAsObject(FacesContext facesContext, UIComponent uiComponent, String chapterId) {
        if (chapterId == null || chapterId.isBlank())
            return null;
        try {
            return chapterService.find(Long.parseLong(chapterId));
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not a valid chapter.", ""));
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Chapter chapter) {
        return chapter == null ? null : String.valueOf(chapter.getId());
    }
}
