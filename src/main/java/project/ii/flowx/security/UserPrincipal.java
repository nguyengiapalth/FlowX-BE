package project.ii.flowx.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserPrincipal is a custom implementation of UserDetails that represents the authenticated user.
 * It contains user information such as ID, email, password, and authorities (roles).
 * This class is used by Spring Security to manage user authentication and authorization.
 */
@Slf4j
public class UserPrincipal implements UserDetails {
    @Getter
    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    @Setter
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    public static UserPrincipal create(User user, List<UserRoleResponse> roles) {
        List<GrantedAuthority> authorities;
        if (roles == null || roles.isEmpty()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        else {
            authorities = roles.stream()
                .map(userRole ->
                        new SimpleGrantedAuthority(
                                "ROLE_" +
                                        userRole.getRole().getName().toUpperCase()))
                .collect(Collectors.toList());
        }

        // Log the roles for debugging
        assert roles != null;
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
