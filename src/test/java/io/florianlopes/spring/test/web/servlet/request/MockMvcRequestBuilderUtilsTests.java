package io.florianlopes.spring.test.web.servlet.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * @author Florian Lopes
 */
public class MockMvcRequestBuilderUtilsTests {

    private static final String POST_FORM_URL = "/test";
    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";

    private ServletContext servletContext;

    @Before
    public void setUp() {
        this.servletContext = new MockServletContext();
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
        assertEquals(StringUtils.EMPTY, request.getParameter("birthDate"));
        assertEquals(null, request.getParameter("currentAddress.city"));
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
        final AddUserForm addUserForm = AddUserForm.builder()
                .metadatas(metadatas)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("John", request.getParameter("metadatas[firstName]"));
        assertEquals("Doe", request.getParameter("metadatas[name]"));
    }

    @Test
    public void unregisterPropertyEditor() {
        // Register a property editor and ensure it is used
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate userBirthDate = LocalDate.of(2016, 8, 29);
        final AddUserForm addUserForm = new AddUserForm("John", "Doe", userBirthDate, null);
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertEquals(userBirthDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("birthDate"));

        // Unregister the property editor and ensure it is not used anymore
        MockMvcRequestBuilderUtils.unregisterPropertyEditor(LocalDate.class);
        final MockHttpServletRequest secondRequest = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm).buildRequest(this.servletContext);

        assertEquals(String.valueOf(userBirthDate), secondRequest.getParameter("birthDate"));
    }

    @Test
    public void unregisterPropertyEditors() {
        // Register a property editor and ensure it is used
        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate userBirthDate = LocalDate.of(2016, 8, 29);
        final AddUserForm addUserForm = new AddUserForm("John", "Doe", userBirthDate, null);
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertEquals(userBirthDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("birthDate"));

        // Unregister all property editors and ensure the previously registered property editor is not used anymore
        MockMvcRequestBuilderUtils.unregisterPropertyEditors();
        final MockHttpServletRequest secondRequest = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm).buildRequest(this.servletContext);

        assertEquals(String.valueOf(userBirthDate), secondRequest.getParameter("birthDate"));
    }
}
