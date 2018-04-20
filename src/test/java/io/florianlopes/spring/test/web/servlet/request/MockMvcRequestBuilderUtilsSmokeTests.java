package io.florianlopes.spring.test.web.servlet.request;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by flopes on 15/04/2018.
 */
public class MockMvcRequestBuilderUtilsSmokeTests {

    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new UserController())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    public void fullTest() throws Exception {
        final AddUserForm addUserForm = getCompletedAddUserForm();

        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        addUserForm.setDiplomas(Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate)));

        this.mockMvc.perform(MockMvcRequestBuilderUtils.postForm("/users", addUserForm))
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
    }

    private AddUserForm getCompletedAddUserForm() {
        final LocalDate userBirthDate = LocalDate.of(2016, 8, 29);
        final Map<String, String> metadatas = new HashMap<>();
        metadatas.put("firstName", "John");
        metadatas.put("name", "Doe");

        final LocalDate bachelorDate = LocalDate.now().minusYears(2);
        final LocalDate masterDate = LocalDate.now();
        final List<AddUserForm.Diploma> userDiplomas =
                Arrays.asList(new AddUserForm.Diploma("License", bachelorDate), new AddUserForm.Diploma("MSC", masterDate));

        return AddUserForm.builder()
                .firstName("John").name("Doe")
                .usernames(Arrays.asList("john.doe", "jdoe"))
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .gender(AddUserForm.Gender.MALE)
                .identificationNumber(BigDecimal.ONE)
                .identificationNumberBigInt(BigInteger.ONE)
                .currentAddress(new AddUserForm.Address(1, "Street", 5222, "New York"))
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .birthDate(userBirthDate)
                .diplomas(userDiplomas)
                .metadatas(metadatas)
                .build();
    }

    @Controller
    private class UserController {

        @RequestMapping(value = "/users", method = RequestMethod.POST)
        public String addUser(@Valid AddUserForm addUserForm, BindingResult bindingResult) {
            return StringUtils.EMPTY;
        }
    }
}
