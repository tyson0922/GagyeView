$(document).ready(function () {
    let form = document.getElementById("form");

    // 이메일 인증메일 발송
    $('#updateUserNameBtn').on("click", function () {
        updateUserName(form);
    });

    // 이메일 인증 메일 발송
    $('#sendAuthEmail').on("click", function () {
        sendAuthEmailForEmailUpdate(form);
    });

    // 인증 번호 후 이메일 업데이트
    $('#updateUserEmailBtn').on("click", function () {
        verifyAuthCodeOnly(form);
    });

    // 새로운 비밀번호 생성
    $('#newPwBtn').on("click", function () {
        newPassword(form);
    });

    // 회원 탈퇴
    $('#deleteAccountBtn').on("click", function () {

        showPresetToast("commitWarning", "정말 탈퇴하시겠습니까?",
            "탈퇴 시 모든 정보가 삭제되며 복구할 수 없습니다.", function(){
                deleteAccount();
            }
        );
    });

});

function updateUserName(form) {
    if (form.userName.value === "") {
        showPresetToast("warning", "주의", "아이디를 입력하세요", function () {
            form.userName.focus();
        });
        return;
    }

    $.ajax({
        url: "/user/updateUserName",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userName: form.userName.value
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
function sendAuthEmailForEmailUpdate(form){
    if (form.userEmail.value === ""){
        showPresetToast("warning", "주의", "이메일을 입력하세요", function () {
            form.userEmail.focus();
        });
        return;
    }
    $.ajax({
        url: "/user/sendAuthEmailForEmailUpdate",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userEmail: form.userEmail.value
        }),
        success: function(json){
            if (json.data){
                showPresetToast(json.data.icon, json.data.title, json.data.text);
            } else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }
        }
    })
}


function verifyAuthCodeOnly(form) {
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
        url:"/user/verifyAuthCodeOnly",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            userEmail: form.userEmail.value,
            authNumber: form.authNumber.value
        }),
        success: function (json) {
            if (json.data) {
                // showPresetToast(json.data.icon, json.data.title, json.data.text, function(){

                // });
                updateUserEmail(form);
            }  else {
                showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
            }
        }

    });
}


function updateUserEmail(form){
    $.ajax({
        url: "/user/updateUserEmail",
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
    })

}

function newPassword(form) {

        if (form.userPw.value === "") {
            showPresetToast("warning", "주의", "비밀번호를 입력하세요", function () {
                form.userPw.focus();
            });
            return;
        }
        if (form.userPw2.value === "") {
            showPresetToast("warning", "주의", "비밀번호를 입력하세요", function () {
                form.userPw2.focus();
            });
            return;
        }
        if (form.userPw.value !== form.userPw2.value) {
            showPresetToast("error", "오류", "비밀번호가 일치하지 않습니다", function () {
                form.userPw2.focus();
            });
            return;
        }
        $.ajax({
            url: "/user/newPwProc",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                userPw: form.userPw.value,
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

function deleteAccount(){
    $.ajax({
        url: "/user/deleteUserById",
        type: "post",
        dataType: "json",
        success: function (json) {
            if (json.data){
                showPresetToast(json.data.icon, json.data.title, json.data.text, function(){
                   if (json.data.title === "회원 탈퇴 성공"){
                       location.href = "/user/login";
                   }
                });
            }
        }
    });
}