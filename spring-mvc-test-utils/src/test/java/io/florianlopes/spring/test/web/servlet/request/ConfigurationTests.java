package io.florianlopes.spring.test.web.servlet.request;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTests {

    @Test
    void throwsIllegalArgumentExceptionWhenFieldPredicateIsNull() {
        assertThrows(NullPointerException.class, () -> Configuration.builder()
                .fieldPredicate(null));
    }

    @Test
    void customPredicate() throws NoSuchFieldException {
        final Configuration config = Configuration.builder()
                .fieldPredicate(field -> "simpleField".equals(field.getName()))
                .build();

        final Field simpleField = TestClass.class.getDeclaredField("simpleField");

        assertTrue(config.fieldPredicate().test(simpleField));
    }

    @Test
    void registersPropertyEditor() {
        final PropertyEditorSupport propertyEditor = new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                return "textValue";
            }
        };

        final Configuration config = Configuration.builder()
                .withPropertyEditor(propertyEditor, BigInteger.class)
                .build();

        assertThat(config.propertyEditorFor(BigInteger.class)).isEqualTo(propertyEditor);
    }

    @Test
    void returnsWhetherPropertyEditorHasBeenRegistered() {
        final PropertyEditorSupport propertyEditor = new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                return "textValue";
            }
        };

        final Configuration config = Configuration.builder()
                .withPropertyEditor(propertyEditor, BigInteger.class)
                .build();

        assertTrue(config.hasPropertyEditorFor(BigInteger.class));
        assertFalse(config.hasPropertyEditorFor(BigDecimal.class));
    }

    @Test
    void registerNullPropertyEditorThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Configuration.builder()
                .withPropertyEditor(null, BigInteger.class));
    }

    @Test
    void registersMultiplePropertyEditor() {
        final PropertyEditorSupport propertyEditorForBigInteger = new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                return "bigIntegerTextValue";
            }
        };

        final PropertyEditorSupport propertyEditorForBigDecimal = new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                return "bigDecimalTextValue";
            }
        };
        final Configuration config = Configuration.builder()
                .withPropertyEditor(propertyEditorForBigInteger, BigInteger.class)
                .withPropertyEditor(propertyEditorForBigDecimal, BigDecimal.class)
                .build();

        assertThat(config.propertyEditorFor(BigInteger.class))
                .isEqualTo(propertyEditorForBigInteger);
        assertThat(config.propertyEditorFor(BigDecimal.class))
                .isEqualTo(propertyEditorForBigDecimal);
    }

    static class TestClass {

        private static String STATIC_FIELD = "staticFieldValue";

        private final String finalField = "finalFieldValue";
        private transient String transientField = "transientFieldValue";

        private String simpleField = "simpleFieldValue";

    }

    @Nested
    class DefaultConfiguration {

        private final Configuration defaultConfiguration = Configuration.DEFAULT;

        @Test
        void includesFinalFields() throws NoSuchFieldException {
            final Field finalField = TestClass.class.getDeclaredField("finalField");

            assertTrue(defaultConfiguration.fieldPredicate().test(finalField));
        }

        @Test
        void excludesTransientFields() throws NoSuchFieldException {
            final Field transientField = TestClass.class.getDeclaredField("transientField");

            assertFalse(defaultConfiguration.fieldPredicate().test(transientField));
        }

        @Test
        void excludesStaticFields() throws NoSuchFieldException {
            final Field staticField = TestClass.class.getDeclaredField("STATIC_FIELD");

            assertFalse(defaultConfiguration.fieldPredicate().test(staticField));
        }
    }

    @Nested
    class ExcludeFinalConfiguration {

        private final Configuration excludeFinalConfiguration = Configuration.EXCLUDE_FINAL;

        @Test
        void excludesFinalFields() throws NoSuchFieldException {
            final Field finalField = TestClass.class.getDeclaredField("finalField");

            assertFalse(excludeFinalConfiguration.fieldPredicate().test(finalField));
        }

        @Test
        void excludesTransientFields() throws NoSuchFieldException {
            final Field transientField = TestClass.class.getDeclaredField("transientField");

            assertFalse(excludeFinalConfiguration.fieldPredicate().test(transientField));
        }
    }

    @Nested
    class IncludeTransientConfiguration {

        private final Configuration includeTransientConfiguration = Configuration.INCLUDE_TRANSIENT;

        @Test
        void includesTransientFields() throws NoSuchFieldException {
            final Field transientField = TestClass.class.getDeclaredField("transientField");

            assertTrue(includeTransientConfiguration.fieldPredicate().test(transientField));
        }

        @Test
        void excludesFinalFields() throws NoSuchFieldException {
            final Field finalField = TestClass.class.getDeclaredField("finalField");

            assertFalse(includeTransientConfiguration.fieldPredicate().test(finalField));
        }
    }

    @Nested
    class IncludeStaticConfiguration {

        private final Configuration includeStaticConfiguration = Configuration.INCLUDE_STATIC;

        @Test
        void includesStaticFields() throws NoSuchFieldException {
            final Field staticField = TestClass.class.getDeclaredField("STATIC_FIELD");

            assertTrue(includeStaticConfiguration.fieldPredicate().test(staticField));
        }

        @Test
        void excludesFinalFields() throws NoSuchFieldException {
            final Field finalField = TestClass.class.getDeclaredField("finalField");

            assertFalse(includeStaticConfiguration.fieldPredicate().test(finalField));
        }
    }
}
