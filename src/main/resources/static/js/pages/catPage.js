console.log("catInfo.js loaded!");

$(document).ready(function () {

    // ✅ [1] 카테고리 등록
    $('#catAddForm').on("submit", function (e) {
        e.preventDefault();

        const formData = {
            catNm: $('input[name="catNm"]').val().trim(),
            catType: $('select[name="catType"]').val()
        };

        $.ajax({
            url: "/cat/insertUserCat",
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
                showPresetToast("error", "오류", "카테고리 등록 중 오류 발생");
            }
        });
    });

    // ✅ [2] 카테고리 수정
    $('#catEditForm').on("submit", function (e) {
        e.preventDefault();

        const form = $(this);

        const updateData = {
            newCatNm: form.find("input[name='catNm']").val().trim(),
            newCatType: form.find("select[name='catType']").val(),
            originalCatNm: form.data("original-nm"),
            originalCatType: form.data("original-type")
        };

        $.ajax({
            url: "/cat/updateUserCat",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(updateData),
            success: function (res) {
                const { icon, title, text } = res.data;
                showPresetToast(icon, title, text, function () {
                    if (icon === "success") {
                        $("#editCard").slideUp();
                        $("#addCard").slideDown();
                        location.reload();
                    }
                });
            },
            error: function () {
                showPresetToast("error", "오류", "카테고리 수정 중 오류 발생");
            }
        });
    });

});

function openEditForm(button) {
    const catNm = $(button).data("nm");
    const catType = $(button).data("type");

    const $form = $("#catEditForm");
    $form.find("input[name='catNm']").val(catNm);
    $form.find("select[name='catType']").val(catType);

    // 🔹 원래 값 저장 (수정 시 백엔드로 전송)
    $form.data("original-nm", catNm);
    $form.data("original-type", catType);

    $("#addCard").slideUp();
    $("#editCard").slideDown();
}

// ✅ 수정 취소
function cancelEdit() {
    $("#editCard").slideUp();
    $("#addCard").slideDown();
}

// ✅ 카테고리 삭제
function deleteCategory(button) {
    const catNm = $(button).data("nm");
    const catType = $(button).data("type");

    Swal.fire({
        title: `"${catNm}" 카테고리를 삭제할까요?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "삭제",
        cancelButtonText: "취소"
    }).then(result => {
        if (result.isConfirmed) {
            $.ajax({
                url: "/cat/deleteUserCat",
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify({ catNm, catType }),
                success: function (res) {
                    const { icon, title, text } = res.data;
                    showPresetToast(icon, title, text, function () {
                        if (icon === "success") location.reload();
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "카테고리 삭제 실패");
                }
            });
        }
    });
}

// ✅ 기본 카테고리로 초기화
function resetCategories() {
    Swal.fire({
        title: "기본 카테고리로 초기화할까요?",
        text: "모든 사용자 정의 카테고리가 삭제됩니다.",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "초기화",
        cancelButtonText: "취소"
    }).then(result => {
        if (result.isConfirmed) {
            $.ajax({
                url: "/cat/resetUserCats",
                method: "POST",
                success: function (res) {
                    const { icon, title, text } = res.data;
                    showPresetToast(icon, title, text, function () {
                        if (icon === "success") location.reload();
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "초기화 실패");
                }
            });
        }
    });
}
