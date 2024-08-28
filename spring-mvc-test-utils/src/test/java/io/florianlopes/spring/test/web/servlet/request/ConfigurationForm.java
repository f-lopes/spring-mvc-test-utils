package io.florianlopes.spring.test.web.servlet.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationForm {

    private static final Comparator<ConfigurationForm> COMPARATOR_NAME = Comparator.comparing(ConfigurationForm::getName);
    private static String STATIC_NAME = "static name";

    private final String finalName = "finalValue";

    private String name;
    private transient String transientName;
    private Inner inner;

    @Data
    @AllArgsConstructor
    public static class Inner {
        private String value;
    }

}
