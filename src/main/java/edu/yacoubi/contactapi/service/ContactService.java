package edu.yacoubi.contactapi.service;

import edu.yacoubi.contactapi.model.Contact;
import edu.yacoubi.contactapi.repository.ContactRepository;
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

import static edu.yacoubi.contactapi.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SuppressWarnings("ALL")
@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(
                PageRequest.of(page, size, Sort.by("name"))
        );
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElseThrow(
                () -> {
                    log.error("Contact for user ID: {} not found", id);
                    return new RuntimeException("Contact not found");
                }
        );
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Saving picture for user ID: {}", id);
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoUrl(photoUrl);
        contactRepository.save(contact);

        return photoUrl;
    }

    // Function for getting file extension
   /* private Function<String, String> getFileExtension = fileName -> "." + fileName
            .substring("." + fileName.lastIndexOf(".") + 1);*/

    private final Function<String, String> fileExtension = fileName ->
            Optional.of(fileName)
                    .filter(name -> name.contains("."))
                    .map(name -> "." + name.substring(name.lastIndexOf(".") + 1))
                    .orElse(".png");


    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String fileName = id + fileExtension.apply(image.getOriginalFilename());
        try {
            // get the file storage location
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            // copy image to fileStorageLocation
            Files.copy(
                    image.getInputStream(),
                    fileStorageLocation.resolve(fileName),
                    REPLACE_EXISTING
            );

            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + fileName)
                    .toUriString();

        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image");
        }
    };
}














