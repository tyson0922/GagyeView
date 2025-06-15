    // ✅ Replace your entire scan.js with this version (no OpenCV required)

    let fabricCanvas;
    let uploadedImage;
    let cropRect;

    $(document).ready(function () {
        // ✅ 이미지 업로드 및 Fabric에 표시
        $('#receiptImage').on('change', function (event) {
            const file = event.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = function (e) {
                const imgObj = new Image();
                imgObj.onload = function () {
                    const canvasEl = document.getElementById('receiptCanvas');
                    canvasEl.width = imgObj.width;
                    canvasEl.height = imgObj.height;

                    fabricCanvas = new fabric.Canvas('receiptCanvas', {
                        selection: false
                    });

                    fabric.Image.fromURL(e.target.result, function (img) {
                        uploadedImage = img;
                        img.selectable = false;

                        const maxWidth = 600;
                        const maxHeight = 800;

                        const scale = Math.min(maxWidth / img.width, maxHeight / img.height, 1);

                        // 💡 Apply scaling to image and canvas dimensions
                        img.scaleX = scale;
                        img.scaleY = scale;

                        const scaledWidth = img.width * scale;
                        const scaledHeight = img.height * scale;

                        fabricCanvas.setWidth(scaledWidth);
                        fabricCanvas.setHeight(scaledHeight);

                        fabricCanvas.clear();
                        fabricCanvas.setBackgroundImage(img, fabricCanvas.renderAll.bind(fabricCanvas));
                    });

                    $('#uploadedReceiptPreview').show();
                };
                imgObj.src = e.target.result;
            };
            reader.readAsDataURL(file);
        });

        // ✅ 업로드 처리
        $('#receiptUploadForm').on('submit', function (e) {
            e.preventDefault();
            const fileInput = $('#receiptImage')[0];
            if (!fileInput.files.length) {
                showPresetToast('warning', '주의', '이미지를 선택하세요');
                return;
            }

            const formData = new FormData(this);
            $.ajax({
                url: '/scan/upload',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (json) {
                    if (json.data && json.message === 'success') {
                        showPresetToast(json.data.icon, json.data.title, json.data.text, () => {
                            window.location.href = '/scan/result';
                        });
                    }
                },
                error: function () {
                    showPresetToast('error', '오류', '서버 오류가 발생했습니다.');
                }
            });
        });

        // ✅ 등록 버튼 (사용자 로직 삽입 가능)
        $('#submitReceiptBtn').on('click', async function () {
            if (!uploadedImage) {
                showPresetToast('warning', '주의', '이미지를 먼저 업로드하세요.');
                return;
            }

            const dataURL = uploadedImage.toDataURL();
            const base64 = dataURL.replace(/^data:image\/(png|jpeg);base64,/, '');

            showPresetToast('info', '처리 중', 'Google Vision API 호출 중...');

            // 1. OCR 요청
            const visionRes = await fetch('/receipt/api/v1/vision', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ image: base64 })
            });
            const visionJson = await visionRes.json();

            const text = visionJson.data || '텍스트 없음';
            console.log("📄 OCR 결과:", text);

            // 사용자 ID를 HTML 어딘가에서 가져옵니다 (ex. hidden input or data-attribute)
            const userId = document.getElementById('userIdInput')?.value;
            if (!userId) {
                showPresetToast('warning', '주의', '로그인이 필요합니다.');
                return;
            }

            showPresetToast('info', '처리 중', '분류 및 저장 중...');

            // 2. GPT 분석 + 저장 요청
            const saveRes = await fetch('/receipt/api/v1/analyze-and-save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ocrText: text,
                    userId: userId
                })
            });
            const saveJson = await saveRes.json();

            console.log("💾 저장 결과:", saveJson);
            console.log("🧪 상태 값:", saveJson.status, typeof saveJson.status);

            if (saveJson.httpStatus?.toUpperCase() === 'OK') {
                Swal.fire({
                    icon: 'success',
                    title: '저장 완료',
                    html: `<b>GPT 분석 및 저장 성공</b><br><br><pre>${text}</pre>`,
                    confirmButtonText: '확인'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: '실패',
                    text: saveJson.message || '저장 실패'
                });
            }

        });

        // ✅ 스캔 영역 수정 버튼
        $('#editScanAreaBtn').on('click', function () {
            if (!fabricCanvas || !uploadedImage) {
                showPresetToast('warning', '주의', '먼저 이미지를 업로드하세요.');
                return;
            }

            // Remove existing rect
            if (cropRect) {
                fabricCanvas.remove(cropRect);
            }

            // Add default crop rectangle (user will adjust it)
            cropRect = new fabric.Rect({
                left: 100,
                top: 100,
                width: 200,
                height: 300,
                fill: 'rgba(0, 0, 255, 0.2)',
                stroke: 'blue',
                strokeWidth: 2,
                selectable: true,
                hasRotatingPoint: false,
                cornerColor: 'blue'
            });

            fabricCanvas.add(cropRect);
            fabricCanvas.setActiveObject(cropRect);
            fabricCanvas.renderAll();

            // ✅ Optional toast
            showPresetToast('info', '안내', '자르고 싶은 영역을 드래그하거나 크기를 조절하세요.\n완료 후 "선택 완료" 버튼을 눌러주세요.');
        });

        $('#confirmCropBtn').on('click', function () {
            if (!cropRect) {
                showPresetToast('warning', '주의', '스캔 영역을 먼저 지정해주세요.');
                return;
            }

            Swal.fire({
                title: '스캔 영역을 적용할까요?',
                text: '선택한 영역으로 이미지를 자릅니다.',
                icon: 'question',
                showCancelButton: true,
                confirmButtonText: '예, 적용하기',
                cancelButtonText: '아니요'
            }).then(result => {
                if (result.isConfirmed) {
                    applyCrop(); // ✅ Proceed
                }
            });
        });
    });

    function applyCrop() {
        const rect = cropRect.getBoundingRect();
        const canvasEl = document.createElement('canvas');
        canvasEl.width = rect.width;
        canvasEl.height = rect.height;
        const ctx = canvasEl.getContext('2d');

        const tempCanvas = new fabric.StaticCanvas(canvasEl);
        const croppedImage = uploadedImage.toDataURL({
            left: rect.left,
            top: rect.top,
            width: rect.width,
            height: rect.height
        });

        fabric.Image.fromURL(croppedImage, function (img) {
            fabricCanvas.clear();
            fabricCanvas.setBackgroundImage(img, fabricCanvas.renderAll.bind(fabricCanvas));
        });
    }
