package com.omardev.contactapi.controller;

import com.omardev.contactapi.exception.ContactNotFoundException;
import com.omardev.contactapi.exception.FileStorageException;
import com.omardev.contactapi.model.Contact;
import com.omardev.contactapi.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

/**
 * Controller for handling contact-related requests.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Retrieves a paginated list of contacts.
     *
     * @param page Page number, default is 0
     * @param size Number of contacts per page, default is 12
     * @return A page of contacts
     */
    @GetMapping
    public ResponseEntity<Page<Contact>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Contact> contacts = contactService.getAllContacts(page, size);
        return ResponseEntity.ok(contacts);
    }

    /**
     * Retrieves a contact by its ID.
     *
     * @param id Contact ID
     * @return The contact with the specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        return ResponseEntity.ok(contact);
    }

    /**
     * Adds a new contact.
     *
     * @param contact The contact to add
     * @return The added contact with the location header
     */
    @PostMapping
    public ResponseEntity<Contact> addContact(@Valid @RequestBody Contact contact) {
        Contact newContact = contactService.addContact(contact);
        URI location = URI.create("/api/contacts/" + newContact.getId());
        return ResponseEntity.created(location).body(newContact);
    }

    /**
     * Updates an existing contact.
     *
     * @param id             Contact ID
     * @param contactUpdates The contact details to update
     * @return The updated contact
     * @throws ContactNotFoundException if the contact is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @Valid @RequestBody Contact contactUpdates) {
        Contact updatedContact = contactService.updateContact(id, contactUpdates);
        return ResponseEntity.ok(updatedContact);
    }

    /**
     * Deletes a contact by its ID.
     *
     * @param id Contact ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }


    private String getContentType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream"; // Default for unknown types
        }
    }

    /**
     * Retrieves a photo by its filename and returns it as a resource.
     *
     * @param fileName The name of the file to be retrieved
     * @return A ResponseEntity containing the photo resource and the appropriate HTTP headers
     */
    @GetMapping("/uploads/photos/{fileName:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String fileName) {
        try {
            Resource photo = contactService.getPhoto(fileName);
            String contentType = getContentType(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(photo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    /**
     * Retrieves a photo associated with a contact by the contact's ID.
     *
     * @param id the ID of the contact whose photo is to be retrieved
     * @return a ResponseEntity containing the photo resource and the appropriate HTTP headers
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getPhotoByContactId(@PathVariable Long id) {
        try {
            Resource photo = contactService.getPhotoByContactId(id);
            String contentType = getContentType(photo.getFilename());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(photo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Uploads a photo for a contact.
     *
     * @param id    Contact ID
     * @param photo The photo file
     * @return The updated contact
     */
    @PutMapping("/{id}/photo")
    public ResponseEntity<Contact> uploadPhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile photo) {
        Contact updatedContact = contactService.uploadPhoto(id, photo);
        return ResponseEntity.ok(updatedContact);
    }

}
