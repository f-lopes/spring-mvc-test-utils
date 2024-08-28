package io.florianlopes.spring.test.web.servlet.request;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Configuration class that allows the exclusion of specific fields.
 */
public class Configuration {

    public static final Configuration DEFAULT = new Builder()
            .includeFinal(true)
            .includeTransient(false)
            .includeStatic(false)
            .build();
    public static final Configuration EXCLUDE_FINAL = new Builder()
            .includeFinal(false)
            .includeTransient(true)
            .build();
    public static final Configuration INCLUDE_TRANSIENT = new Builder()
            .includeFinal(false)
            .includeTransient(true)
            .build();
    public static final Configuration INCLUDE_STATIC = new Builder()
            .includeFinal(false)
            .includeStatic(true)
            .build();

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

    public Predicate<Field> fieldPredicate() {
        return fieldPredicate;
    }

    public static class Builder {

        private static final Predicate<Field> BASE_PREDICATE = Builder::isNotSynthetic;

        private Predicate<Field> fieldPredicate;
        private boolean includeFinal = true;
        private boolean includeTransient = false;
        private boolean includeStatic = false;

        private Builder() {
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

    }

}
