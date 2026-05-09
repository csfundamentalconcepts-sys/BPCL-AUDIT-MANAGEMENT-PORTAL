package com.bpcl.audit_portal.auth.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.bpcl.audit_portal.common.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NoArgsConstructor
@Data
public class UserDetailsImplementation implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImplementation(Long id, String username, String password,
                                     Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userName = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImplementation build(User user) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.addAll(
                user.getPermissions().stream()
                        .map(p -> new SimpleGrantedAuthority(p.getName().name()))
                        .toList()
        );

        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name())
        );

        return new UserDetailsImplementation(
                user.getId(),
                user.getUserName(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImplementation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}