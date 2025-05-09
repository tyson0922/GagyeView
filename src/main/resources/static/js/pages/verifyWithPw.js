$(document).ready(function () {
    let form = document.getElementById("form");

    // 비밀번호 인증 발송
    $('#nwPwBtn').on("click", function () {

        if (form.userPw.value === "") {
            showPresetToast("warning", "주의", "비밀번호를 입력하세요", function () {
                form.userPw.focus();
            });
            return;
        }

        $.ajax({
            url: "/user/getUserPwCheckProc",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify($("#form").serializeObject()),
            success: function (json) {
                if (json.message === "success") {
                    // If password check passed
                    // showPresetToast("success", "성공", json.data, function () {
                        location.href = "/user/myPage";
                    // });

                } else if (json.message === "fail" && json.data) {
                    // If password check failed (json.data is SweetAlertMsgDTO)
                    showPresetToast(json.data.icon, json.data.title, json.data.text);

                } else {
                    // Unexpected fallback
                    showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
                }
            }
        });
    });
});