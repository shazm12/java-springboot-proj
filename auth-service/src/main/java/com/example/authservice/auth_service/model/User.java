package com.example.authservice.auth_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String role;

  public String getRole() {
    return this.role;
  }

  public void setRole(String value) {
    this.role = value;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String value) {
    this.password = value;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  public UUID getId() {
    return this.id;
  }

  public void setId(UUID value) {
    this.id = value;
  }
}
