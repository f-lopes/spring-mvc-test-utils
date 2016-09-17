package io.florianlopes.spring.test.web.servlet.request;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Property editor for {@link java.time.LocalDate}
 * Register this property editor with MockMvcRequestBuilderUtils#registerPropertyEditor
 *
 * @author Florian Lopes
 * @see java.time.LocalDate
 * @see java.time.format.DateTimeFormatter
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils#registerPropertyEditor(Class, PropertyEditor)
 */
class CustomLocalDatePropertyEditor extends PropertyEditorSupport {

    private LocalDate localDate;
    private final DateTimeFormatter dateTimeFormatter;

    public CustomLocalDatePropertyEditor(String dateFormatPattern) {
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
