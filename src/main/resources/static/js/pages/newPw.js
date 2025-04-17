$(document).ready(function () {

    let form = document.getElementById("form");
    $("#nwPwBtn").on("click", function () {
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
            data: JSON.stringify($("#form").serializeObject()),
            success: function (json) {
                if (json.data) {
                    showPresetToast(json.data.icon, json.data.title, json.data.text, function () {
                        location.href = "/user/login";
                    });
                } else {
                    showPresetToast("error", "오류", "서버 응답이 올바르지 않습니다");
                }
            }
        });
    });
});