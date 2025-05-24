//package project.ii.flowx.applications.service.auth;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import project.ii.flowx.model.entity.InvalidToken;
//import project.ii.flowx.model.repository.InvalidTokenRepository;
//
//import java.util.List;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class InvalidTokenService {
//    InvalidTokenRepository invalidTokenRepository;
//
//    @Transactional
//    public void addInvalidToken(String token) {
//        if (!invalidTokenRepository.existsByToken(token)) {
//            InvalidToken invalidToken = new InvalidToken();
//            invalidToken.setToken(token);
//            invalidTokenRepository.save(invalidToken);
//            log.info("Token added to invalid tokens list");
//        } else {
//            log.info("Token already exists in invalid tokens list");
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isTokenInvalid(String token) {
//        return invalidTokenRepository.existsByToken(token);
//    }
//
//    @Transactional(readOnly = true)
//    public List<InvalidToken> getAllInvalidTokens() {
//        return invalidTokenRepository.findAll();
//    }
//
//    @Transactional
//    public void deleteInvalidToken(Long id) {
//        invalidTokenRepository.deleteById(id);
//        log.info("Token with ID {} removed from invalid tokens list", id);
//    }
//
//    @Transactional
//    public void clearAllInvalidTokens() {
//        invalidTokenRepository.deleteAll();
//        log.info("All tokens cleared from invalid tokens list");
//    }
//}