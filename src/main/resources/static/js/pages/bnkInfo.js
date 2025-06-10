console.log("bnkInfo.js loaded!");

$(document).ready(function () {

    $('#bankAddForm').on("submit", function (e) {
        e.preventDefault();

        const formData = {
            bnkNm: $('input[name="bnkNm"]').val().trim(),
            curNum: parseFloat($('input[name="curNum"]').val())
        };

        $.ajax({
            url: "/bank/insertUserBnk",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(formData),
            success: function (res) {
                const { icon, title, text } = res.data;
                showPresetToast(icon, title, text, function () {
                    if (icon === "success") location.reload();
                });
            },
            error: function () {
                showPresetToast("error", "오류", "계좌 등록 중 오류 발생");
            }
        });
    });
});

function editBank(button) {
    const bnkNm = $(button).data("bnk");
    showPresetToast("info", "준비 중", `${bnkNm} 계좌 수정은 현재 준비 중입니다.`);
}

function deleteBank(button) {
    const bnkNm = $(button).data("bnk");

    Swal.fire({
        title: `${bnkNm} 계좌를 삭제할까요?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "삭제",
        cancelButtonText: "취소"
    }).then(result => {
        if (result.isConfirmed) {
            $.ajax({
                url: "/bank/deleteUserBnk", // ← 삭제용 API 엔드포인트 필요
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify({ bnkNm }),
                success: function (res) {
                    const { icon, title, text } = res.data;
                    showPresetToast(icon, title, text, function () {
                        if (icon === "success") location.reload();
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "계좌 삭제 실패");
                }
            });
        }
    });
}
