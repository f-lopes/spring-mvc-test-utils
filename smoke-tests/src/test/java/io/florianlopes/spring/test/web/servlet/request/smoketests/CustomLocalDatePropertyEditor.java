package io.florianlopes.spring.test.web.servlet.request.smoketests;

import io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Property editor for {@link LocalDate}
 * Register this property editor with MockMvcRequestBuilderUtils#registerPropertyEditor
 *
 * @author Florian Lopes
 * @see LocalDate
 * @see DateTimeFormatter
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see MockMvcRequestBuilderUtils#registerPropertyEditor(Class, PropertyEditor)
 */
class CustomLocalDatePropertyEditor extends PropertyEditorSupport {

    private final DateTimeFormatter dateTimeFormatter;
    private LocalDate localDate;

    CustomLocalDatePropertyEditor(String dateFormatPattern) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
    }

    @Override
    public void setValue(Object value) {
        this.localDate = (LocalDate) value;
    }

    @Override
    public Object getValue() {
        return this.localDate;
    }

    @Override
    public String getAsText() {
        return this.localDate != null ? this.localDate.format(this.dateTimeFormatter) : "";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isNotEmpty(text)) {
            this.localDate = LocalDate.parse(text, this.dateTimeFormatter);
        }
    }
}
