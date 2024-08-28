package io.florianlopes.spring.test.web.servlet.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Created by flopes on 15/04/2018.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
class AddUserForm {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String name;
    @NotNull
    private BigDecimal identificationNumber;
    @NotNull
    private BigInteger identificationNumberBigInt;
    @NotNull
    private Gender gender;
    @NotNull
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;
    @NotNull
    private Address currentAddress;
    @NotEmpty
    private List<String> usernames;
    @NotEmpty
    private String[] usernamesArray;
    @NotEmpty
    private List<Diploma> diplomas;
    @NotEmpty
    private Address[] formerAddresses;
    @NotEmpty
    private Map<String, String> metadatas;

    AddUserForm(String firstName, String name, LocalDate birthDate, Address currentAddress) {
        this.firstName = firstName;
        this.name = name;
        this.birthDate = birthDate;
        this.currentAddress = currentAddress;
    }

    enum Gender {
        MALE,
        FEMALE
    }

    @Data
    @NoArgsConstructor
    static class Diploma {
        @NotEmpty
        private String name;
        @DateTimeFormat(pattern = "dd/MM/yyyy")
        @NotNull
        private LocalDate date;

        Diploma(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }
    }

    @Data
    @NoArgsConstructor
    static class Address {
        @NotNull
        private int streetNumber;
        @NotEmpty
        private String streetName;
        @NotNull
        private int postalCode;
        @NotEmpty
        private String city;

        Address(int streetNumber, String streetName, int postalCode, String city) {
            this.streetNumber = streetNumber;
            this.streetName = streetName;
            this.postalCode = postalCode;
            this.city = city;
        }
    }
}
