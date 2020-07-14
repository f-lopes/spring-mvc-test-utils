[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/f-lopes/spring-mvc-test-utils)

# Spring MVC Test utils

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.florianlopes/spring-mvc-test-utils/badge.svg)](https://search.maven.org/#artifactdetails%7Cio.florianlopes%7Cspring-mvc-test-utils%7C2.2.1%7Cjar)
[![Build Status](https://travis-ci.org/f-lopes/spring-mvc-test-utils.svg?branch=develop)](https://travis-ci.org/f-lopes/spring-mvc-test-utils)
[![codecov](https://codecov.io/gh/f-lopes/spring-mvc-test-utils/branch/master/graph/badge.svg?token=2yY70RB1tw)](https://codecov.io/gh/f-lopes/spring-mvc-test-utils)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1e521e339dc446328b7ccc52faaa7648)](https://www.codacy.com/app/f-lopes/spring-mvc-test-utils?utm_source=github.com&utm_medium=referral&utm_content=f-lopes/spring-mvc-test-utils&utm_campaign=badger)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/f-lopes/spring-mvc-test-utils.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/f-lopes/spring-mvc-test-utils/context:java)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=f-lopes/spring-mvc-test-utils)](https://dependabot.com)

Test library aimed to ease Spring MVC form validation tests. Easily post an entire form to a given url.

See [MockMvcRequestBuilderUtils](src/main/java/io/florianlopes/spring/test/web/servlet/request/MockMvcRequestBuilderUtils.java)

More details here: [blog.florianlopes.io](https://blog.florianlopes.io/tool-for-spring-mockmvcrequestbuilder-forms-tests/)

## How to use?

Add this dependency to your pom.xml file:
```
<dependency>
    <groupId>io.florianlopes</groupId>
    <artifactId>spring-mvc-test-utils</artifactId>
    <version>2.2.1</version>
    <scope>test</scope>
</dependency>
```

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

Usage with MockMvc:
```
final AddUserForm addUserForm = new AddUserForm("John", "Doe", null, new Address(1, "Street", 5222, "New York")));

mockMvc.perform(MockMvcRequestBuilderUtils.postForm("/users", addUserForm))
		.andExpect(MockMvcResultMatchers.model().hasNoErrors());
```

Using `with()` syntax (`FormRequestPostProcessor`):

```
final AddUserForm addUserForm = new AddUserForm("John", "Doe", null, new Address(1, "Street", 5222, "New York")));

// POST
mockMvc.perform(post("/users").with(MockMvcRequestBuilderUtils.form(addUserForm)))
		.andExpect(MockMvcResultMatchers.model().hasNoErrors());

// GET
mockMvc.perform(get("/users").with(MockMvcRequestBuilderUtils.form(addUserForm)))
		.andExpect(MockMvcResultMatchers.model().hasNoErrors());
		
// PUT
mockMvc.perform(put("/users").with(MockMvcRequestBuilderUtils.form(addUserForm)))
		.andExpect(MockMvcResultMatchers.model().hasNoErrors());
```

### Register property editor(s)

This tool relies on default Spring's property editors (see https://github.com/spring-projects/spring-framework/blob/master/spring-beans/src/main/java/org/springframework/beans/PropertyEditorRegistrySupport.java#L200).

If you want to override one of those registered by default, simple use the `MockMvcRequestBuilderUtils.registerPropertyEditor(...)` method:
```
MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor("dd/MM/yyyy"));

final AddUserForm addUserForm = new AddUserForm("John", "Doe", LocalDate.now(), null);
final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), request.getParameter("birthDate"));
```

## Limitations and restrictions
This helper utility handles your form objects using the Java Reflection API. This implies
some restrictions in the usage within your test cases:

* As long as you use simple, common Java types like `String`, etc, the mocked
  HTTPServletRequest should not fail to be processed by data binding.
* You can always provide a custom property editor (see above).

* Converting data using classes from the Java Collection API is supported since
  version 1.0.0. The parameters will follow the convention `name[index] = value`.
  * Currently, no multidimensional collections (like array of arrays) are supported.

* Converting data using classes from the Java Map API is supported in a simple
  manner since version 1.1.0. The parameters will follow the convention
  `name[key] = value`.
  * Currently, no map of maps is supported, only simple datatypes with key and
    value easily transformable to a `String`.

* As a last resort, your properties will be converted using the `toString()`
  method of the member object under the name of the object.

## Contributing

Feel free to contribute using this guide:

1. Fork this project
2. Clone your forked repository
    ```git clone git@github.com:{your-username}/spring-mvc-test-utils.git```
3. Add a new remote pointing to the original repository
    ```git remote add upstream git@github.com:flopes/spring-mvc-test-utils.git```
4. Create a new branch for your feature
    ```git branch -b my-feature```
5. Commit your changes (and squash them if necessary using `git rebase -i` or `git add -p`)
6. Pull the latest changes from the original repository
    ```git checkout master && git pull --rebase upstream master```
7. Rebase master branch with your feature
    ```git checkout my-feature && git rebase master```
    Solve any existing conflicts
8. Push your changes and create a PR on GitHub
    ```git push -u origin my-feature```
    Go to the original repository and create a new PR with comments.
