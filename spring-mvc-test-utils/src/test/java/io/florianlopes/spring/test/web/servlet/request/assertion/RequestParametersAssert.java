package io.florianlopes.spring.test.web.servlet.request.assertion;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;

public class RequestParametersAssert extends AbstractAssert<RequestParametersAssert, HttpServletRequest> {

    protected RequestParametersAssert(HttpServletRequest httpServletRequest) {
        super(httpServletRequest, RequestParametersAssert.class);
    }

    public static RequestParametersAssert assertThat(HttpServletRequest httpServletRequest) {
        return new RequestParametersAssert(httpServletRequest);
    }

    public RequestParametersAssert containsParameters(Map<String, String> parameters) {
        isNotNull();

        parameters.forEach((key, value) -> {
            if (this.actual.getParameter(key) == null || !this.actual.getParameter(key).equals(value)) {
                failWithMessage("http servlet request does not contain required parameters: %s", parameters);
            }
        });

        return this;
    }
}
