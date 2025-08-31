package project.ii.flowx.module.message.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.module.message.dto.member.MemberCreateRequest;
import project.ii.flowx.module.message.dto.member.MemberUpdateRequest;
import project.ii.flowx.module.message.dto.member.MemberResponse;
import project.ii.flowx.module.message.entity.ConversationMember;
import project.ii.flowx.module.message.repository.MemberRepository;
import project.ii.flowx.module.message.mapper.MemberMapper;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberService {
    MemberRepository memberRepository;
    MemberMapper memberMapper;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        ConversationMember conversationMember = memberMapper.toMember(request);
        conversationMember.setJoinedAt(LocalDateTime.now());
        conversationMember = memberRepository.save(conversationMember);
        return memberMapper.toMemberResponse(conversationMember);
    }

    @Transactional
    public MemberResponse updateMember(UUID id, MemberUpdateRequest request) {
        ConversationMember conversationMember = memberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "ConversationMember not found"));
        memberMapper.updateMemberFromRequest(conversationMember, request);
        conversationMember = memberRepository.save(conversationMember);
        return memberMapper.toMemberResponse(conversationMember);
    }

    @Transactional
    public void deleteMember(UUID id) {
        ConversationMember conversationMember = memberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "ConversationMember not found"));
        memberRepository.delete(conversationMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(UUID id) {
        ConversationMember conversationMember = memberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "ConversationMember not found"));
        return memberMapper.toMemberResponse(conversationMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByConversationId(UUID conversationId) {
        List<ConversationMember> conversationMembers = memberRepository.findByConversationId(conversationId);
        if (conversationMembers.isEmpty()) {
            throw new FlowXException(FlowXError.NOT_FOUND, "No conversationMembers found for this conversation");
        }
        return memberMapper.toMemberResponseList(conversationMembers);
    }

    @Transactional
    public void updateReadAt(UUID conversationId, UUID userId, LocalDateTime readAt) {
        ConversationMember conversationMember = memberRepository.findAll().stream()
            .filter(m -> m.getConversationId().equals(conversationId) && m.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "ConversationMember not found"));
        conversationMember.setReadAt(readAt);
        memberRepository.save(conversationMember);
    }
}