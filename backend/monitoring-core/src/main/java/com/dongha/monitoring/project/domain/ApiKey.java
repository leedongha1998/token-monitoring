package com.dongha.monitoring.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Entity
@Table(name = "api_key")
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "key_hash", nullable = false, length = 64, unique = true)
  private String keyHash;

  @Column(nullable = false, length = 8)
  private String prefix;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ApiKey() {}

  private ApiKey(Project project, String keyHash, String prefix) {
    this.project = project;
    this.keyHash = keyHash;
    this.prefix = prefix;
    this.active = true;
    this.createdAt = Instant.now();
  }

  public static ApiKey create(Project project, String rawKey) {
    String prefix = rawKey.length() >= 8 ? rawKey.substring(0, 8) : rawKey;
    return new ApiKey(project, hashKey(rawKey), prefix);
  }

  public static String hashKey(String rawKey) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  public boolean matches(String rawKey) {
    return keyHash.equals(hashKey(rawKey));
  }

  public void deactivate() {
    this.active = false;
  }

  public Long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public String getKeyHash() {
    return keyHash;
  }

  public String getPrefix() {
    return prefix;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
