package io.florianlopes.spring.test.web.servlet.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

/**
 * Custom MockMvcRequestBuilder to post an entire form to a given url.
 * Useful to test Spring MVC form validation.
 *
 * @author Florian Lopes
 * @see MockMvcRequestBuilders
 */
public class MockMvcRequestBuilderUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockMvcRequestBuilderUtils.class);

    private static final Configuration DEFAULT_CONFIG = Configuration.DEFAULT;

    private MockMvcRequestBuilderUtils() {
    }

    /**
     * Post a form to the given url.
     * All non-null, non-static, non-final form fields will be added as HTTP request parameters using POST method
     *
     * @param url    the URL to post the form to
     * @param form   form object to send using POST method
     * @param config configuration object
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder postForm(String url, Object form, Configuration config) {
        return buildMockHttpServletRequestBuilder(url, form, config, HttpMethod.POST);
    }

    /**
     * Post a form to the given url.
     * All non-null, non-static, non-final, non-transient form fields will be added as HTTP request parameters using POST method
     *
     * @param url  the URL to post the form to
     * @param form form object to send using POST method
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder postForm(String url, Object form) {
        return buildMockHttpServletRequestBuilder(url, form, DEFAULT_CONFIG, HttpMethod.POST);
    }

    /**
     * Put a form to the given url.
     * All non-null, non-static, non-final form fields will be added as HTTP request parameters using PUT method
     *
     * @param url    the URL to put the form to
     * @param form   form object to send using PUT method
     * @param config configuration object
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder putForm(String url, Object form, Configuration config) {
        return buildMockHttpServletRequestBuilder(url, form, config, HttpMethod.PUT);
    }

    /**
     * Put a form to the given url.
     * All non-null, non-static, non-final, non-transient form fields will be added as HTTP request parameters using PUT method
     *
     * @param url  the URL to put the form to
     * @param form form object to send using PUT method
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder putForm(String url, Object form) {
        return buildMockHttpServletRequestBuilder(url, form, DEFAULT_CONFIG, HttpMethod.PUT);
    }

    /**
     * Creates a FormRequestPostProcessor that can be used to add form parameters to an HTTP request.
     *
     * @param form   the form object from which to extract HTTP request parameters
     * @param config the configuration object that customizes how the fields are processed
     * @return a FormRequestPostProcessor that adds the form parameters to the HTTP request
     */
    public static FormRequestPostProcessor form(Object form, Configuration config) {
        return new FormRequestPostProcessor(form, config);
    }

    /**
     * Creates a FormRequestPostProcessor that can be used to add form parameters to an HTTP request.
     * Uses the default configuration
     *
     * @param form the form object from which to extract HTTP request parameters
     * @return a FormRequestPostProcessor that adds the form parameters to the HTTP request
     * @see Configuration#DEFAULT
     */
    public static FormRequestPostProcessor form(Object form) {
        return form(form, DEFAULT_CONFIG);
    }

    private static MockHttpServletRequestBuilder buildMockHttpServletRequestBuilder(
            String url,
            Object form,
            Configuration config,
            HttpMethod method
    ) {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        addFormFieldsToRequestBuilder(form, builder, config);
        return builder;
    }

    private static void addFormFieldsToRequestBuilder(
            Object form,
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder,
            Configuration config
    ) {
        final Map<String, String> formFields = new FormFieldWrapper(form, config).collectFields();

        formFields.forEach((fieldName, fieldValue) -> {
            LOGGER.trace("Adding form field ({}={}) to HTTP request parameters", fieldName, fieldValue);
            mockHttpServletRequestBuilder.param(fieldName, fieldValue);
        });
    }
}
