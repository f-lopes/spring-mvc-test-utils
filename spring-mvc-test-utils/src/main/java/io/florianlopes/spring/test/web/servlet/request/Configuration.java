package io.florianlopes.spring.test.web.servlet.request;

import org.springframework.beans.PropertyEditorRegistrySupport;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Configuration class that allows inclusion/exclusion of specific fields.
 * Also allows to register one or more {@link PropertyEditor}
 * to customize how field values are added to the HTTP request
 */
public class Configuration {

    public static final Configuration DEFAULT = new Builder()
            .includeFinal(true)
            .includeTransient(false)
            .includeStatic(false)
            .build();
    public static final Configuration EXCLUDE_FINAL = new Builder()
            .includeFinal(false)
            .includeTransient(false)
            .build();
    public static final Configuration INCLUDE_TRANSIENT = new Builder()
            .includeTransient(true)
            .includeFinal(false)
            .build();
    public static final Configuration INCLUDE_STATIC = new Builder()
            .includeStatic(true)
            .includeFinal(false)
            .build();

    private final Predicate<Field> fieldPredicate;
    private final PropertyEditorRegistrySupport propertyEditorRegistrySupport;

    private Configuration(Predicate<Field> fieldPredicate, PropertyEditorRegistrySupport propertyEditorRegistrySupport) {
        this.fieldPredicate = fieldPredicate;
        this.propertyEditorRegistrySupport = propertyEditorRegistrySupport;
    }

    /**
     * Creates a new builder that excludes transient and static fields by default.
     */
    public static Builder builder() {
        return new Builder();
    }

    public Predicate<Field> fieldPredicate() {
        return fieldPredicate;
    }

    public PropertyEditor propertyEditorFor(Class<?> propertyEditorClass) {
        return this.propertyEditorRegistrySupport.hasCustomEditorForElement(propertyEditorClass, null) ?
                this.propertyEditorRegistrySupport.findCustomEditor(propertyEditorClass, null) :
                this.propertyEditorRegistrySupport.getDefaultEditor(propertyEditorClass);
    }

    public boolean hasPropertyEditorFor(Class<?> propertyEditorClass) {
        return this.propertyEditorRegistrySupport.hasCustomEditorForElement(propertyEditorClass, null) ||
               this.propertyEditorRegistrySupport.getDefaultEditor(propertyEditorClass) != null;
    }

    public static class Builder {

        private static final Predicate<Field> BASE_PREDICATE = FieldPredicates::isNotSynthetic;

        private final PropertyEditorRegistrySupport propertyEditorRegistrySupport = new PropertyEditorRegistrySupport();
        private Predicate<Field> fieldPredicate;
        private boolean includeFinal = true;
        private boolean includeTransient = false;
        private boolean includeStatic = false;

        private Builder() {
        }

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

        public Builder withPropertyEditor(PropertyEditorSupport propertyEditor, Class<?> propertyEditorClass) {
            Objects.requireNonNull(propertyEditor, "propertyEditor cannot be null");
            this.propertyEditorRegistrySupport.registerCustomEditor(propertyEditorClass, propertyEditor);
            return this;
        }

        public Configuration build() {
            Predicate<Field> fieldPredicate = this.fieldPredicate != null ? BASE_PREDICATE.and(this.fieldPredicate) : BASE_PREDICATE;

            if (!this.includeFinal) {
                fieldPredicate = fieldPredicate.and(FieldPredicates::isNotFinal);
            }

            if (!this.includeTransient) {
                fieldPredicate = fieldPredicate.and(FieldPredicates::isNotTransient);
            }

            if (!this.includeStatic) {
                fieldPredicate = fieldPredicate.and(FieldPredicates::isNotStatic);
            }

            return new Configuration(fieldPredicate, this.propertyEditorRegistrySupport);
        }
    }

    static class FieldPredicates {

        public static boolean isNotFinal(Field field) {
            return !Modifier.isFinal(field.getModifiers());
        }

        public static boolean isNotTransient(Field field) {
            return !Modifier.isTransient(field.getModifiers());
        }

        public static boolean isNotStatic(Field field) {
            return !Modifier.isStatic(field.getModifiers());
        }

        public static boolean isNotSynthetic(Field field) {
            return !field.isSynthetic();
        }
    }
}
