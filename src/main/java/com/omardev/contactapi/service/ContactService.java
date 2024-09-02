package com.omardev.contactapi.service;

import com.omardev.contactapi.repository.ContactRepo;
import com.omardev.contactapi.model.Contact;
import com.omardev.contactapi.exception.ContactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service class for managing contacts and handling photo uploads.
 */
@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private static final String BASE_URL = "http://localhost:8080/api/contacts/";
    private static final String UPLOAD_DIR = "uploads/photos";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    private final ContactRepo contactRepo;

    @Autowired
    public ContactService(ContactRepo contactRepo) {
        this.contactRepo = contactRepo;
    }

    public Page<Contact> getAllContacts(int page, int size) {
        logger.info("Fetching all contacts - Page: {}, Size: {}", page, size);
        return contactRepo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContactById(Long contactId) {
        logger.info("Fetching contact with ID: {}", contactId);
        return contactRepo.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with ID: " + contactId));
    }

    public Contact addContact(Contact contact) {
        logger.info("Adding new contact: {}", contact);
        return contactRepo.save(contact);
    }

    public Contact updateContact(Long contactId, Contact contactUpdates) {
        logger.info("Updating contact with ID: {}", contactId);

        Contact existingContact = getContactById(contactId);
        updateContactFields(existingContact, contactUpdates);

        return contactRepo.save(existingContact);
    }

    private void updateContactFields(Contact existingContact, Contact contactUpdates) {
        if (contactUpdates.getName() != null) {
            existingContact.setName(contactUpdates.getName());
        }
        if (contactUpdates.getTitle() != null) {
            existingContact.setTitle(contactUpdates.getTitle());
        }
        if (contactUpdates.getPhone() != null) {
            existingContact.setPhone(contactUpdates.getPhone());
        }
        if (contactUpdates.getEmail() != null) {
            existingContact.setEmail(contactUpdates.getEmail());
        }
        if (contactUpdates.getAddress() != null) {
            existingContact.setAddress(contactUpdates.getAddress());
        }
        if (contactUpdates.getStatus() != null) {
            existingContact.setStatus(contactUpdates.getStatus());
        }
        if (contactUpdates.getPhotoUrl() != null) {
            existingContact.setPhotoUrl(contactUpdates.getPhotoUrl());
        }
    }

    public void deleteContact(Long id) {
        logger.info("Deleting contact with ID: {}", id);
        contactRepo.deleteById(id);
    }

    public Contact uploadPhoto(Long contactId, MultipartFile photo) {
        Contact contact = getContactById(contactId);
        validatePhoto(photo);

        String fileName = generateUniqueFileName(contact.getName(), photo);
        Path filePath = createFilePath(fileName);
        saveFile(photo, filePath);

        String photoUrl = BASE_URL + filePath.toString().replace("\\", "/");;
        contact.setPhotoUrl(photoUrl);

        return contactRepo.save(contact);
    }

    private void validatePhoto(MultipartFile photo) {
        if (photo.isEmpty()) {
            throw new IllegalArgumentException("Photo file is empty!");
        }
        if (photo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Photo file size exceeds the maximum allowed size of 2 MB!");
        }
        String extension = getFileExtension(photo).toLowerCase();
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("Invalid file extension! Only JPG, JPEG, and PNG files are allowed.");
        }
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(MultipartFile photo) {
        String originalFileName = photo.getOriginalFilename();
        if (originalFileName == null || originalFileName.lastIndexOf('.') == -1) {
            throw new IllegalArgumentException("Invalid file extension!");
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    private String generateUniqueFileName(String contactName, MultipartFile photo) {
        String formattedName = contactName.replaceAll("[^a-zA-Z0-9]", "_");
        String extension = getFileExtension(photo);
        return String.format("%s_photo_%s%s", formattedName, UUID.randomUUID(), extension);
    }

    private Path createFilePath(String fileName) {
        return Paths.get(UPLOAD_DIR, fileName);
    }

    private void saveFile(MultipartFile file, Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            logger.error("Failed to save photo file", e);
            throw new RuntimeException("Failed to save photo file", e);
        }
    }

    public Resource getPhoto(String fileName) {
        try {
            // Construct the file path
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            // Check if the file exists
            if (Files.exists(filePath)) {
                // Return the file as a resource
                return new UrlResource(filePath.toUri());
            } else {
                // Handle the case where the file does not exist
                throw new IOException("File not found: " + fileName);
            }
        } catch (IOException e) {
            logger.error("Failed to load photo ", e);
            throw new RuntimeException("Failed to load photo ", e);
        }
    }

//    public Resource getPhotoByContactId(Long contactId) {
//        Contact contact = getContactById(contactId);
//        String photoUrl = contact.getPhotoUrl();
//
//        if (photoUrl == null || photoUrl.isEmpty()) {
//            throw new RuntimeException("No photo available for contact ID: " + contactId);
//        }
//
//        // Extract the file name from the photo URL
//        String fileName = Paths.get(photoUrl).getFileName().toString();
//        return getPhoto(fileName);
//    }

    public Resource getPhotoByContactId(Long contactId) {
        logger.info("Retrieving photo for contact ID: {}", contactId);

        Contact contact = getContactById(contactId);
        String photoUrl = contact.getPhotoUrl();

        if (photoUrl == null || photoUrl.isEmpty()) {
            throw new RuntimeException("No photo available for contact ID: " + contactId);
        }

        // Extract the file name from the photo URL
        // String fileName = Paths.get(photoUrl).getFileName().toString();
        String fileName = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
        logger.info("Photo file name extracted: {}", fileName);

        Resource photo = getPhoto(fileName);
        logger.info("Photo successfully retrieved for contact ID: {}", contactId);

        return photo;
    }

}
