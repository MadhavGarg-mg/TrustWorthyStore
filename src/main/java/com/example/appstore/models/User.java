package com.example.appstore.models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")  // Optional: explicitly define the table name.
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    
    // Assume the role is stored as a string like "ROLE_USER", "ROLE_ADMIN", etc.
    private String role;

    public User() {}

    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { 
        return id; 
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    @Override
    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getRole() { 
        return role; 
    }

    public void setRole(String role) { 
        this.role = role; 
    }

    // Implementation of UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Wrap the role string into a SimpleGrantedAuthority.
        // Ensure your role is stored with the "ROLE_" prefix (e.g., "ROLE_USER").
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        // In this case, we use email as the username.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Return true if the account is not expired.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Return true if the account is not locked.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Return true if the credentials (password) are not expired.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Return true if the user is enabled.
        return true;
    }
}
