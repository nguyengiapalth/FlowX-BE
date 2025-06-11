package project.ii.flowx.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.UserRepository;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        // Load user roles and authorities
        List<UserRoleResponse> roles = userRoleService.getGlobalRolesForUser(user.getId());
        log.info("Found roles for user {}", user.getEmail());
        return UserPrincipal.create(user, roles);
    }
}
