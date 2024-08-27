package io.florianlopes.spring.test.web.servlet.request;

import io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils.Configuration;
import jakarta.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Florian Lopes
 */
public class MockMvcRequestBuilderUtilsTests {

    private static final String POST_FORM_URL = "/test";
    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";

    private static final Configuration EXCLUDE_FINAL = Configuration.builder().includeFinal(false).includeTransient(true).build();
    private static final Configuration INCLUDE_TRANSIENT = Configuration.builder().includeFinal(false).includeTransient(true).build();
    private static final Configuration INCLUDE_STATIC = Configuration.builder().includeFinal(false).includeStatic(true).build();

    private ServletContext servletContext;

    @BeforeEach
    public void setUp() {
        this.servletContext = new MockServletContext();
    }

    @Test
    public void withParamsNullForm() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals(0, request.getParameterMap().size());
    }

    @Test
    public void withParamsNullAndEmptyFields() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(new AddUserForm("", "", null, null)));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals(StringUtils.EMPTY, request.getParameter("name"));
        assertEquals(StringUtils.EMPTY, request.getParameter("firstName"));
        assertNull(request.getParameter("birthDate"));
        assertNull(request.getParameter("currentAddress.city"));
    }

    @Test
    public void withParamsSimpleFields() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe")
                .currentAddress(new AddUserForm.Address(1, "Street", 5222, "New York"))
                .build();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("John", request.getParameter("firstName"));
        assertEquals("New York", request.getParameter("currentAddress.city"));
    }

    @Test
    public void withParamsSimpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .build();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("john.doe", request.getParameter("usernames[0]"));
        assertEquals("jdoe", request.getParameter("usernames[1]"));
    }

    @Test
    public void withParamsComplexCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe").build();
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        addUserForm.setDiplomas(Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate)));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("License", request.getParameter("diplomas[0].name"));
        assertEquals(bachelorDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("diplomas[0].date"));
        assertEquals("MSC", request.getParameter("diplomas[1].name"));
        assertEquals(masterDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("diplomas[1].date"));
    }

    @Test
    public void withParamsSimpleArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("john.doe", request.getParameter("usernamesArray[0]"));
        assertEquals("jdoe", request.getParameter("usernamesArray[1]"));
    }

    @Test
    public void withParamsComplexArray() {
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

        assertEquals("john.doe", request.getParameter("usernames[0]"));
        assertEquals("jdoe", request.getParameter("usernames[1]"));
    }

    @Test
    public void withParamsEnumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe").gender(AddUserForm.Gender.MALE)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("MALE", request.getParameter("gender"));
    }

    @Test
    public void withParamsSimpleMapField() {
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

        assertEquals("John", request.getParameter("metadatas[firstName]"));
        assertEquals("Doe", request.getParameter("metadatas[name]"));
        assertEquals("", request.getParameter("metadatas[gender]"));
    }

    @Test
    public void withParamsBigDecimal() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumber(BigDecimal.TEN)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("10", request.getParameter("identificationNumber"));
    }

    @Test
    public void withParamsBigInteger() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(POST_FORM_URL)
                .with(MockMvcRequestBuilderUtils.form(addUserForm));
        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        mockHttpServletRequestBuilder.postProcessRequest(request);

        assertEquals("10", request.getParameter("identificationNumberBigInt"));
    }

    @Test
    public void withParamsRegisterPropertyEditor() {
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

            assertEquals("textValue", request.getParameter("identificationNumberBigInt"));
        } finally {
            // Restore original property editor
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        }
    }

    @Test
    public void correctUrl() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals(POST_FORM_URL, request.getPathInfo());
    }

    @Test
    public void nullForm() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, null);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals(0, request.getParameterMap().size());
    }

    @Test
    public void nullAndEmptyFields() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("", "", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals(StringUtils.EMPTY, request.getParameter("name"));
        assertEquals(StringUtils.EMPTY, request.getParameter("firstName"));
        assertNull(request.getParameter("birthDate"));
        assertNull(request.getParameter("currentAddress.city"));
    }

    @Test
    public void simpleFields() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe")
                .currentAddress(new AddUserForm.Address(1, "Street", 5222, "New York"))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("John", request.getParameter("firstName"));
        assertEquals("New York", request.getParameter("currentAddress.city"));
    }

    @Test
    public void simpleCollection() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("john.doe", request.getParameter("usernames[0]"));
        assertEquals("jdoe", request.getParameter("usernames[1]"));
    }

    @Test
    public void complexCollections() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe").build();
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        addUserForm.setDiplomas(Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate)));
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertEquals("License", request.getParameter("diplomas[0].name"));
        assertEquals(bachelorDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("diplomas[0].date"));
        assertEquals("MSC", request.getParameter("diplomas[1].name"));
        assertEquals(masterDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("diplomas[1].date"));
    }

    @Test
    public void simpleArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("john.doe", request.getParameter("usernamesArray[0]"));
        assertEquals("jdoe", request.getParameter("usernamesArray[1]"));
    }

    @Test
    public void complexArray() {
        final AddUserForm addUserForm = AddUserForm.builder().firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("john.doe", request.getParameter("usernames[0]"));
        assertEquals("jdoe", request.getParameter("usernames[1]"));
    }

    @Test
    public void enumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe").gender(AddUserForm.Gender.MALE)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("MALE", request.getParameter("gender"));
    }

    @Test
    public void simpleMapField() {
        final Map<String, String> metadatas = new HashMap<>();
        metadatas.put("firstName", "John");
        metadatas.put("name", "Doe");
        metadatas.put("gender", null);
        final AddUserForm addUserForm = AddUserForm.builder()
                .metadatas(metadatas)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("John", request.getParameter("metadatas[firstName]"));
        assertEquals("Doe", request.getParameter("metadatas[name]"));
        assertEquals("", request.getParameter("metadatas[gender]"));
    }

    @Test
    public void bigDecimal() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumber(BigDecimal.TEN)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("10", request.getParameter("identificationNumber"));
    }

    @Test
    public void bigInteger() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .identificationNumberBigInt(BigInteger.TEN)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("10", request.getParameter("identificationNumberBigInt"));
    }

    @Test
    public void registerPropertyEditor() {
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
            assertEquals("textValue", request.getParameter("identificationNumberBigInt"));
        } finally {
            // Restore original property editor
            MockMvcRequestBuilderUtils.registerPropertyEditor(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        }
    }

    @Test
    public void postMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("POST", request.getMethod());
    }

    @Test
    public void putMethod() {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.putForm(POST_FORM_URL,
                new AddUserForm("John", "Doe", null, null));
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("PUT", request.getMethod());
    }

    @Nested
    class ConfigurationTest {

        @Test
        public void testDefaultConfig() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");
            form.setInner(new ConfigurationForm.Inner("inner"));

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertEquals(3, request.getParameterMap().size());
            assertEquals("name", request.getParameter("name"));
            assertEquals("inner", request.getParameter("inner.value"));
            assertEquals("finalValue", request.getParameter("finalName"));
        }

        @Test
        public void testExcludeFinalField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, EXCLUDE_FINAL);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertEquals(2, request.getParameterMap().size());
            assertEquals("name", request.getParameter("name"));
            assertEquals("transientName", request.getParameter("transientName"));
        }

        @Test
        public void testIncludeTransientField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, INCLUDE_TRANSIENT);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertEquals(2, request.getParameterMap().size());
            assertEquals("name", request.getParameter("name"));
            assertEquals("transientName", request.getParameter("transientName"));
        }

        @Test
        public void testIncludeStaticField() {
            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, INCLUDE_STATIC);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertEquals(2, request.getParameterMap().size());
            assertEquals("name", request.getParameter("name"));
            assertEquals("static name", request.getParameter("STATIC_NAME"));
        }

        @Test
        public void testCustomPredicate() {
            final Configuration config = Configuration.builder().fieldPredicate(field -> "transientName".equals(field.getName())).build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertTrue(request.getParameterMap().isEmpty());
        }

        @Test
        public void testCustomPredicateWithModifier() {
            final Configuration config = Configuration.builder().includeTransient(true).fieldPredicate(field -> "transientName".equals(field.getName())).build();

            final ConfigurationForm form = new ConfigurationForm();
            form.setName("name");
            form.setTransientName("transientName");

            final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, form, config);
            final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(servletContext);

            assertEquals(1, request.getParameterMap().size());
            assertEquals("transientName", request.getParameter("transientName"));
        }

    }

}
