package org.example.com.productservice.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "user")
@Entity
@Data
public class User {
    @Id
    private String id;
    private String username;
    private String password;
}
