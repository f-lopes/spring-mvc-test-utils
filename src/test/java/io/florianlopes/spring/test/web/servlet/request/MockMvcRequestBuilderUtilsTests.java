package io.florianlopes.spring.test.web.servlet.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.ServletContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void correctUrl() throws Exception {
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
    public void simpleFields() throws Exception {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe")
                .currentAddress(new Address(1, "Street", 5222, "New York"))
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
        addUserForm.setDiplomas(Arrays.asList(new Diploma("License", bachelorDate), new Diploma("MSC", masterDate)));
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
                .usernamesArray(new String[] {"john.doe", "jdoe"})
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
                .formerAddresses(new Address[] {new Address(10, "Street", 5222, "Chicago"), new Address(20, "Street", 5222, "Washington")})
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("john.doe", request.getParameter("usernames[0]"));
        assertEquals("jdoe", request.getParameter("usernames[1]"));
    }

    @Test
    public void enumField() {
        final AddUserForm addUserForm = AddUserForm.builder()
                .firstName("John").name("Doe").gender(Gender.MALE)
                .build();
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
        final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

        assertEquals("MALE", request.getParameter("gender"));
    }

    @Test
    public void registerCustomPropertyEditor() {

        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor("dd/MM/yyyy"));
        final AddUserForm addUserForm = new AddUserForm("John", "Doe", LocalDate.now(), null);
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), request.getParameter("birthDate"));

//
//        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
//        final LocalDate userBirthDate = LocalDate.of(2016, 8, 29);
//        final AddUserForm addUserForm = new AddUserForm("John", "Doe", userBirthDate, null);
//        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);
//
//        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
//        assertEquals(userBirthDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)), request.getParameter("birthDate"));
    }

    @Test
    public void unregisterPropertyEditor() {
        MockMvcRequestBuilderUtils.unregisterPropertyEditor(LocalDate.class);
        final LocalDate userBirthDate = LocalDate.of(2016, 8, 29);
        final AddUserForm addUserForm = new AddUserForm("John", "Doe", userBirthDate, null);
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

        MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
        assertEquals(String.valueOf(userBirthDate), request.getParameter("birthDate"));
    }

    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Setter
    static class AddUserForm {
        private String firstName;
        private String name;
        private Gender gender;
        private LocalDate birthDate;
        private Address currentAddress;
        private List<String> usernames;
        private String[] usernamesArray;
        private List<Diploma> diplomas;
        private Address[] formerAddresses;

        public AddUserForm(String firstName, String name, LocalDate birthDate, Address currentAddress) {
            this.firstName = firstName;
            this.name = name;
            this.birthDate = birthDate;
            this.currentAddress = currentAddress;
        }
    }

    enum Gender {
        MALE,
        FEMALE
    }

    static class Diploma {
        private String name;
        private LocalDate date;

        public Diploma(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }
    }

    static class Address {
        private int streetNumber;
        private String streetName;
        private int postalCode;
        private String city;

        public Address(int streetNumber, String streetName, int postalCode, String city) {
            this.streetNumber = streetNumber;
            this.streetName = streetName;
            this.postalCode = postalCode;
            this.city = city;
        }
    }
}
