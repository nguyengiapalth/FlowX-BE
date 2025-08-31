package project.ii.flowx.module.message.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.message.service.MemberService;
import project.ii.flowx.module.message.dto.member.MemberCreateRequest;
import project.ii.flowx.module.message.dto.member.MemberResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberController {
    MemberService memberService;

    @PostMapping
    public Response<MemberResponse> create(@RequestBody MemberCreateRequest request) {
        MemberResponse response = memberService.createMember(request);
        return Response.<MemberResponse>builder()
                .code(201)
                .message("ConversationMember created successfully")
                .data(response)
                .build();
    }

    @PostMapping("/{conversationId}/add")
    public Response<MemberResponse> joinConversation(
            @PathVariable UUID conversationId,
            @RequestBody MemberCreateRequest request) {
        MemberResponse response = memberService.joinConversation(conversationId, request);
        return Response.<MemberResponse>builder()
                .code(200)
                .message("Joined conversation successfully")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable UUID id) {
        memberService.deleteMember(id);
        return Response.<Void>builder()
                .code(204)
                .message("ConversationMember deleted successfully")
                .build();
    }



    @GetMapping("/conversations/{conversationId}")
    public Response<List<MemberResponse>> getAll(@PathVariable UUID conversationId) {
        List<MemberResponse> responses = memberService.getMembersByConversationId(conversationId);
        return Response.<List<MemberResponse>>builder()
                .code(200)
                .message("Members retrieved successfully")
                .data(responses)
                .build();
    }
} 