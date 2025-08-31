package project.ii.flowx.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.auth.entity.UserRole;
import project.ii.flowx.module.auth.repository.RoleRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.module.auth.repository.UserRoleRepository;
import project.ii.flowx.applications.enums.RoleDefault;
import project.ii.flowx.applications.enums.RoleScope;

import java.time.LocalDate;
import java.util.List;

/**
 * Application initialization configuration.
 * This class initializes default roles and an admin user if they do not exist when the application starts.
 * It uses an ApplicationRunner to execute the initialization logic after the application context is loaded.
 */
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInit {
    PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:nguyengiapnf5@gmail.com}")
    @NonFinal
    String adminEmail;

    @Value("${app.admin.password:admin}")
    @NonFinal
    String adminPassword;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (roleRepository.findAll().isEmpty()) {
                log.info("Creating default roles...");
                roleRepository.saveAllAndFlush(List.of(
                        Role.builder().name(String.valueOf(RoleDefault.MANAGER)).build(),
                        Role.builder().name(String.valueOf(RoleDefault.MEMBER)).build(),
                        Role.builder().name(String.valueOf(RoleDefault.USER)).build()
                ));
                log.info("Default roles created");
            }
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                log.warn("Admin user has been created with default credentials. Please change them immediately!");
                User adminUser = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .fullName("Nguyen Khac Giap")
                        .phoneNumber("0987654321")
                        .dateOfBirth(LocalDate.parse("2004-05-19"))
                        .address("Ha Noi")
                        .build();
                userRepository.save(adminUser);

                userRoleRepository.save(UserRole.builder()
                        .userId(adminUser.getId())
                        .role(roleRepository.findByName(String.valueOf(RoleDefault.MANAGER))
                                .orElseThrow(() -> new RuntimeException("Role not found")))
                        .scope(RoleScope.GLOBAL)
                        .scopeId(null)
                        .build());

                userRoleRepository.save(UserRole.builder()
                        .userId(adminUser.getId())
                        .role(roleRepository.findByName(String.valueOf(RoleDefault.USER))
                                .orElseThrow(() -> new RuntimeException("Role not found")))
                        .scope(RoleScope.GLOBAL)
                        .scopeId(null)
                        .build());

                log.info("Admin user created with email: {}", adminEmail);
                if ("admin".equals(adminPassword)) {
                    log.warn("WARNING: Default admin password is being used! Please change it immediately for security!");
                }
            }
            log.info("Application initialization completed .....");
        };
    }
}
