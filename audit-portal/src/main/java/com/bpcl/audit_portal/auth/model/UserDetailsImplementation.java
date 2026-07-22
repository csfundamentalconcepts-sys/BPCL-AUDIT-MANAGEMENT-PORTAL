package com.bpcl.audit_portal.auth.model;

import com.bpcl.audit_portal.common.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Data
public class UserDetailsImplementation implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    @JsonIgnore
    private String password;

    /**
     * Stores:
     * ROLE_ADMIN
     * ROLE_MANAGER
     * APP_1
     * APP_2
     */
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImplementation(
            Long id,
            String userName,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImplementation build(User user) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().getRoleName().name()
                )
        );

//        if (user.getApplications() != null) {
//
//            authorities.addAll(
//                    user.getApplications()
//                            .stream()
//                            .map(application ->
//                                    new SimpleGrantedAuthority(
//                                            "APP_" + application.getId()
//                                    )
//                            )
//                            .toList()
//            );
//        }

        return new UserDetailsImplementation(
                user.getId(),
                user.getUserName(),
                user.getPassword(),
                authorities
        );
    }

    public boolean hasApplicationAccess(Long applicationId) {

        String authority = "APP_" + applicationId;

        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof UserDetailsImplementation that)) {
            return false;
        }

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}