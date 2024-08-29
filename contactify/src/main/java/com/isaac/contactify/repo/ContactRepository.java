package com.isaac.contactify.repo;

import com.isaac.contactify.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// The string passed is the Primary Key
public interface ContactRepository extends JpaRepository <Contact, String>{

    Optional<Contact> findById(String id);
    // We could do a findByEmail too, it depends on what you want to look for

}

