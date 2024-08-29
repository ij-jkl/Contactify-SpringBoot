package com.isaac.contactify.services;

import com.isaac.contactify.domain.Contact;
import com.isaac.contactify.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// Service annotation makes it so Spring detects it as a component scanning, allowing us to use it for dependency injection
@Service
// Logger
@Slf4j
// If the transaction isn't completed successfully it's going to be rolled back, helps to mantain data integrity
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {

    private ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page,int size) {
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id){
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact Not Found. "));
    }

    public Contact createContact(Contact contact){
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact){
        contactRepository.delete(contact);
    }
}
