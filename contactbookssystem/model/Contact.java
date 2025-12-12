package com.example.contactbookssystem.model;

import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String company;

    @Column
    private String position;

    @Column
    private String notes;

    @Column(name = "is_bookmarked", nullable = false)
    private boolean bookmarked = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id")
    private List<ContactMethod> contactMethods = new ArrayList<>();

    public void addContactMethod(ContactMethod method) {
        this.contactMethods.add(method);
    }

    public void removeContactMethod(ContactMethod method) {
        this.contactMethods.remove(method);
    }
}