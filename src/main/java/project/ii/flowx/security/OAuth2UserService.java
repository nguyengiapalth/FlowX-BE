package project.ii.flowx.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Extract email from OAuth2 user info
        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user already exists
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Only allow login for existing users, reject if user doesn't exist
        if (userOptional.isEmpty()) {
            log.warn("OAuth2 login attempt from non-registered user: {}", email);
            throw new OAuth2AuthenticationException("User not registered in the system");
        }

        // User exists, update user information
        User user = userOptional.get();
        user = updateExistingUser(user, attributes);

        // Create UserPrincipal with roles from UserRoleService
        UserPrincipal userPrincipal = UserPrincipal.create(user, userRoleService.getGlobalRolesForUser(user.getId()));
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    private User updateExistingUser(User existingUser, Map<String, Object> attributes) {
        // Update user information if needed
        existingUser.setFullName((String) attributes.get("name"));
        existingUser.setAvatar((String) attributes.get("picture"));

        return userRepository.save(existingUser);
    }
}