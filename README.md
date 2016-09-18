# Spring MVC Test utils

[![CircleCI](https://circleci.com/gh/f-lopes/spring-mvc-test-utils/tree/master.svg?style=svg&circle-token=b34b191c4f183c701aee405ba6dca66c9f277d49)](https://circleci.com/gh/f-lopes/spring-mvc-test-utils/tree/master)

[![codecov](https://codecov.io/gh/f-lopes/spring-mvc-test-utils/branch/master/graph/badge.svg?token=2yY70RB1tw)](https://codecov.io/gh/f-lopes/spring-mvc-test-utils)

[![versioneye](https://www.versioneye.com/user/projects/57dd76f8037c2000458f6c52/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57dd76f8037c2000458f6c52/)

Test library aimed to ease Spring MVC form validation tests. Easily post an entire form to a given url.

See [MockMvcRequestBuilderUtils](src/main/java/io/florianlopes/spring/test/web/servlet/request/MockMvcRequestBuilderUtils.java)

## How to use?

This library will be soon available at the Maven Central Repository http://search.maven.org/.

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
