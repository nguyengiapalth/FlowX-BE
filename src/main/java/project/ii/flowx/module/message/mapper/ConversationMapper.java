package project.ii.flowx.module.message.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import project.ii.flowx.module.message.dto.conversation.ConversationCreateRequest;
import project.ii.flowx.module.message.dto.conversation.ConversationUpdateRequest;
import project.ii.flowx.module.message.dto.conversation.ConversationResponse;
import project.ii.flowx.module.message.entity.Conversation;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);
    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
    Conversation toConversation(ConversationCreateRequest request);
    void updateConversationFromRequest(@MappingTarget Conversation conversation, ConversationUpdateRequest request);
} 