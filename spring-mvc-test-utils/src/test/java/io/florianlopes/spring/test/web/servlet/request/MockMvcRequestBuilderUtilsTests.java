package io.florianlopes.spring.test.web.servlet.request;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Florian Lopes
 */
public class MockMvcRequestBuilderUtilsTests {

    private static final String POST_FORM_URL = "/test";
    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";

    private ServletContext servletContext;

    @BeforeEach
    void setUp() {
        this.servletContext = new MockServletContext();
    }

    @Test
    void withParamsNullForm() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameterMap()).isEmpty();
    }

    @Test
    void withParamsNullAndEmptyFields() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(new AddUserForm("", "", null, null)));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("name")).isEmpty();
        assertThat(request.getParameter("firstName")).isEmpty();
        assertThat(request.getParameter("birthDate")).isNull();
        assertThat(request.getParameter("currentAddress.city")).isNull();
    }

    @Test
    void withParamsSimpleFields() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe")
                .currentAddress(new AddUserForm.Address(1, "Street", 5222, "New York"))
                .build();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("firstName")).isEqualTo("John");
        assertThat(request.getParameter("currentAddress.city")).isEqualTo("New York");
    }

    @Test
    void withParamsSimpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .build();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void withParamsComplexCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe").build();
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        addUserForm.setDiplomas(Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate)));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("diplomas[0].name")).isEqualTo("License");
        assertThat(request.getParameter("diplomas[0].date")).isEqualTo(bachelorDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        assertThat(request.getParameter("diplomas[1].name")).isEqualTo("MSC");
        assertThat(request.getParameter("diplomas[1].date")).isEqualTo(masterDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
    }

    @Test
    void withParamsSimpleArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("usernamesArray[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernamesArray[1]")).isEqualTo("jdoe");
    }

    @Test
    void withParamsComplexArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void withParamsEnumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe").gender(AddUserForm.Gender.MALE)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("gender")).isEqualTo("MALE");
    }

    @Test
    void withParamsSimpleMapField() {
        final Map<String, String> metadatas = new HashMap<>();
        metadatas.put("firstName", "John");
        metadatas.put("name", "Doe");
        metadatas.put("gender", null);
        final AddUserForm addUserForm = AddUserForm.builder()
                .metadatas(metadatas)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("metadatas[firstName]")).isEqualTo("John");
        assertThat(request.getParameter("metadatas[name]")).isEqualTo("Doe");
        assertThat(request.getParameter("metadatas[gender]")).isEmpty();
    }

    @Test
    void withParamsBigDecimal() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumber(BigDecimal.TEN)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("identificationNumber")).isEqualTo("10");
    }

    @Test
    void withParamsBigInteger() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("10");
    }

    @Test
    void withParamsRegisterPropertyEditor() {
        try {
            // Registering a property editor should override default conversion strategy
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new PropertyEditorSupport() {
                @Override
                public String getAsText() {
                    return "textValue";
                }
            });

            final AddUserForm build = AddUserForm.builder().identificationNumberBigInt(BigInteger.TEN).build();
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                    .with(MockMvcRequestBuilderUtils.form(build));
            MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
            mockHttpServletRequestBuilder.postProcessRequest(request);

            assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("textValue");
        } finally {
            // Restore original property editor
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        }
    }

    @Test
    void correctUrl() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getPathInfo()).isEqualTo(POST_FORM_URL);
    }

    @Test
    void nullForm() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, null);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameterMap()).isEmpty();
    }

    @Test
    void nullAndEmptyFields() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("", "", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("name")).isEmpty();
        assertThat(request.getParameter("firstName")).isEmpty();
        assertThat(request.getParameter("birthDate")).isNull();
        assertThat(request.getParameter("currentAddress.city")).isNull();
    }

    @Test
    void simpleFields() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe")
                .currentAddress(new AddUserForm.Address(1, "Street", 5222, "New York"))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("firstName")).isEqualTo("John");
        assertThat(request.getParameter("currentAddress.city")).isEqualTo("New York");
    }

    @Test
    void simpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexCollections() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe").build();
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        addUserForm.setDiplomas(Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate)));
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertThat(request.getParameter("diplomas[0].name")).isEqualTo("License");
        assertThat(request.getParameter("diplomas[0].date")).isEqualTo(bachelorDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        assertThat(request.getParameter("diplomas[1].name")).isEqualTo("MSC");
        assertThat(request.getParameter("diplomas[1].date")).isEqualTo(masterDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
    }

    @Test
    void simpleArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("usernamesArray[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernamesArray[1]")).isEqualTo("jdoe");
    }

    @Test
    void complexArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getParameter("usernames[0]")).isEqualTo("john.doe");
        assertThat(request.getParameter("usernames[1]")).isEqualTo("jdoe");
    }

    @Test
    void enumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe").gender(AddUserForm.Gender.MALE)
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
        assertThat(request.getParameter("metadatas[gender]")).isEmpty();
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
    void registerPropertyEditor() {
        try {
            // Registering a property editor should override default conversion strategy
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new PropertyEditorSupport() {
                @Override
                public String getAsText() {
                    return "textValue";
                }
            });

            final AddUserForm build = AddUserForm.builder().identificationNumberBigInt(BigInteger.TEN).build();
            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, build);

            MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
            assertThat(request.getParameter("identificationNumberBigInt")).isEqualTo("textValue");
        } finally {
            // Restore original property editor
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        }
    }

    @Test
    void postMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void putMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.putForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertThat(request.getMethod()).isEqualTo("PUT");
    }

    @Nested
    class ConfigurationTest {

        @Test
        void testDefaultConfig() {
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
        void testExcludeFinalField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, Configuration.EXCLUDE_FINAL);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(2);
            assertThat(request.getParameter("name")).isEqualTo("name");
            assertThat(request.getParameter("transientName")).isEqualTo("transientName");
        }

        @Test
        void testIncludeTransientField() {
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
        void testIncludeStaticField() {
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
        void testCustomPredicate() {
            final Configuration config = Configuration.builder().fieldPredicate(field -> "transientName".equals(field.getName())).build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).isEmpty();
        }

        @Test
        void testCustomPredicateWithModifier() {
            final Configuration config = Configuration.builder().includeTransient(true).fieldPredicate(field -> "transientName".equals(field.getName())).build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertThat(request.getParameterMap()).hasSize(1);
            assertThat(request.getParameter("transientName")).isEqualTo("transientName");
        }

    }

}
