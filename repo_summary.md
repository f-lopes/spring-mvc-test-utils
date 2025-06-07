# Project Summary: Spring MVC Test Utils

## Overview

The project, **Spring MVC Test Utils**, is a Java library designed to simplify the testing of form submissions and data binding in Spring MVC applications. It provides utility methods to easily convert a Java object (representing a form) into a `MockHttpServletRequestBuilder`, which can then be used with Spring's `MockMvc` to perform integration tests.

## Problem Solved

Testing form submissions in Spring MVC typically involves manually setting up request parameters, which can be cumbersome and error-prone, especially for complex forms. This library automates that process by using reflection to map fields from a form object to request parameters. This makes tests cleaner, easier to write, and more maintainable.

The core utility class `io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils` offers methods like `postForm()` and `form()` that take a URL and a form object as input, returning a request builder pre-configured with the form data.

## Structure

The repository is a multi-module Maven project:

1.  **`spring-mvc-test-utils`**: This is the main module containing the library's source code. It provides the `MockMvcRequestBuilderUtils` class and related components.
2.  **`smoke-tests`**: This module contains integration tests that act as smoke tests for the library. These tests ensure the library functions correctly with specified versions of Java and the Spring Framework.

The project uses common Java development practices:
*   **Maven:** For build automation and dependency management (`pom.xml`).
*   **GitHub Actions:** For continuous integration and deployment (CI/CD), including automated builds, testing, code quality analysis, and releases (configured in `.github/workflows`).
*   **Testing:** The library itself is designed for testing, and it includes its own unit tests and smoke tests.
*   **Dependency:** It is intended to be used as a test-scoped dependency in other Spring MVC projects.

## Key Features

*   Automatic conversion of form objects to request parameters using Java Reflection.
*   Support for simple Java types, collections (e.g., `List`, `Set`), and maps.
*   Ability to register custom `PropertyEditor` instances for handling specific data types or custom formatting.
*   Integration with `MockMvc` for writing expressive and fluent tests.
*   Provides `FormRequestPostProcessor` for use with `MockMvc`'s `with()` syntax for GET, POST, and PUT requests.

## Limitations

*   Relies on Java Reflection, which has inherent limitations.
*   Multidimensional collections (e.g., array of arrays) are not supported.
*   Map of maps is not supported; only simple data types for map keys and values that can be easily converted to strings.
*   Properties are converted using `toString()` as a last resort if no specific editor is found.

## Target Audience

Java developers working with the Spring MVC framework who need to write integration tests for their controllers, specifically for form submissions and data binding logic.

## Recent Updates (Version 4.0.0)

*   Minimum Java version required is now Java 17 (Java 11 is no longer supported).
*   Enhanced support for "complex" data types as values in Maps.
*   Tested with Spring Framework 6.1.12, Java 17, and Java 21.
