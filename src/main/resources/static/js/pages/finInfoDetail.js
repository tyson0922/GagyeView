$(document).ready(function () {

    // 수동 입력창 보이기
    $('#showManualInputBtn').on("click", function () {
        $('#manualInputRow').stop(true, true).slideDown();
        $('#cancelInsertBtnContainer').removeClass('hidden').hide().fadeIn();
    });

    // 수동 입력 취소
    $('#cancelInsertBtn').on("click", function () {
        $('#manualInputRow').slideUp();
        $('#cancelInsertBtnContainer').fadeOut(function () {
            $(this).addClass('hidden');
        });
        $('#manualTrnsForm')[0].reset();
        $('#manualTrnsForm').removeAttr("data-mode data-id");
    });

    // 거래 정보 제출 (insert or update)
    $('#manualTrnsForm').on("submit", function (e) {
        e.preventDefault();

        const isUpdate = $(this).attr("data-mode") === "update";
        const id = $(this).attr("data-id");

        const formData = {
            catType: $("select[name='catType']").val(),
            catNm: $("input[name='catNm']").val(),
            trnsDt: $("input[name='trnsDt']").val(),
            totNm: $("input[name='totNm']").val(),
            srcNm: $("input[name='srcNm']").val(),
            note: $("input[name='note']").val()
        };

        if (!formData.catType || !formData.catNm || !formData.trnsDt || !formData.totNm) {
            showPresetToast("warning", "필수", "항목/날짜/금액은 필수입니다");
            return;
        }

        const commonPayload = {
            catType: formData.catType,
            monTrnsDetailDTO: {
                catNm: formData.catNm,
                trnsDt: formData.trnsDt,
                totNm: formData.totNm,
                srcNm: formData.srcNm,
                note: formData.note
            }
        };

        if (isUpdate) {
            commonPayload.id = id;

            $.ajax({
                url: "/finInfo/modify",
                type: "post",
                contentType: "application/json",
                data: JSON.stringify(commonPayload),
                success: function (res) {
                    const { title, text, icon } = res.data;
                    Swal.fire({ title, text, icon }).then(() => {
                        if (res.message === "success") {
                            window.location.href = "/finInfo/detail";
                        }
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "서버 오류 발생");
                }
            });

        } else {
            $.ajax({
                url: "/finInfo/manualInsert",
                type: "post",
                contentType: "application/json",
                data: JSON.stringify(commonPayload),
                success: function (res) {
                    const { title, text, icon } = res.data;
                    Swal.fire({ title, text, icon }).then(() => {
                        if (res.message === "success") {
                            window.location.href = "/finInfo/detail";
                        }
                    });
                },
                error: function () {
                    showPresetToast("error", "오류", "서버 오류 발생");
                }
            });
        }
    });

    // 수정 버튼 클릭 시 폼에 값 채우기
    $(document).on("click", ".updateBtn", function () {
        $("select[name='catType']").val($(this).data("type")); // 추가: catType 설정
        $("input[name='catNm']").val($(this).data("cat"));
        $("input[name='trnsDt']").val($(this).data("date"));
        $("input[name='totNm']").val($(this).data("amount"));
        $("input[name='srcNm']").val($(this).data("src"));
        $("input[name='note']").val($(this).data("note"));

        $("#manualTrnsForm").attr("data-mode", "update");
        $("#manualTrnsForm").attr("data-id", $(this).data("id"));

        $("#manualInputRow").slideDown();
        $("#cancelInsertBtnContainer").removeClass("hidden").hide().fadeIn();
    });

    // 삭제 버튼 클릭 시 확인 후 삭제 요청
    $(document).on("click", ".deleteBtn", function () {
        const id = $(this).data("id");

        showPresetToast(
            "commitWarning",
            "삭제 확인",
            "이 거래 정보를 정말 삭제하시겠습니까?",
            function (result) {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `/finInfo/delete/${id}`,
                        type: "delete",
                        success: function (res) {
                            const { title, text, icon } = res.data;
                            showPresetToast(icon, title, text, function () {
                                if (res.message === "success") {
                                    window.location.href = "/finInfo/detail";
                                }
                            });
                        },
                        error: function () {
                            showPresetToast("error", "오류", "삭제 중 서버 오류 발생");
                        }
                    });
                }
            }
        );
    });

    document.getElementById('filterBtn').addEventListener('click', function () {
        const startDate = new Date(document.getElementById('startDate').value);
        const endDate = new Date(document.getElementById('endDate').value);
        const rows = document.querySelectorAll('.divTableBody .divTableRow');

        rows.forEach(row => {
            const dateCell = row.querySelectorAll('.divTableCell')[2]; // 날짜 셀
            if (!dateCell) return; // 헤더나 수동입력 행 무시

            const rowDate = new Date(dateCell.textContent.trim());

            const isVisible = (!isNaN(startDate) ? rowDate >= startDate : true) &&
                (!isNaN(endDate) ? rowDate <= endDate : true);

            row.style.display = isVisible ? '' : 'none';
        });
    });


});
