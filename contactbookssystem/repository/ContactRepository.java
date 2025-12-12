package com.example.contactbookssystem.repository;

import com.example.contactbookssystem.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByBookmarkedTrue();
    List<Contact> findByNameContainingIgnoreCase(String keyword);
}