# Spring MVC Test utils

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1e521e339dc446328b7ccc52faaa7648)](https://www.codacy.com/app/f-lopes/spring-mvc-test-utils?utm_source=github.com&utm_medium=referral&utm_content=f-lopes/spring-mvc-test-utils&utm_campaign=badger)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.florianlopes/spring-mvc-test-utils/badge.svg)](https://search.maven.org/#artifactdetails%7Cio.florianlopes%7Cspring-mvc-test-utils%7C1.0.0%7Cjar)
[![CircleCI](https://circleci.com/gh/f-lopes/spring-mvc-test-utils/tree/master.svg?style=svg&circle-token=b34b191c4f183c701aee405ba6dca66c9f277d49)](https://circleci.com/gh/f-lopes/spring-mvc-test-utils/tree/master)
[![codecov](https://codecov.io/gh/f-lopes/spring-mvc-test-utils/branch/master/graph/badge.svg?token=2yY70RB1tw)](https://codecov.io/gh/f-lopes/spring-mvc-test-utils)
[![Dependency Status](https://www.versioneye.com/user/projects/57e036e36dfcd0003e7f55c3/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57e036e36dfcd0003e7f55c3)

Test library aimed to ease Spring MVC form validation tests. Easily post an entire form to a given url.

See [MockMvcRequestBuilderUtils](src/main/java/io/florianlopes/spring/test/web/servlet/request/MockMvcRequestBuilderUtils.java)

## How to use?

Add this dependency to your pom.xml file:
```
<dependency>
    <groupId>io.florianlopes</groupId>
    <artifactId>spring-mvc-test-utils</artifactId>
    <version>1.0.0</version>
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

### Register property editor(s)
Register a PropertyEditor to send properly formatted fields:
```
MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor("dd/MM/yyyy"));

final AddUserForm addUserForm = new AddUserForm("John", "Doe", LocalDate.now(), null);
final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilderUtils.postForm(POST_FORM_URL, addUserForm);

MockHttpServletRequest request = mockHttpServletRequestBuilder.buildRequest(this.servletContext);
assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), request.getParameter("birthDate"));
```

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