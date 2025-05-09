$(document).ready(function(){
    const container = document.getElementById('martMap');
    const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
    };

    const map = new kakao.maps.Map(container, options);

    let markers = []; // clear makers before new search

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
                            <li class="list-group-item search-result">
                                <strong>
                                    <a href="${mart.mUrl}" target="_blank">${mart.mName}</a>
                                </strong><br/>
                                ${mart.mAddress}<br/>
                                ${mart.mPhoneNm ?? "전화번호 없음"}
                            </li>
                        `;
                        rListElement.append(html);

                        const marker = new kakao.maps.Marker({
                            map: map,
                            position: new kakao.maps.LatLng(mart.y, mart.x)
                        });

                        kakao.maps.event.addListener(marker, 'click', function() {
                            map.setCenter(marker.getPosition());
                        });

                        markers.push(marker);
                        }
                    );
                }

                rCard.show();

            },
            error: function(){
                showPresetToast("error", "오류","마트 검색중 문제가 발생했습니다.");
            }
        });
    });
});