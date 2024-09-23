package io.florianlopes.spring.test.web.servlet.request;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TestFixtures {

    private TestFixtures() {
    }

    static AddUserForm aCompleteAddUserForm() {
        return AddUserForm.builder()
                .firstName("John").name("Doe")
                .gender(AddUserForm.Gender.MALE)
                .identificationNumber(BigDecimal.ONE)
                .identificationNumberBigInt(BigInteger.ONE)
                .metadatas(Map.of("firstName", "John", "name", "Doe"))
                .birthDate(LocalDate.of(2016, 8, 29))
                .currentAddress(anAddress().withLinkedAddress(aLinkedAddress()))
                .usernames(List.of("john.doe", "jdoe"))
                .usernamesArray(new String[]{"john.doe", "jdoe"})
                .formerAddresses(new AddUserForm.Address[]{
                        new AddUserForm.Address(10, "Street", 5222, "Chicago"),
                        new AddUserForm.Address(20, "Street", 5222, "Washington")
                })
                .diplomas(List.of(
                        new AddUserForm.Diploma("License", LocalDate.of(2021, 9, 4)),
                        new AddUserForm.Diploma("MSC", LocalDate.of(2024, 9, 4))
                ))
                .build();
    }

    static AddUserForm.Address aLinkedAddress() {
        return new AddUserForm.Address(42, "Linked Street", 8888, "Linked New York");
    }

    static AddUserForm.Address anAddress() {
        return new AddUserForm.Address(1, "Street", 5222, "New York");
    }
}
