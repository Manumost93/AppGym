package com.appgym.business.domain;

import com.appgym.common.dto.BusinessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BusinessType type;

    @Column
    private String description;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column
    private String address;

    @Column(name = "primary_color")
    private String primaryColor;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Business() {
    }

    public Business(UUID id, String name, BusinessType type, String description, String contactEmail,
                     String contactPhone, String address, String primaryColor) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.primaryColor = primaryColor;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BusinessType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getAddress() {
        return address;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public boolean isActive() {
        return active;
    }
}
