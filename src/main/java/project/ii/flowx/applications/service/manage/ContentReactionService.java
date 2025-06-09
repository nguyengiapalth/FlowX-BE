package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.content.ContentReactionRequest;
import project.ii.flowx.model.dto.content.ContentReactionResponse;
import project.ii.flowx.model.dto.content.ContentReactionSummary;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.ContentReaction;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.mapper.ContentReactionMapper;
import project.ii.flowx.model.repository.ContentReactionRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ReactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentReactionService {
    ContentReactionRepository contentReactionRepository;
    ContentReactionMapper contentReactionMapper;
    EntityLookupService entityLookupService;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ContentReactionResponse addOrUpdateReaction(ContentReactionRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        // Check if content exists
        Content content = entityLookupService.getContentById(request.getContentId());
        User user = entityLookupService.getUserById(userId);

        // Check if user already reacted to this content
        Optional<ContentReaction> existingReaction = contentReactionRepository.findByContentIdAndUserId(request.getContentId(), userId);

        ContentReaction reaction;
        if (existingReaction.isPresent()) {
            // Update existing reaction
            reaction = existingReaction.get();
            reaction.setReactionType(request.getReactionType());
            log.info("Updated reaction for user {} on content {} to {}", userId, request.getContentId(), request.getReactionType());
        } else {
            // Create new reaction
            reaction = contentReactionMapper.toContentReaction(request);
            reaction.setContent(content);
            reaction.setUser(user);
            log.info("Created new reaction for user {} on content {} with type {}", userId, request.getContentId(), request.getReactionType());
        }

        reaction = contentReactionRepository.save(reaction);
        return contentReactionMapper.toContentReactionResponse(reaction);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public void removeReaction(Long contentId) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        if (!contentReactionRepository.existsByContentIdAndUserId(contentId, userId)) {
            throw new FlowXException(FlowXError.NOT_FOUND, "Reaction not found");
        }

        contentReactionRepository.deleteByContentIdAndUserId(contentId, userId);
        log.info("Removed reaction for user {} on content {}", userId, contentId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<ContentReactionResponse> getReactionsByContent(Long contentId) {
        // Verify content exists
        entityLookupService.getContentById(contentId);
        
        List<ContentReaction> reactions = contentReactionRepository.findByContentIdOrderByCreatedAtDesc(contentId);
        return contentReactionMapper.toContentReactionResponseList(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<ContentReactionResponse> getReactionsByContentAndType(Long contentId, ReactionType reactionType) {
        // Verify content exists
        entityLookupService.getContentById(contentId);
        
        List<ContentReaction> reactions = contentReactionRepository.findByContentIdAndReactionTypeOrderByCreatedAtDesc(contentId, reactionType);
        return contentReactionMapper.toContentReactionResponseList(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ContentReactionSummary getReactionSummary(Long contentId) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        // Verify content exists
        entityLookupService.getContentById(contentId);

        // Get total reactions count
        Long totalReactions = contentReactionRepository.countByContentId(contentId);

        // Get reactions count by type
        List<Object[]> reactionCounts = contentReactionRepository.countReactionsByTypeAndContentId(contentId);
        Map<ReactionType, Long> reactionCountsMap = new HashMap<>();
        for (Object[] result : reactionCounts) {
            ReactionType type = (ReactionType) result[0];
            Long count = (Long) result[1];
            reactionCountsMap.put(type, count);
        }

        // Get current user's reaction
        Optional<ContentReaction> userReaction = contentReactionRepository.findByContentIdAndUserId(contentId, userId);
        ReactionType userReactionType = userReaction.map(ContentReaction::getReactionType).orElse(null);

        return new ContentReactionSummary(contentId, totalReactions, reactionCountsMap, userReactionType);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Optional<ContentReactionResponse> getUserReaction(Long contentId) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        Optional<ContentReaction> reaction = contentReactionRepository.findByContentIdAndUserId(contentId, userId);
        return reaction.map(contentReactionMapper::toContentReactionResponse);
    }
} 