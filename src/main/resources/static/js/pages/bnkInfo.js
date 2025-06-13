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

    $('#bankEditForm').on("submit", function (e) {
        e.preventDefault();

        const updateData = {
            bnkNm: $(this).find("input[name='bnkNm']").val().trim(),
            iniNum: parseFloat($(this).find("input[name='iniNum']").val()),
            curNum: parseFloat($(this).find("input[name='curNum']").val())
        };

        $.ajax({
            url: "/bank/updateUserBnk",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(updateData),
            success: function (res) {
                const { icon, title, text } = res.data;
                showPresetToast(icon, title, text, function () {
                    if (icon === "success") {
                        // ✅ Restore add card
                        $("#editCard").slideUp();
                        $("#addCard").slideDown();
                        location.reload();
                    }
                });
            },
            error: function () {
                showPresetToast("error", "오류", "계좌 수정 중 오류 발생");
            }
        });
    });
});

function editBank(button) {
    const bnkNm = $(button).data("bnk");

    const card = $(button).closest(".card-content");
    const curNum = parseFloat(card.find("span").text().replace(/[^0-9]/g, ""));

    // OPTIONAL: iniNum can be retrieved via `data-ini` if available, or you can pass it in manually
    const iniNum = $(button).data("ini") || ""; // You'll need to bind this in your HTML

    Swal.fire({
        title: `${bnkNm} 계좌 수정`,
        html: `
            <div class="form-group text-start" style="margin-bottom: 10px;">
                <label for="iniNum">초기 금액</label>
                <input type="number" id="iniNum" class="swal2-input" value="${iniNum}" placeholder="초기 금액" min="0" step="1000">
            </div>
            <div class="form-group text-start">
                <label for="curNum">현재 금액</label>
                <input type="number" id="curNum" class="swal2-input" value="${curNum}" placeholder="현재 금액" min="0" step="1000">
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "저장",
        cancelButtonText: "취소",
        preConfirm: () => {
            const ini = parseFloat(document.getElementById("iniNum").value);
            const cur = parseFloat(document.getElementById("curNum").value);

            if (isNaN(ini) || isNaN(cur)) {
                Swal.showValidationMessage("모든 값을 입력해주세요.");
                return false;
            }

            return { iniNum: ini, curNum: cur };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const updatePayload = {
                bnkNm: bnkNm,
                iniNum: result.value.iniNum,
                curNum: result.value.curNum
            };

            $.ajax({
                url: "/bank/updateUserBnk",
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(updatePayload),
                success: function (res) {
                    const { icon, title, text } = res.data;
                    showPresetToast(icon, title, text, function () {
                        if (icon === "success") location.reload();
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "계좌 수정 실패");
                }
            });
        }
    });
}

function openEditForm(button) {
    const bnkNm = $(button).data("bnk");
    const iniNum = $(button).data("ini");
    const curNum = $(button).data("cur");

    const $form = $("#bankEditForm");
    $form.find("input[name='bnkNm']").val(bnkNm);
    $form.find("input[name='iniNum']").val(iniNum);
    $form.find("input[name='curNum']").val(curNum);

    // ✅ Hide 계좌 추가
    $("#addCard").slideUp();
    // ✅ Show 계좌 수정
    $("#editCard").slideDown();
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

function cancelEdit() {
    $("#editCard").slideUp();
    $("#addCard").slideDown();
}
