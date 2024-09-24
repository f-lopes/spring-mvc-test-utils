package io.florianlopes.spring.test.web.servlet.request;

import io.florianlopes.spring.test.web.servlet.request.assertion.RequestParametersAssert;
import jakarta.servlet.ServletContext;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class FormRequestPostProcessorTests {

    private static final LogCaptor LOG_CAPTOR = LogCaptor.forClass(FormRequestPostProcessor.class);

    private static final String POST_FORM_URL = "/test";

    private ServletContext servletContext;

    @BeforeEach
    void setUp() {
        this.servletContext = new MockServletContext();
        LOG_CAPTOR.clearLogs();
    }

    @Test
    void nullForm() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameterMap()).isEmpty();
    }

    @Test
    void nullAndEmptyFields() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(
                        AddUserForm.builder()
                                .firstName("")
                                .birthDate(null)
                                .currentAddress(null)
                                .build()
                ));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("firstName")).isEmpty();
        assertThat(request.getParameter("birthDate")).isNull();
        assertThat(request.getParameter("currentAddress.city")).isNull();
    }

    @Test
    void simpleFields() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John")
                .currentAddress(TestFixtures.anAddress())
                .build();

        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("firstName")).isEqualTo("John");
        assertThat(request.getParameter("currentAddress.city"))
                .isEqualTo(addUserForm.getCurrentAddress().getCity());
    }

    @Test
    void simpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .usernames(List.of("john.doe", "jdoe"))
                .build();

        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexCollection() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .diplomas(List.of(
                        new AddUserForm.Diploma("License", LocalDate.now().minusYears(2)),
                        new AddUserForm.Diploma("MSC", LocalDate.now())
                ))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        RequestParametersAssert.assertThat(request)
                .containsParameters(Map.of(
                        "diplomas[0].name", addUserForm.getDiplomas().get(0).getName(),
                        "diplomas[0].date", addUserForm.getDiplomas().get(0).getDate().format(DateTimeFormatter.ISO_DATE),
                        "diplomas[1].name", addUserForm.getDiplomas().get(1).getName(),
                        "diplomas[1].date", addUserForm.getDiplomas().get(1).getDate().format(DateTimeFormatter.ISO_DATE)
                ));
    }

    @Test
    void simpleArray() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("usernamesArray[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernamesArray[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexArray() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street 10", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street 20", 5222, "Washington")
                })
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("formerAddresses[0].streetNumber")).isEqualTo("10");
        assertThat(request.getParameter("formerAddresses[1].streetNumber")).isEqualTo("20");
        assertThat(request.getParameter("formerAddresses[0].streetName")).isEqualTo("Street 10");
        assertThat(request.getParameter("formerAddresses[1].streetName")).isEqualTo("Street 20");
        assertThat(request.getParameter("formerAddresses[0].city")).isEqualTo("Chicago");
        assertThat(request.getParameter("formerAddresses[1].city")).isEqualTo("Washington");
        assertThat(request.getParameter("formerAddresses[0].postalCode")).isEqualTo("5222");
        assertThat(request.getParameter("formerAddresses[1].postalCode")).isEqualTo("5222");
    }

    @Test
    void enumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .gender(AddUserForm.Gender.MALE)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("gender")).isEqualTo("MALE");
    }

    @Test
    void simpleMapField() {
        final Map<String, String> metadatas = new HashMap<>();
        metadatas.put("firstName", "John");
        metadatas.put("name", "Doe");
        metadatas.put("gender", null);
        final AddUserForm addUserForm = AddUserForm.builder()
                .metadatas(metadatas)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("metadatas[firstName]")).isEqualTo("John");
        assertThat(request.getParameter("metadatas[name]")).isEqualTo("Doe");
        assertThat(request.getParameter("metadatas[gender]")).isEmpty();
    }

    @Test
    void complexMapField() {
        final LocalDate mscDate = LocalDate.now();
        final LocalDate licenseDate = LocalDate.now().minusYears(2);
        final AddUserForm addUserForm = AddUserForm.builder()
                .diplomasMap(Map.of(
                        "License", new AddUserForm.Diploma("License", licenseDate),
                        "MSC", new AddUserForm.Diploma("MSC", mscDate)
                ))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        RequestParametersAssert.assertThat(request)
                .containsParameters(Map.of(
                        "diplomasMap[License].name", addUserForm.getDiplomasMap().get("License").getName(),
                        "diplomasMap[License].date", addUserForm.getDiplomasMap().get("License").getDate().format(DateTimeFormatter.ISO_DATE),
                        "diplomasMap[MSC].name", addUserForm.getDiplomasMap().get("MSC").getName(),
                        "diplomasMap[MSC].date", addUserForm.getDiplomasMap().get("MSC").getDate().format(DateTimeFormatter.ISO_DATE)
                ));
    }

    @Test
    void bigDecimal() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumber(BigDecimal.TEN)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("identificationNumber")).isEqualTo("10");
    }

    @Test
    void bigInteger() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("10");
    }

    @Test
    void registerPropertyEditor() {
        // Registering a property editor should override default conversion strategy
        final PropertyEditorSupport bigIntegerPropertyEditor = new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                return "textValue";
            }
        };

        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        final Configuration config = Configuration.builder()
                .withPropertyEditor(bigIntegerPropertyEditor, BigInteger.class)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm, config));

        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("textValue");
    }

    @Test
    void logsEveryFieldAddedToHttpRequest() {
        final int numberOfFormFields = 36;
        final AddUserForm addUserForm = TestFixtures.aCompleteAddUserForm();

        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));

        mockHttpServletRequestBuilder.postProcessRequest(mockHttpServletRequestBuilder.buildRequest(servletContext));

        assertThat(LOG_CAPTOR.getTraceLogs())
                .allMatch(logLine -> logLine.matches("Adding form field \\(\\w.*=\\w.*\\) to HTTP request parameters"))
                .hasSize(numberOfFormFields);
    }
}
