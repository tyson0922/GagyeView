package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kopo.gagyeview.validation.OnCheckUserId;
import kopo.gagyeview.validation.OnRegister;
import kopo.gagyeview.validation.OnSendEmail;
import kopo.gagyeview.validation.OnVerifyAuth;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoDTO(

        @NotBlank(message = "사용자 아이디는 필수 학목입니다.", groups = {OnCheckUserId.class, OnRegister.class})
        @Size(max = 10, message = "사용자 아이디는 10글자까지 입력가능합니다.", groups = {OnCheckUserId.class, OnRegister.class})
        String userId, // 사용자 아이디

        @NotBlank(message = "사용자 이름은 필수 학목입니다.", groups = OnRegister.class)
        @Size(max = 10, message = "사용자 이름은 10글자까지 입력가능합니다.")
        String userName,

        @NotBlank(message = "이메일은 필수 학목입니다.", groups = {
                OnSendEmail.class, OnRegister.class
        })
        @Email(message = "올바른 이메일 형식이어야 합니다.", groups = {OnSendEmail.class, OnRegister.class})
        String userEmail, // 사용자 이메일

        @NotBlank(message = "비밀번호는 필수 학목입니다.", groups = OnRegister.class)

        String userPw, // 사용자 비밀번호

        String regId, // 등록 아이디

        String regDt, // 등록일자

        String chgId, // 수정 아이디

        String chgDt, // 수정일자

        // 회원가입시, 중복가입을 방지 위해 사용할 변수
        // DB를 조회해서 회원이 존재하면 Y값을 반환
        // DB 테이블에 존재하지 않는 가상의 컬럼(ALIAS)
        String existsYn,

        @NotBlank(message = "인증번호는 필수 학목입니다.", groups = OnVerifyAuth.class)
        String authNumber // 이메일 중복체크를 위한 인증번호
) {
}
