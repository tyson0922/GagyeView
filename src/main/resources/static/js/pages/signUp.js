// 아이디 중복체크여부( 중복 Y / 중복아님 : N)
let userIdCheck = "Y";

$(document).ready(function () {
    let form = document.getElementById("form");

    // 아이디 중복체크
    $('#btnUserId').on("click", function () {
        userIdExists(form);
    });

    // 이메일 중복체크
    $('#btnEmail').on("click", function () {
        emailExists(form);
    });

    // 회원가입
    $('#btnSend').on("click", function () {
        register(form);
    });
});

// 회원 아이디 중복 체크
function userIdExists(form) {
    if (form.userId.value === "") {
        showPresetToast("warning", "주의", "아이디를 입력하세요", function () {
            form.userId.focus();
        });
        return;
    }

    $.ajax({
        url: "/user/getUserIdExists",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userId: form.userId.value
        }),
        success: function (json) {
            if (json.data) {
                showPresetToast(json.data.icon, json.data.title, json.data.text);
                if (json.data.title === "사용가능") {
                    userIdCheck = "N";
                } else {
                    userIdCheck = "Y";
                }
            } else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }

        }
    });
}

// 이메일 중복 체크
function emailExists(form) {
    if (form.userEmail.value === "") {
        showPresetToast("warning", "주의", "이메일을 입력하세요", function () {
            form.userEmail.focus();
        });
        return;
    }

    $.ajax({
        url: "/user/getEmailExists",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userEmail: form.userEmail.value
        }),
        success: function (json) {
            if (json.data) {
                showPresetToast(json.data.icon, json.data.title, json.data.text);
            }
        }
    });
}

// 회원가입 정보의 유효성 체크하기
function register(form) {
    if (form.userId.value === "") {
        showPresetToast("warning", "주의", "아이디를 입력하세요", function () {
            form.userId.focus();
        });
        return;
    }
    if (userIdCheck !== "N") {
        showPresetToast("warning", "주의", "아이디 중복 체크 및 중복되지 않은 아이디로 가입 바랍니다",
            function () {
                form.userId.focus();
            });
        return;
    }
    if (form.userName.value === "") {
        showPresetToast("warning", "주의", "이름을 입력하세요", function () {
            form.userName.focus();
        });
        return;
    }
    if (form.userPw.value === "") {
        showPresetToast("warning", "주의", "비밀번호를 입력하세요", function () {
            form.userPw.focus();
        });
        return;
    }
    if (form.userPw2.value === "") {
        showPresetToast("warning", "주의", "비밀번호 확인을 입력하세요", function () {
            form.userPw2.focus();
        });
        return;
    }
    if (form.userPw.value !== form.userPw2.value) {
        showPresetToast("warning", "주의", "비밀번호와 비밀번호 확인이 일치하지 않습니다",
            function () {
                form.userPw.focus();
            });
        return;
    }
    if (form.userEmail.value === "") {
        showPresetToast("warning", "주의", "이메일을 입력하세요", function () {
            form.userEmail.focus();
        });
        return;
    }
    if (form.authNumber.value === "") {
        showPresetToast("warning", "주의", "이메일 인증번호를 입력하세요", function () {
            form.authNumber.focus();
        });
        return;
    }

    $.ajax({
        url: "/user/insertUserInfo",
        type: "post",
        dataType: "json",
        contentType: "application/json", // sending JSON to the server
        data: JSON.stringify($("#form").serializeObject()),
        success: function (json) {
            if (json.data) {
                showPresetToast(json.data.icon, json.data.title, json.data.text);

                // ✅ Optional redirect on success
                if (json.message === "success") {
                    window.location.href = "/user/login";
                }
            } else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }
        }
    });
}
