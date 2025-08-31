package project.ii.flowx.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    @Builder.Default
    int code = 200;
    String message;
    T data;

    public Response<T> success(T data) {
        return Response.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public Response<T> success(T data, String message) {
        return Response.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public Response<T> error(int code, String message) {
        return Response.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public Response<T> error(String message) {
        return Response.<T>builder()
                .code(500)
                .message(message)
                .build();
    }



}

