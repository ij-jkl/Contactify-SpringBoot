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
        log.info("Fetching All the Contacts with page {} and size {}", page, size);
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));

    }

    public Contact getContact(String id){
        log.info("Fetching contact with ID : {}", id);
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact Not Found. "));
    }

    public Contact createContact(Contact contact){
        log.info("Creating contact : {}", contact);
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact){
        log.info("Deleting contact : {}",contact);
        contactRepository.delete(contact);
        log.info("Deleted contact : {}", contact);
    }

    // We upload the Photo for the contact with the ID passed. We update his image url, we save the contact and we return the new Url
    public String uploadPhoto(String id, MultipartFile file){
        log.info("Uploading photo for contact with ID : {}", id);
        Contact contact = getContact(id);
        // We are passing the ID of the contact and the File which is the image
        String photoUrl = photoFunction.apply(id, file);
        contact.setImageUrl(photoUrl);
        contactRepository.save(contact);
        log.info("Uploaded photo and updated contact imageUrl to : {}", photoUrl);

        return photoUrl;
    }

    // This function takes a String and returns a String
    // We are checking if the File passed as a dot, if it doesn't it cant be a .png file. If its has a dot we are going to map it

    private final Function<String, String> fileExtension = filename -> Optional.of(filename).filter(name -> name.contains("."))
            .map(name -> "." + name.substring(filename.lastIndexOf(".")  + 1 )).orElse(".png");

    // This function takes a String and a MultiFile (Image in this case ) and returns a String


    private final BiFunction<String, MultipartFile, String> photoFunction = (id,image) -> {
        String fileName = id + fileExtension.apply(image.getOriginalFilename());
        try{
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();

            // We are saving the Pictures for now in Downloads/uploads, if the user doesn't have an uploads folder, this line will create it
            if(!Files.exists(fileStorageLocation)) {Files.createDirectories(fileStorageLocation);}

            // We copy the image, we pass the Id + the file (image) and we apply the original name, so if it exists we replace it

            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName),REPLACE_EXISTING);

            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/contacts/image/" + id + fileName).toUriString();
        } catch (Exception e){
            throw new RuntimeException("Unable to save the Image");
        }
    };
}
