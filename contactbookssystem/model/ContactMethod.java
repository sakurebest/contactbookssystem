package com.example.contactbookssystem.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "contact_methods")
public class ContactMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMethodType type;

    @Column(nullable = false)
    private String value;

    @Column
    private String label; // 例如：工作电话、家庭电话等

    @Column(name = "is_primary")
    private boolean primary = false;
}