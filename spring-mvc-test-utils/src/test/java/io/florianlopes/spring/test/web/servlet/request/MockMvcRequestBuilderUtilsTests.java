package io.florianlopes.spring.test.web.servlet.request;

import io.florianlopes.spring.test.web.servlet.request.assertion.RequestParametersAssert;
import jakarta.servlet.ServletContext;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Florian Lopes
 */
public class MockMvcRequestBuilderUtilsTests {

    private static final LogCaptor LOG_CAPTOR = LogCaptor.forClass(MockMvcRequestBuilderUtils.class);

    private static final String POST_FORM_URL = "/test";

    private ServletContext servletContext;

    @BeforeEach
    void setUp() {
        this.servletContext = new MockServletContext();
    }

    @Test
    void correctUrl() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, new AddUserForm());
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getPathInfo()).isEqualTo(POST_FORM_URL);
    }

    @Test
    void nullUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> MockMvcRequestBuilderUtils.postForm(null, new AddUserForm()));
    }

    @Test
    void postHttpMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, new AddUserForm());
        final MockHttpServletRequest httpServletRequest = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(httpServletRequest.getMethod()).isEqualTo(HttpMethod.POST.name());
    }

    @Test
    void postHttpMethodWithConfig() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, new AddUserForm(), Configuration.INCLUDE_STATIC);
        final MockHttpServletRequest httpServletRequest = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(httpServletRequest.getMethod()).isEqualTo(HttpMethod.POST.name());
    }

    @Test
    void putHttpMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.putForm(POST_FORM_URL, new AddUserForm());
        final MockHttpServletRequest httpServletRequest = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(httpServletRequest.getMethod()).isEqualTo(HttpMethod.PUT.name());
    }


    @Test
    void putHttpMethodWithConfig() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.putForm(POST_FORM_URL, new AddUserForm(), Configuration.INCLUDE_STATIC);
        final MockHttpServletRequest httpServletRequest = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(httpServletRequest.getMethod()).isEqualTo(HttpMethod.PUT.name());
    }

    @Test
    void nullForm() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, null);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameterMap()).isEmpty();
    }

    @Test
    void nullFieldsAreNotAddedToRequestParameters() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName(null)
                .birthDate(null)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("firstName")).isNull();
        assertThat(request.getParameter("birthDate")).isNull();
    }

    @Test
    void emptyFieldsAreAddedAsEmptyRequestParameters() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("")
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("firstName")).isEmpty();
    }

    @Test
    void simpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .usernames(List.of("john.doe", "jdoe"))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexCollections() {
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        final AddUserForm addUserForm = AddUserForm.builder()
                .diplomas(List.of(
                        new AddUserForm.Diploma("License", bachelorDate),
                        new AddUserForm.Diploma("MSC", masterDate)
                ))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
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
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("usernamesArray[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernamesArray[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexArray() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        RequestParametersAssert.assertThat(request)
                .containsParameters(Map.of(
                        "formerAddresses[0].city", addUserForm.getFormerAddresses()[0].getCity(),
                        "formerAddresses[1].city", addUserForm.getFormerAddresses()[1].getCity(),
                        "formerAddresses[0].streetName", addUserForm.getFormerAddresses()[0].getStreetName(),
                        "formerAddresses[1].streetName", addUserForm.getFormerAddresses()[1].getStreetName(),
                        "formerAddresses[0].streetNumber", String.valueOf(addUserForm.getFormerAddresses()[0].getStreetNumber()),
                        "formerAddresses[1].streetNumber", String.valueOf(addUserForm.getFormerAddresses()[1].getStreetNumber()),
                        "formerAddresses[0].postalCode", String.valueOf(addUserForm.getFormerAddresses()[0].getPostalCode()),
                        "formerAddresses[1].postalCode", String.valueOf(addUserForm.getFormerAddresses()[1].getPostalCode())
                ));
    }

    @Test
    void enumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .gender(AddUserForm.Gender.MALE)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

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
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("metadatas[firstName]")).isEqualTo("John");
        assertThat(request.getParameter("metadatas[name]")).isEqualTo("Doe");
    }

    @Test
    void simpleMapFieldsWithNullValueAreAddedAsEmptyRequestParameters() {
        final Map<String, String> metadatas = new HashMap<>();
        metadatas.put("gender", null);
        final AddUserForm addUserForm = AddUserForm.builder()
                .metadatas(metadatas)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

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
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

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
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("identificationNumber")).isEqualTo("10");
    }

    @Test
    void bigInteger() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("10");
    }

    @Test
    void logsEveryFieldAddedToHttpRequest() {
        final int numberOfFormFields = 32;
        final AddUserForm addUserForm = TestFixtures.aCompleteAddUserForm();

        MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        assertThat(LOG_CAPTOR.getTraceLogs())
                .allMatch(logLine -> logLine.matches("Adding form field \\(\\w.*=\\w.*\\) to HTTP request parameters"))
                .hasSize(numberOfFormFields);
    }

    @Nested
    class ConfigurationTests {

        @Test
        void defaultConfig() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");
            form.setInner(new ConfigurationForm.Inner("inner"));

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(3);
            assertThat(request.getParameter("name")).isEqualTo("name");
            assertThat(request.getParameter("inner.value")).isEqualTo("inner");
            assertThat(request.getParameter("finalName")).isEqualTo("finalValue");
        }

        @Test
        void excludeFinalField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, Configuration.EXCLUDE_FINAL);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(1);
            assertThat(request.getParameter("name")).isEqualTo("name");
            assertThat(request.getParameter("transientName")).isNull();
        }

        @Test
        void includeTransientField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, Configuration.INCLUDE_TRANSIENT);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(2);
            assertThat(request.getParameter("name")).isEqualTo("name");
            assertThat(request.getParameter("transientName")).isEqualTo("transientName");
        }

        @Test
        void includeStaticField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, Configuration.INCLUDE_STATIC);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(2);
            assertThat(request.getParameter("name")).isEqualTo("name");
            assertThat(request.getParameter("STATIC_NAME")).isEqualTo("static name");
        }

        @Test
        void customPredicate() {
            final Configuration config = Configuration.builder()
                    .fieldPredicate(field -> "simpleField".equals(field.getName()))
                    .build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setSimpleField("simpleValue");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap())
                    .containsExactly(Map.entry("simpleField", new String[]{form.getSimpleField()}));
        }

        @Test
        void customPredicateWithModifier() {
            final Configuration config = Configuration.builder()
                    .includeTransient(true)
                    .fieldPredicate(field -> "transientName".equals(field.getName()))
                    .build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(1);
            assertThat(request.getParameter("transientName")).isEqualTo("transientName");
        }

        @Test
        void customPropertyEditor() {
            // Registering a property editor should override default conversion strategy
            final PropertyEditorSupport propertyEditor = new PropertyEditorSupport() {
                @Override
                public String getAsText() {
                    return "textValue";
                }
            };

            final AddUserForm addUserForm = AddUserForm
                    .builder()
                    .identificationNumberBigInt(BigInteger.TEN)
                    .build();
            final Configuration config = Configuration
                    .builder()
                    .withPropertyEditor(propertyEditor, BigInteger.class)
                    .build();
            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                    MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm, config);

            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
            assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("textValue");
        }

        @Test
        void registerMultiplePropertyEditors() {
            final PropertyEditorSupport propertyEditor = new PropertyEditorSupport() {
                @Override
                public String getAsText() {
                    return "textValue";
                }
            };
            final PropertyEditorSupport customDatePropertyEditor = new PropertyEditorSupport() {
                @Override
                public String getAsText() {
                    return "textDateValue";
                }
            };

            final AddUserForm addUserForm = AddUserForm
                    .builder()
                    .identificationNumberBigInt(BigInteger.TEN)
                    .birthDate(LocalDate.now().minusYears(2))
                    .build();
            final Configuration config = Configuration
                    .builder()
                    .withPropertyEditor(propertyEditor, BigInteger.class)
                    .withPropertyEditor(customDatePropertyEditor, LocalDate.class)
                    .build();
            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                    MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm, config);

            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);
            assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("textValue");
            assertThat(request.getParameter("birthDate")).isEqualTo("textDateValue");
        }

        @Nested
        class DefaultConfiguration {

            @Test
            void includesFinalFields() {
                final ConfigurationForm form = new ConfigurationForm();

                final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form);
                final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

                assertThat(request.getParameter("finalName")).isEqualTo("finalValue");
            }

            @Test
            void excludesTransientFields() {
                final ConfigurationForm form = new ConfigurationForm();
                form.setTransientName("transientName");

                final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form);
                final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

                assertThat(request.getParameter("transientName")).isNullOrEmpty();
            }

            @Test
            void excludesStaticFields() {
                final ConfigurationForm form = new ConfigurationForm();

                final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form);
                final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

                assertThat(request.getParameter("STATIC_NAME")).isNullOrEmpty();
            }
        }
    }

}
