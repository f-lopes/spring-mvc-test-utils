package io.florianlopes.spring.test.web.servlet.request;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

class FormFieldWrapper {

    private final Field rawField;
    private final FormFieldWrapper parent;

    private final Object targetObject;
    private final String indexOrPosition;

    private final Configuration configuration;

    private final List<FormFieldWrapper> children;

    FormFieldWrapper(Field rawField, FormFieldWrapper parent, Object targetObject,
                     String indexOrPosition, Configuration configuration) {
        this.rawField = rawField;
        this.parent = parent;
        this.targetObject = targetObject;
        this.indexOrPosition = indexOrPosition;
        this.configuration = configuration;

        if (this.targetObject != null) {
            this.children = discoverChildren();
        } else {
            this.children = Collections.emptyList();
        }
    }

    FormFieldWrapper(Object form, Configuration config) {
        this(null, null, form, null, config);
    }

    private FormFieldWrapper(Field rawField, FormFieldWrapper parent, Object targetObject) {
        this(rawField, parent, targetObject, null, null);
    }

    private FormFieldWrapper(FormFieldWrapper parent, Object targetObject, String indexOrPosition) {
        this(null, parent, targetObject, indexOrPosition, null);
    }

    private static FormFieldWrapper newSimpleFieldWrapper(Field rawField, FormFieldWrapper parent, Object targetObject) {
        return new FormFieldWrapper(rawField, parent, targetObject);
    }

    private static FormFieldWrapper newMapFieldWrapper(FormFieldWrapper parent, Object targetObject, String index) {
        return new FormFieldWrapper(parent, targetObject, index);
    }

    private static FormFieldWrapper newIterableFieldWrapper(FormFieldWrapper parent, Object targetObject, String position) {
        return new FormFieldWrapper(parent, targetObject, position);
    }

    Map<String, String> collectFields() {
        final Map<String, String> fields = new HashMap<>();
        if (hasChildren()) {
            fields.putAll(collectChildrenFields());
        } else {
            if (this.targetObject != null || this.parent != null && this.parent.isMap()) {
                fields.put(getNestedPath(), stringRepresentation());
            }
        }
        return fields;
    }

    private Map<String, String> collectChildrenFields() {
        final Map<String, String> fields = new HashMap<>();
        this.children.forEach(child -> {
            if (child.hasChildren()) {
                child.getChildren().forEach(
                        grandChild -> fields.putAll(grandChild.collectFields())
                );
            } else if (child.getTargetObject() != null) {
                fields.put(child.getNestedPath(), child.stringRepresentation());
            }
        });
        return fields;
    }

    private List<FormFieldWrapper> discoverChildren() {
        final List<FormFieldWrapper> children = new ArrayList<>();

        if (isIterable()) {
            final Iterable<?> iterableTargetObject =
                    Iterable.class.isAssignableFrom(this.rawField.getType()) ?
                            (Iterable<?>) targetObject :
                            CollectionUtils.arrayToList(targetObject);

            final AtomicInteger position = new AtomicInteger(0);
            StreamSupport.stream(iterableTargetObject.spliterator(), false)
                    .forEach(object -> children.add(
                            newIterableFieldWrapper(
                                    this,
                                    object,
                                    String.valueOf(position.getAndIncrement())
                            )
                    ));
        } else if (isMap()) {
            final Map<?, ?> mapTargetObject = (Map<?, ?>) this.targetObject;
            mapTargetObject.forEach((key, value) -> children.add(
                    newMapFieldWrapper(
                            this,
                            value,
                            String.valueOf(key)
                    )
            ));
        } else if (isComplex()) {
            FieldUtils.getAllFieldsList(targetObject.getClass())
                    .stream()
                    .filter(getConfiguration().fieldPredicate())
                    .forEach(field -> children.add(
                            newSimpleFieldWrapper(
                                    field,
                                    this,
                                    ReflectionTestUtils.getField(targetObject, field.getName())
                            )
                    ));
        }

        return children;
    }

    private boolean isComplex() {
        if (this.rawField != null) {
            final Class<?> fieldType = getFieldType();
            if (fieldType.isAssignableFrom(BigInteger.class) || fieldType.isAssignableFrom(BigDecimal.class)) {
                return false;
            } else {
                return isComplexType(fieldType);
            }
        } else {
            return isComplexType(this.targetObject.getClass());
        }
    }

    private boolean isComplexType(Class<?> type) {
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

    private boolean isGeneric() {
        return this.rawField != null && !(this.rawField.getGenericType() instanceof Class);
    }

    private boolean isMap() {
        return this.rawField != null && Map.class.isAssignableFrom(this.rawField.getType());
    }

    private boolean isIterable() {
        return this.rawField != null &&
               (Iterable.class.isAssignableFrom(this.rawField.getType()) || Object[].class.isAssignableFrom(this.rawField.getType()));
    }

    private Object getTargetObject() {
        return targetObject;
    }

    private List<FormFieldWrapper> getChildren() {
        return this.children;
    }

    private FormFieldWrapper getParent() {
        return parent;
    }

    private Configuration getConfiguration() {
        if (this.configuration == null && this.parent != null) {
            return this.parent.getConfiguration();
        }
        return this.configuration;
    }

    private String getIndexOrPosition() {
        return indexOrPosition;
    }

    private Class<?> getFieldType() {
        if (isGeneric()) {
            final ParameterizedType genericType = (ParameterizedType) this.rawField.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        } else {
            return this.rawField.getType();
        }
    }

    private String getPath() {
        return this.rawField != null ? this.rawField.getName() : StringUtils.EMPTY;
    }

    private String getNestedPath() {
        final String path = getPath();
        if (this.parent != null) {
            if (this.parent.hasIndexOrPosition()) {
                return String.format("%s[%s].%s", this.parent.getParent().getNestedPath(), this.parent.getIndexOrPosition(), path);
            } else if (StringUtils.isNotEmpty(this.parent.getNestedPath())) {
                if (this.hasIndexOrPosition()) {
                    return String.format("%s[%s]%s", this.parent.getNestedPath(), this.getIndexOrPosition(), path);
                } else {
                    return String.format("%s.%s", this.parent.getNestedPath(), path);
                }
            }
        }
        return path;
    }

    private String stringRepresentation() {
        if (this.rawField != null && getConfiguration().hasPropertyEditorFor(this.getFieldType())) {
            final PropertyEditor propertyEditor = getConfiguration().propertyEditorFor(this.getFieldType());
            propertyEditor.setValue(this.targetObject);
            return propertyEditor.getAsText();
        } else {
            return this.targetObject != null ? String.valueOf(this.targetObject) : StringUtils.EMPTY;
        }
    }

    private boolean hasIndexOrPosition() {
        return StringUtils.isNotEmpty(this.indexOrPosition);
    }

    private boolean hasChildren() {
        return !this.children.isEmpty();
    }
}
