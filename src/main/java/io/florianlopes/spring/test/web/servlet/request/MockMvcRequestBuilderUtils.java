package io.florianlopes.spring.test.web.servlet.request;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Custom MockMvcRequestBuilder to post an entire form to a given url.
 * Useful to test Spring MVC form validation.
 *
 * @author Florian Lopes
 * @see org.springframework.test.web.servlet.request.MockMvcRequestBuilders
 */
public class MockMvcRequestBuilderUtils {

    private static final PropertyEditorRegistrySupport PROPERTY_EDITOR_REGISTRY = new SimpleTypeConverter();
    private static final Configuration DEFAULT_CONFIG = Configuration.builder()
            .includeFinal(true)
            .includeTransient(false)
            .includeStatic(false)
            .build();

    private static final Logger LOGGER = LoggerFactory.getLogger(MockMvcRequestBuilderUtils.class);

    private MockMvcRequestBuilderUtils() {
    }

    /**
     * Register custom property editor for a given type.
     *
     * @param type type of the property
     * @param propertyEditor {@link PropertyEditor} to register
     */
    public static void registerPropertyEditor(Class type, PropertyEditor propertyEditor) {
        PROPERTY_EDITOR_REGISTRY.registerCustomEditor(type, propertyEditor);
    }

    public static FormRequestPostProcessor form(Object form) {
        return form(form, DEFAULT_CONFIG);
    }

    public static FormRequestPostProcessor form(Object form, Configuration config) {
        return new FormRequestPostProcessor(form, config);
    }

    /**
     * Post a form to the given url.
     * All non-null, non-static, non-final, non-transient form fields will be added as HTTP request parameters using POST method
     *
     * @param url the URL to post the form to
     * @param form form object to send using POST method
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder postForm(String url, Object form) {
        return postForm(url, form, DEFAULT_CONFIG);
    }

    /**
     * Post a form to the given url.
     * All non-null, non-static, non-final form fields will be added as HTTP request parameters using POST method
     *
     * @param url the URL to post the form to
     * @param form form object to send using POST method
     * @param config configuration object
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder postForm(String url, Object form, Configuration config) {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return buildFormFields(form, mockHttpServletRequestBuilder, config);
    }

    /**
     * Put a form to the given url.
     * All non-null, non-static, non-final, non-transient form fields will be added as HTTP request parameters using PUT method
     *
     * @param url the URL to put the form to
     * @param form form object to send using PUT method
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder putForm(String url, Object form) {
        return putForm(url, form, DEFAULT_CONFIG);
    }

    /**
     * Put a form to the given url.
     * All non-null, non-static, non-final form fields will be added as HTTP request parameters using PUT method
     *
     * @param url the URL to put the form to
     * @param form form object to send using PUT method
     * @param config configuration object
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder putForm(String url, Object form, Configuration config) {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return buildFormFields(form, mockHttpServletRequestBuilder, config);
    }

    private static MockHttpServletRequestBuilder buildFormFields(Object form, MockHttpServletRequestBuilder mockHttpServletRequestBuilder, Configuration config) {
        final Map<String, String> formFields = getFormFields(form, new TreeMap<>(), StringUtils.EMPTY, config);
        formFields.forEach((path, value) -> {
            LOGGER.debug("Adding form field ({}={}) to HTTP request parameters", path, value);
            mockHttpServletRequestBuilder.param(path, value);
        });

        return mockHttpServletRequestBuilder;
    }

    private static Map<String, String> getFormFields(Object form, Map<String, String> formFields, String path, Configuration config) {
        final List<Field> fields = form != null ? getFormFields(form, config) : new ArrayList<>();
        for (Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object fieldValue = getFieldValue(form, field);
            if (fieldValue != null) {
                if (isIterable(fieldType)) {
                    final Iterable<?> iterableObject = getIterable(fieldValue, fieldType);
                    if (iterableObject != null) {
                        if (isComplexField(field)) {
                            int i = 0;
                            for (Object object : iterableObject) {
                                final String nestedPath = getPositionedField(path, field, i) + ".";
                                formFields.putAll(getFormFields(object, formFields, nestedPath, config));
                                i++;
                            }
                        } else {
                            formFields.putAll(getCollectionFields(iterableObject, path, field));
                        }
                    }
                } else if (isMap(fieldType)) {
                    final Map<?, ?> map = getMap(fieldValue, fieldType);
                    if (map != null) {
                        map.forEach((key, value) -> formFields.put(getPositionedField(path, field, getStringValue(key)), getStringValue(value)));
                    }
                } else {
                    if (!isComplexField(field) || hasPropertyEditorFor(fieldType)) {
                        // Resolve field's value either using a property editor or its string representation
                        formFields.put(path + field.getName(), getStringValue(fieldValue));
                    } else {
                        // Iterate over object's fields
                        final String nestedPath = getNestedPath(field);
                        formFields.putAll(getFormFields(ReflectionTestUtils.getField(form, field.getName()), formFields, nestedPath, config));
                    }
                }
            }
        }
        return formFields;
    }

    private static List<Field> getFormFields(Object form, Configuration config) {
        return FieldUtils.getAllFieldsList(form.getClass())
                .stream()
                .filter(config.fieldPredicate)
                .collect(Collectors.toList());
    }

    private static Map<?, ?> getMap(Object fieldValue, Class<?> type) {
        return Map.class.isAssignableFrom(type) ? (Map) fieldValue : null;
    }

    private static Iterable<?> getIterable(Object fieldValue, Class<?> type) {
        return Iterable.class.isAssignableFrom(type) ? (Iterable<?>) fieldValue : CollectionUtils.arrayToList(fieldValue);
    }

    private static Map<String, String> getCollectionFields(Iterable<?> iterable, String path, Field field) {
        final Map<String, String> fields = new TreeMap<>();
        int i = 0;
        for (Object object : iterable) {
            fields.put(getPositionedField(path, field, i), getStringValue(object));
            i++;
        }
        return fields;
    }

    private static String getPositionedField(String path, Field field, int position) {
        return getPositionedField(path, field, String.valueOf(position));
    }

    private static String getPositionedField(String path, Field field, String positionOrKey) {
        return String.format("%s%s[%s]", path, field.getName(), positionOrKey);
    }

    private static String getNestedPath(Field field) {
        return field.getName() + ".";
    }

    private static Object getFieldValue(Object form, Field field) {
        return ReflectionTestUtils.getField(form, field.getName());
    }

    private static String getStringValue(Object object) {
        if (object != null) {
            final PropertyEditor propertyEditor = getPropertyEditorFor(object);
            if (propertyEditor != null) {
                propertyEditor.setValue(object);
                return propertyEditor.getAsText();
            }
            // Default strategy
            return String.valueOf(object);
        }
        return StringUtils.EMPTY;
    }

    private static boolean hasPropertyEditorFor(Class<?> type) {
        return PROPERTY_EDITOR_REGISTRY.hasCustomEditorForElement(type, null) ||
                PROPERTY_EDITOR_REGISTRY.getDefaultEditor(type) != null;
    }

    private static PropertyEditor getPropertyEditorFor(Object object) {
        return PROPERTY_EDITOR_REGISTRY.hasCustomEditorForElement(object.getClass(), null) ? PROPERTY_EDITOR_REGISTRY.findCustomEditor(object.getClass(), null)
                : PROPERTY_EDITOR_REGISTRY.getDefaultEditor(object.getClass());
    }

    private static boolean isComplexField(Field field) {
        if (isGeneric(field)) {
            final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            final Type[] actualTypeArguments = genericType.getActualTypeArguments();
            return isComplexType((Class<?>) actualTypeArguments[0]);
        } else {
            return isComplexType(field.getType());
        }
    }

    private static boolean isGeneric(Field field) {
        return !(field.getGenericType() instanceof Class);
    }

    private static boolean isComplexType(Class<?> type) {
        if (type.getComponentType() != null) {
            return isComplexType(type.getComponentType());
        }
        return !ClassUtils.isPrimitiveOrWrapper(type)
                && !String.class.isAssignableFrom(type)
                && !Date.class.isAssignableFrom(type)
                && !Temporal.class.isAssignableFrom(type)
                && type.getSuperclass() != null
                && !Enum.class.isAssignableFrom(type.getSuperclass());
    }

    private static boolean isIterable(Class fieldClass) {
        return Iterable.class.isAssignableFrom(fieldClass) || Object[].class.isAssignableFrom(fieldClass);
    }

    private static boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    /**
     * Implementation of {@link RequestPostProcessor} that adds form parameters to the request before execution.
     * 
     * @see MockMvcRequestBuilderUtils#postForm(String, Object) for implementation details
     */
    public static class FormRequestPostProcessor implements RequestPostProcessor {

        private final Object form;
        private final Configuration config;

        private FormRequestPostProcessor(Object form, Configuration config) {
            this.form = form;
            this.config = config;
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            final Map<String, String> formFields = getFormFields(form, new TreeMap<>(), StringUtils.EMPTY, config);
            formFields.forEach((path, value) -> {
                LOGGER.debug("Adding form field ({}={}) to HTTP request parameters", path, value);
                request.addParameter(path, value);
            });
            return request;
        }
    }

    /**
     * Configuration class that allows the exclusion of specific fields.
     */
    public static class Configuration {

        private final Predicate<Field> fieldPredicate;

        private Configuration(Predicate<Field> fieldPredicate) {
            this.fieldPredicate = fieldPredicate;
        }

        /**
         * Creates a new builder that excludes transient and static fields by default.
         */
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private static final Predicate<Field> BASE_PREDICATE = Builder::isNotSynthetic;

            private Predicate<Field> fieldPredicate;
            private boolean includeFinal = true;
            private boolean includeTransient = false;
            private boolean includeStatic = false;

            public Builder fieldPredicate(Predicate<Field> fieldPredicate) {
                this.fieldPredicate = Objects.requireNonNull(fieldPredicate, "fieldPredicate cannot be null");
                return this;
            }

            public Builder includeTransient(boolean includeTransient) {
                this.includeTransient = includeTransient;
                return this;
            }

            public Builder includeFinal(boolean includeFinal) {
                this.includeFinal = includeFinal;
                return this;
            }

            public Builder includeStatic(boolean includeStatic) {
                this.includeStatic = includeStatic;
                return this;
            }

            public Configuration build() {
                Predicate<Field> fieldPredicate = this.fieldPredicate != null ? BASE_PREDICATE.and(this.fieldPredicate) : BASE_PREDICATE;

                if (!this.includeFinal) {
                    fieldPredicate = fieldPredicate.and(Builder::isNotFinal);
                }

                if (!this.includeTransient) {
                    fieldPredicate = fieldPredicate.and(Builder::isNotTransient);
                }

                if (!this.includeStatic) {
                    fieldPredicate = fieldPredicate.and(Builder::isNotStatic);
                }

                return new Configuration(fieldPredicate);
            }

            private static boolean isNotFinal(Field field) {
                return !Modifier.isFinal(field.getModifiers());
            }

            private static boolean isNotTransient(Field field) {
                return !Modifier.isTransient(field.getModifiers());
            }

            private static boolean isNotStatic(Field field) {
                return !Modifier.isStatic(field.getModifiers());
            }

            private static boolean isNotSynthetic(Field field) {
                return !field.isSynthetic();
            }

        }

    }

}
