package io.florianlopes.spring.test.web.servlet.request;

import static io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils.form;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by flopes on 15/04/2018.
 */
public class MockMvcRequestBuilderUtilsSmokeTests {

    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new UserController())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        MockMvcRequestBuilderUtils.registerPropertyEditor(LocalDate.class, new CustomLocalDatePropertyEditor(DATE_FORMAT_PATTERN));
    }

    @Test
    public void fullTest() throws Exception {
        final AddUserForm addUserForm = getCompletedAddUserForm();

        this.mockMvc.perform(MockMvcRequestBuilderUtils.postForm("/users", addUserForm))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("users.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));

        this.mockMvc.perform(MockMvcRequestBuilderUtils.postForm("/users/create", addUserForm))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("create-user.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));

        this.mockMvc.perform(MockMvcRequestBuilderUtils.putForm("/users/edit", addUserForm))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("edit-user.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));
    }
    
    @Test
    public void withParamsFullTestGetMethod() throws Exception {
        final AddUserForm addUserForm = getCompletedAddUserForm();
        
        this.mockMvc.perform(get("/users").with(form(addUserForm)))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("users.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));
    }

    @Test
    public void withParamsFullTestPutMethod() throws Exception {
        final AddUserForm addUserForm = getCompletedAddUserForm();

        this.mockMvc.perform(put("/users").with(form(addUserForm)))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("users.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));
    }

    @Test
    public void withParamsFullTestPostMethod() throws Exception {
        final AddUserForm addUserForm = getCompletedAddUserForm();

        this.mockMvc.perform(post("/users").with(form(addUserForm)))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("users.html"))
                .andExpect(model().attribute("firstName", addUserForm.getFirstName()))
                .andExpect(model().attribute("name", addUserForm.getName()))
                .andExpect(model().attribute("myInt", addUserForm.getMyInt()));
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
                .myInt(10)
                .myLong(1_000L)
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
    @RequestMapping(value = "/users")
    private static class UserController {

        @RequestMapping(value = "")
        public ModelAndView addUser(@Valid AddUserForm addUserForm, BindingResult bindingResult) {
            return new ModelAndView("users.html", mapForm(addUserForm));
        }

        @PostMapping("/create")
        public ModelAndView createUser(@Valid AddUserForm addUserForm) {
            return new ModelAndView("create-user.html", mapForm(addUserForm));
        }

        @PutMapping("/edit")
        public ModelAndView editUser(@Valid AddUserForm addUserForm) {
            return new ModelAndView("edit-user.html", mapForm(addUserForm));
        }

        private Map<String,Object> mapForm(final AddUserForm addUserForm) {
            return Map.of(
                    "firstName", addUserForm.getFirstName(),
                    "name", addUserForm.getName(),
                    "myInt", addUserForm.getMyInt(),
                    "myLong", addUserForm.getMyLong(),
                    "identificationNumber", addUserForm.getIdentificationNumber()
            );
        }
    }
}
