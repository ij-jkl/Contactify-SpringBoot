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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.isaac.contactify.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        log.info("Fetching All the Contacts with page {} and size {}", page, size);
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        log.info("Fetching contact with ID : {}", id);
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact Not Found."));
    }

    public Contact createContact(Contact contact) {
        log.info("Creating contact : {}", contact);
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {
        log.info("Deleting contact : {}", contact);
        contactRepository.delete(contact);
        log.info("Deleted contact : {}", contact);
    }

    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Uploading photo for contact with ID : {}", id);
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);
        contact.setImageUrl(photoUrl);
        contactRepository.save(contact);
        log.info("Uploaded photo and updated contact imageUrl to : {}", photoUrl);
        return photoUrl;
    }

    private final Function<String, String> fileExtension = filename -> Optional.of(filename)
            .filter(name -> name.contains("."))
            .map(name -> "." + name.substring(filename.lastIndexOf(".") + 1))
            .orElse(".png");

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String fileName = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) { Files.createDirectories(fileStorageLocation); }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName), REPLACE_EXISTING);
            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/contacts/image/" + id + fileName).toUriString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save the Image");
        }
    };
}