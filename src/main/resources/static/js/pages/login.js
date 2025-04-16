// Html 로딩이 완료되고, 실행됨
$(document).ready(function () {

    $("#btnLogin").on("click", function () {

        let form = document.getElementById("form"); // form 태그

        if (form.userId.value === "") {
            showPresetToast("warning", "주의", "아이디를 입력하세요", function () {
                form.userId.focus();
            });
            return;
        }

        if (form.userPw.value === "") {
            showPresetToast("warning", "주의", "비밀번호를 입력하세요", function () {
                form.userPw.focus();
            })
            return;
        }

        // Ajax 호출해서 로그인하기
        $.ajax({
            url: "/user/loginProc",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify($("form").serializeObject()),
            success: function (json) { // /user/loginProc 호출이 성공했다면...

                if (json.data) {
                    showPresetToast(json.data.icon, json.data.title, json.data.text, function () {

                        if (json.message === "success") {
                            // ✅ Optional redirect on success
                            window.location.href = "/";
                        } else { // 로그인 실패
                            form.userId.focus();
                        }
                    });
                }
            }
        })

    })
})