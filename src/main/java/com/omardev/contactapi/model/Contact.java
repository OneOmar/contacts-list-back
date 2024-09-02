package com.omardev.contactapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents a contact entity with personal and contact information.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_seq")
    @SequenceGenerator(name = "contact_seq", sequenceName = "contact_sequence", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    private String title;

    @Pattern(regexp = "^$|\\+?[0-9]{10,15}", message = "Phone number must be between 10 and 15 digits and can optionally start with a '+'")
    private String phone;

    @Column(unique = true)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Size(max = 255, message = "Photo URL must be at most 255 characters")
    private String photoUrl;

    /**
     * Enumeration representing the status of a contact.
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
