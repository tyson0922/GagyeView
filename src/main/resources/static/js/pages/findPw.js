$(document).ready(function (){
    let form = document.getElementById("form");

    // 이메일 인증메일 발송
    $('#emailBtn').on("click", function(){
        findUserIdOrPwProc(form);
    });

    // 아이디 찾기 결과
    $('#findPwBtn').on("click", function (){
        verifyAuthCodeOnly(form);
    });
});

function findUserIdOrPwProc(form) {
    if (form.userName.value === "") {
        showPresetToast("warning", "주의", "이름을 입력하세요", function () {
            form.userName.focus();
        })
        return;
    }
    if (form.userEmail.value === "") {
        showPresetToast("warning", "주의", "이메일을 입력하세요", function () {
            form.userEmail.focus();
        });
        return;
    }


    $.ajax({
        url: "/user/findUserIdOrPwProc",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userName: form.userName.value,
            userEmail: form.userEmail.value
        }),
        success: function (json) {
            if (json.data) {
                showPresetToast(json.data.icon, json.data.title, json.data.text);
            } else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }
        }
    });
}

function verifyAuthCodeOnly(form){

    if (form.userName.value === "") {
        showPresetToast("warning", "주의", "아이디를 입력하세요", function () {
            form.userName.focus();
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
        showPresetToast("warning", "주의", "인증번호를 입력하세요", function () {
            form.authNumber.focus();
        });
        return;
    }

    $.ajax({
        url: "/user/verifyAuthCodeOnly",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify($("#form").serializeObject()),
        success: function (json) {
            if (json.data) {
                // showPresetToast(json.data.icon, json.data.title, json.data.text, function () {
                //     location.href = "/user/login";
                // });
                console.log("message:", json.message);
                console.log("data:", json.data);

                if (json.message === "success"){
                    location.href = "/user/newPw";
                } else {
                    showPresetToast("error", "인증 실패", json.data);
                }

            } else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }
        }
    });
}