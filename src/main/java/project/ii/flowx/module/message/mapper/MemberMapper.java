package project.ii.flowx.module.message.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import project.ii.flowx.module.message.dto.member.MemberCreateRequest;
import project.ii.flowx.module.message.dto.member.MemberUpdateRequest;
import project.ii.flowx.module.message.dto.member.MemberResponse;
import project.ii.flowx.module.message.entity.ConversationMember;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toMemberResponse(ConversationMember conversationMember);
    List<MemberResponse> toMemberResponseList(List<ConversationMember> conversationMembers);
    ConversationMember toMember(MemberCreateRequest request);
    void updateMemberFromRequest(@MappingTarget ConversationMember conversationMember, MemberUpdateRequest request);
} 