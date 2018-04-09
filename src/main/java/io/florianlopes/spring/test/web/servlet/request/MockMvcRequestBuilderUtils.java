package io.florianlopes.spring.test.web.servlet.request;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Custom MockMvcRequestBuilder to post an entire form to a given url.
 * Useful to test Spring MVC form validation.
 * @author Florian Lopes
 * @see org.springframework.test.web.servlet.request.MockMvcRequestBuilders
 */
public class MockMvcRequestBuilderUtils {

    private static final Map<Class, PropertyEditor> propertyEditors = new HashMap<>();

    private MockMvcRequestBuilderUtils() {
    }

    /**
     * Register custom property editor for a given type.
     * @param type type of the property
     * @param propertyEditor {@link PropertyEditor} to register
     */
    public static void registerPropertyEditor(Class type, PropertyEditor propertyEditor) {
        propertyEditors.put(type, propertyEditor);
    }

    /**
     * Unregister custom property editor for a given type
     * @param type type of the property
     */
    public static void unregisterPropertyEditor(Class type) {
        propertyEditors.remove(type);
    }

    /**
     * Unregister all previously registered property editors
     */
    public static void unregisterPropertyEditors() {
        propertyEditors.clear();
    }

    /**
     * Post a form to the given url.
     * All non null form fields will be posted as HTTP parameters using POST method
     * @param url url to post the form to
     * @param form form object to send using POST method
     * @return mockHttpServletRequestBuilder wrapped mockHttpServletRequestBuilder
     */
    public static MockHttpServletRequestBuilder postForm(String url, Object form) {
        final MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
                .characterEncoding(CharEncoding.UTF_8).contentType(MediaType.APPLICATION_FORM_URLENCODED);
        final Map<String, String> formFields = getFormFields(form, new TreeMap<>(), StringUtils.EMPTY);
        formFields.forEach(mockHttpServletRequestBuilder::param);

        return mockHttpServletRequestBuilder;
    }

    private static Map<String, String> getFormFields(Object form, Map<String, String> formFields, String path) {
        final List<Field> fields = form != null ? Arrays.asList(FieldUtils.getAllFields(form.getClass())) : new ArrayList<>();
        for (Field field : fields) {
            if (isIterable(field.getType())) {
                final Iterable<?> iterable = getIterable(getFieldValue(form, field), field.getType());
                if (iterable != null) {
                    if (isComplexField(field)) {
                        int i = 0;
                        for (Object o : iterable) {
                            final String nestedPath = getPositionedField(path, field, i) + ".";
                            formFields.putAll(getFormFields(o, formFields, nestedPath));
                            i++;
                        }
                    } else {
                        formFields.putAll(getCollectionFields(iterable, path, field));
                    }
                }
            } else {
                if (isComplexField(field)) {
                    final String nestedPath = getNestedPath(path, field);
                    formFields.putAll(getFormFields(ReflectionTestUtils.getField(form, field.getName()), formFields, nestedPath));
                } else {
                    formFields.put(path + field.getName(), getFieldStringValue(form, field));
                }
            }
        }
        return formFields;
    }

    private static Iterable<?> getIterable(Object fieldValue, Class<?> type) {
        return fieldValue != null
                ? Iterable.class.isAssignableFrom(type) ? (Iterable<?>) fieldValue : CollectionUtils.arrayToList(fieldValue)
                : null;
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
        return path + field.getName() + "[" + position + "]";
    }

    private static String getNestedPath(String path, Field field) {
        return StringUtils.isNotEmpty(path)
                ? path + field.getName() + "."
                : field.getName() + ".";
    }

    private static Object getFieldValue(Object form, Field field) {
        return ReflectionTestUtils.getField(form, field.getName());
    }

    private static String getFieldStringValue(Object object, Field field) {
        return getStringValue(getFieldValue(object, field));
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

    private static PropertyEditor getPropertyEditorFor(Object object) {
        final Optional<Map.Entry<Class, PropertyEditor>> propertyEditorEntry = propertyEditors.entrySet().stream()
                .filter(entry -> entry.getKey().equals(object.getClass()))
                .findFirst();
        return propertyEditorEntry.isPresent() ? propertyEditorEntry.get().getValue() : null;
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
}
