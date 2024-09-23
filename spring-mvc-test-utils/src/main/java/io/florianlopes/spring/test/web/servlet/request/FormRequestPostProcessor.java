package io.florianlopes.spring.test.web.servlet.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;

/**
 * Implementation of {@link RequestPostProcessor} that adds form parameters to the request before execution.
 *
 * @see MockMvcRequestBuilderUtils#postForm(String, Object) for implementation details
 */
public class FormRequestPostProcessor implements RequestPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormRequestPostProcessor.class);

    private final Object form;
    private final Configuration config;

    FormRequestPostProcessor(Object form, Configuration config) {
        this.form = form;
        this.config = config;
    }

    @Override
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        final Map<String, String> formFields = new FormFieldWrapper(form, config).collectFields();

        formFields.forEach((fieldName, fieldValue) -> {
            LOGGER.trace("Adding form field ({}={}) to HTTP request parameters", fieldName, fieldValue);
            request.addParameter(fieldName, fieldValue);
        });

        return request;
    }
}
