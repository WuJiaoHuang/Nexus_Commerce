package org.example.com.userservice.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "user")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
}
