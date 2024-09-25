package io.florianlopes.spring.test.web.servlet.request;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Created by flopes on 15/04/2018.
 */
public class MockMvcRequestBuilderUtilsSmokeTests {

    private static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    private static final String USERS_ENDPOINT = "/users";

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new UserController())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    public void fullTestPostMethod() throws Exception {
        final AddUserForm addUserForm = aCompleteAddUserForm();

        final Configuration config = Configuration.builder()
                .withPropertyEditor(new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN), LocalDate.class)
                .build();
        this.mockMvc.perform(MockMvcRequestBuilderUtils.postForm(USERS_ENDPOINT, addUserForm, config))
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
    }

    @Test
    public void fullTestPutMethod() throws Exception {
        final AddUserForm addUserForm = aCompleteAddUserForm();

        final Configuration config = Configuration.builder()
                .withPropertyEditor(new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN), LocalDate.class)
                .build();
        this.mockMvc.perform(MockMvcRequestBuilderUtils.putForm(USERS_ENDPOINT, addUserForm, config))
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
    }

    private AddUserForm aCompleteAddUserForm() {
        final List<String> usernames = List.of("john.doe", "jdoe");
        return AddUserForm.builder()
                .firstName("John").name("Doe")
                .usernames(usernames)
                .usernamesArray(usernames.toArray(new String[0]))
                .gender(AddUserForm.Gender.MALE)
                .identificationNumber(BigDecimal.ONE)
                .identificationNumberBigInt(BigInteger.ONE)
                .currentAddress(
                        new AddUserForm.Address(1, "Street", 5222, "New York")
                                .withLinkedAddress(
                                        new AddUserForm.Address(2, "Linked Street", 60, "New York")
                                )
                )
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Chicago Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Washington Street", 6222, "Washington")
                })
                .birthDate(LocalDate.of(2016, 8, 29))
                .diplomas(List.of(
                        new AddUserForm.Diploma("License", LocalDate.now().minusYears(2)),
                        new AddUserForm.Diploma("MSC", LocalDate.now())
                ))
                .diplomasMap(Map.of(
                        "License", new AddUserForm.Diploma("License", LocalDate.now().minusYears(2)),
                        "MSC", new AddUserForm.Diploma("MSC", LocalDate.now())
                ))
                .metadatas(Map.of("firstName", "John", "name", "Doe"))
                .build();
    }

    @Controller
    private static class UserController {

        @RequestMapping(value = USERS_ENDPOINT)
        public String addUser(@Valid AddUserForm addUserForm, BindingResult bindingResult) {
            return StringUtils.EMPTY;
        }
    }

    @Nested
    class PostProcessorTests {

        @Test
        public void withParamsFullTestGetMethod() throws Exception {
            final AddUserForm addUserForm = aCompleteAddUserForm();

            mockMvc.perform(get(USERS_ENDPOINT).with(MockMvcRequestBuilderUtils.form(addUserForm)))
                    .andExpect(MockMvcResultMatchers.model().hasNoErrors());
        }

        @Test
        public void withParamsFullTestPutMethod() throws Exception {
            final AddUserForm addUserForm = aCompleteAddUserForm();

            mockMvc.perform(put(USERS_ENDPOINT).with(MockMvcRequestBuilderUtils.form(addUserForm)))
                    .andExpect(MockMvcResultMatchers.model().hasNoErrors());
        }

        @Test
        public void withParamsFullTestPostMethod() throws Exception {
            final AddUserForm addUserForm = aCompleteAddUserForm();

            mockMvc.perform(post(USERS_ENDPOINT).with(MockMvcRequestBuilderUtils.form(addUserForm)))
                    .andExpect(MockMvcResultMatchers.model().hasNoErrors());
        }
    }
}
