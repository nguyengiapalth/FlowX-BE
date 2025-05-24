package project.ii.flowx.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.entity.UserRole;
import project.ii.flowx.model.repository.RoleRepository;
import project.ii.flowx.model.repository.UserRepository;
import project.ii.flowx.model.repository.UserRoleRepository;
import project.ii.flowx.shared.enums.RoleDefault;
import project.ii.flowx.shared.enums.RoleScope;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInit {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_EMAIL = "nguyengiapnf5@gmail.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (roleRepository.findAll().isEmpty()) {
                log.info("Creating default roles...");
                roleRepository.saveAllAndFlush(List.of(
                        Role.builder().name(String.valueOf(RoleDefault.MANAGER)).build(),
                        Role.builder().name(String.valueOf(RoleDefault.HR)).build(),
                        Role.builder().name(String.valueOf(RoleDefault.MEMBER)).build(),
                        Role.builder().name(String.valueOf(RoleDefault.USER)).build()
                ));
                log.info("Default roles created");
            }
            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                log.warn("admin user has been created with default password: admin, please change it");
                User adminUser = User.builder()
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .fullName("Nguyen Khac Giap")
                        .phoneNumber("0987654321")
                        .dateOfBirth(LocalDate.parse("2004-05-19"))
                        .address("Ha Noi")
                        .build();
                userRepository.save(adminUser);

                userRoleRepository.save(UserRole.builder()
                        .user(adminUser)
                        .role(roleRepository.findByName(String.valueOf(RoleDefault.HR))
                                .orElseThrow(() -> new RuntimeException("Role not found")))
                        .scope(RoleScope.GLOBAL)
                        .scopeId(0L)
                        .build());

                userRoleRepository.save(UserRole.builder()
                        .user(adminUser)
                        .role(roleRepository.findByName(String.valueOf(RoleDefault.MANAGER))
                                .orElseThrow(() -> new RuntimeException("Role not found")))
                        .scope(RoleScope.GLOBAL)
                        .scopeId(0L)
                        .build());

                userRoleRepository.save(UserRole.builder()
                        .user(adminUser)
                        .role(roleRepository.findByName(String.valueOf(RoleDefault.USER))
                                .orElseThrow(() -> new RuntimeException("Role not found")))
                        .scope(RoleScope.GLOBAL)
                        .scopeId(0L)
                        .build());

                log.info("Admin user created with email: {}", ADMIN_EMAIL);
                log.info("Admin user password: {}, please change it", ADMIN_PASSWORD);
            }
            log.info("Application initialization completed .....");
        };
    }
}
