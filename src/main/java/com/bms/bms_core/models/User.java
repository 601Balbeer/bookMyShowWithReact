package com.bms.bms_core.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;


@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User extends GlobalRecord {
    private String fullName;
    private String email;
    @JsonIgnore
    private String password;
    private String contactNumber;
}
