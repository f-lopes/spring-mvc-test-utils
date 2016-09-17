# Spring MVC Test utils

Test library aimed to ease Spring MVC form validation tests. Easily post an entire form to a given url.

See [MockMvcRequestBuilderUtils](src/main/java/io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils)

## How to use?
```MockMvcRequestBuilderUtils.postForm("/url", formObject);```

Example:
```
@Test
public void testSimpleFields() throws Exception {
    final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm("/test",
            new AddUserForm("John", "Doe", null, new Address(1, "Street", 5222, "New York")));
    final MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);

    assertEquals("John", request.getParameter("firstName"));
    assertEquals("New York", request.getParameter("address.city"));
}
```

### Register property editor(s)
Register a PropertyEditor to send properly formatted fields:
```
MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor("dd/MM/yyyy"));

final AddUserForm addUserForm = new AddUserForm("John", "Doe", LocalDate.now(), null);
final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), request.getParameter("birthDate"));
```