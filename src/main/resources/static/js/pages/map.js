$(document).ready(function(){
    const container = document.getElementById('martMap');
    const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
    };

    const map = new kakao.maps.Map(container, options);

    let markers = []; // clear makers before new search
    let currentLocationMarker = null;

    function updateCurrentLocation(position) {
        const accuracy = position.coords.accuracy;
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        if (accuracy > 100) {
            showPresetToast("error", "오류", "고정밀 위치 정보를 가져올 수 없습니다.");
        }

        const latLng = new kakao.maps.LatLng(lat, lng);
        map.setCenter(latLng);

        if (currentLocationMarker) {
            currentLocationMarker.setMap(null);
        }
        const currentLocationImage = new kakao.maps.MarkerImage(
            '/images/currentMarker.svg', // blue version
            new kakao.maps.Size(40, 40)
        );

        currentLocationMarker = new kakao.maps.Marker({
            map: map,
            position: latLng,
            title: "현재 위치",
            image: currentLocationImage
        });
    }

    let watchId = null;

    function startLocationTracking() {
        if (navigator.geolocation) {
            // 이미 추적 중이면 중단
            if (watchId !== null) {
                navigator.geolocation.clearWatch(watchId);
            }

            watchId = navigator.geolocation.watchPosition(
                updateCurrentLocation,
                function (error) {
                    console.error("Geolocation watch error:", error);
                    showPresetToast("error", "오류", "위치 정보를 가져올 수 없습니다.");
                },
                {
                    enableHighAccuracy: true,
                    maximumAge: 0,
                    timeout: 10000
                }
            );
        } else {
            showPresetToast("warning", "경고", "브라우저가 위치 정보를 지원하지 않습니다.");
        }
    }

    $("#currentLocationBtn").on("click", function () {
        startLocationTracking(); // Starts continuous tracking
    });

    $("#searchMartBtn").on("click", function(){

        const form = document.getElementById("searchForm");
        const keyword = form.martName.value.trim();

        if(keyword === ""){
            showPresetToast("warning", "주의", "마트 이름을 입력하세요", function(){
                form.martName.focus();
            });
            return;
        }

        $.ajax({
            url: "/map/searchMart",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({ mName: keyword}),
            success: function(json){

                const rList = json.data; // from CommonResponse<T>
                const rCard = $("#searchResultCard"); //jquery object so resultCard.show methods are available
                const rListElement = $("#searchResultList"); //jquery object so resultListElement.append methods are available

                // clear previous results and markers
                rListElement.empty();
                markers.forEach(m => m.setMap(null));
                markers = [];

                if (!rList || rList.length === 0) {
                    rListElement.append("<li class='list-group-item'>검색 결과가 없습니다</li>");
                } else {
                    rList.forEach(mart => {
                        const html = `
                            <li class="list-group-item search-result d-flex justify-content-between align-items-center flex-column flex-sm-row">
                                <div class="flex-grow-1">
                                    <strong>
                                    <a href="${mart.mUrl}" target="_blank">${mart.mName}</a>
                                    </strong><br/>
                                    ${mart.mAddress}<br/>
                                    ${mart.mPhoneNm ?? "전화번호 없음"}
                                </div>
                                <div class="ms-2 flex-shrink-0">
                                    <button class="btn btn-sm caution-btn-outline move-to-map-btn"
                                            data-x="${mart.x}" data-y="${mart.y}">
                                        지도이동
                                    </button>
                                </div>
                               
                            </li>
                        `;
                        rListElement.append(html);

                        const martMarkerImage = new kakao.maps.MarkerImage(
                            '/images/martMarker.svg', // red version
                            new kakao.maps.Size(40, 40)
                        );

                        const marker = new kakao.maps.Marker({
                            map: map,
                            position: new kakao.maps.LatLng(mart.y, mart.x),
                            image: martMarkerImage
                        });

                        markers.push(marker);
                        }
                    );
                }

                rCard.show();

                $("#searchResultList").on("click", ".move-to-map-btn", function(){
                    const x = parseFloat($(this).data("x"));
                    const y = parseFloat($(this).data("y"));

                    const latlng = new kakao.maps.LatLng(y, x);
                    map.setCenter(latlng);
                });

            },
            error: function(){
                showPresetToast("error", "오류","마트 검색중 문제가 발생했습니다.");
            }
        });
    });

});