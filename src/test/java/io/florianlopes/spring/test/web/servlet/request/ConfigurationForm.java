package io.florianlopes.spring.test.web.servlet.request;

import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationForm {

    public static final Comparator<ConfigurationForm> COMPARATOR_NAME = Comparator.comparing(ConfigurationForm::getName);
    public static final String STATIC_NAME = "static name";

    private String name;
    private transient String transientName;
    private final String finalName = "finalValue";
    
    private Inner inner;

    @Data
    @AllArgsConstructor
    public class Inner {
        private String value;
    }

}
