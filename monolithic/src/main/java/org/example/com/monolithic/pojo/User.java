package org.example.com.monolithic.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "users")
@Entity
@Data
public class User {
    @Id
    private String id;
    private String username;
    private String password;
}
