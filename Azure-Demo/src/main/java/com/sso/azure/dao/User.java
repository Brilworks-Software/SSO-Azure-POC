package com.sso.azure.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Table(name = "Users")
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

}
