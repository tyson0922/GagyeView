document.addEventListener('DOMContentLoaded', function () {

    // ✅ Chart DOM Elements
    const expenseDonut = echarts.init(document.getElementById('expenseDonut'));
    const incomeDonut = echarts.init(document.getElementById('incomeDonut'));
    const barChart = echarts.init(document.getElementById('barChart'));
    const expenseStackChart = echarts.init(document.getElementById('expenseStackChart'));
    const incomeStackChart = echarts.init(document.getElementById('incomeStackChart'));

    // ✅ 공통: JSON 문자열 → 객체 변환 함수
    const parseJson = (data, label) => {
        if (typeof data === 'string') {
            try {
                return JSON.parse(data);
            } catch (e) {
                console.error(`❌ ${label} JSON 파싱 실패:`, e);
                return [];
            }
        }
        return Array.isArray(data) ? data : [];
    };

    // ✅ 1. 도넛 차트용 데이터 가공
    const toDonutData = (data) => {
        return parseJson(data, 'DonutData').map(d => {
            const val = parseFloat(String(d.value).replace(/,/g, ''));
            return {
                name: String(d.name),
                value: isNaN(val) ? 0 : val
            };
        });
    };

    // ✅ 2. 도넛 차트 그리기
    expenseDonut.setOption({
        title: { text: '월간 지출 총합', subtext: '카테고리별 지출', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
            name: '지출',
            type: 'pie',
            radius: ['40%', '70%'],
            data: toDonutData(window.donutExpenseData),
            emphasis: {
                itemStyle: {
                    shadowBlur: 10, shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }]
    });

    incomeDonut.setOption({
        title: { text: '월간 수입 총합', subtext: '카테고리별 수입', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
            name: '수입',
            type: 'pie',
            radius: ['40%', '70%'],
            data: toDonutData(window.donutIncomeData),
            emphasis: {
                itemStyle: {
                    shadowBlur: 10, shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }]
    });

    // ✅ 3. 바 차트 (월별 수입/지출 비교)
    const incomeExpense = parseJson(window.monthlyIncomeExpenseData, 'IncomeExpense');
    const barMonths = incomeExpense.map(d => d.name);
    const incomeSeries = incomeExpense.map(d => d.income || 0);
    const expenseSeries = incomeExpense.map(d => d.expense || 0);

    barChart.setOption({
        title: { text: '월별 수입/지출 비교', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { bottom: 0 },
        xAxis: { type: 'category', data: barMonths },
        yAxis: { type: 'value' },
        series: [
            { name: '수입', type: 'bar', data: incomeSeries },
            { name: '지출', type: 'bar', data: expenseSeries }
        ]
    });

    // ✅ 4. 공통 스택 바 차트 함수
    const renderStackChart = (element, data, titleText) => {
        const parsed = parseJson(data, titleText);
        if (!parsed.length) return;

        const months = [...new Set(parsed.map(d => d.yrMon))].sort();
        const cats = [...new Set(parsed.map(d => d.catNm))];
        const grouped = {};

        parsed.forEach(d => {
            const val = parseFloat(String(d.total).replace(/,/g, '')) || 0;
            if (!grouped[d.catNm]) grouped[d.catNm] = {};
            grouped[d.catNm][d.yrMon] = val;
        });

        const series = cats.map(cat => ({
            name: cat,
            type: 'bar',
            stack: 'total',
            data: months.map(m => grouped[cat][m] || 0)
        }));

        element.setOption({
            title: { text: titleText, left: 'center' },
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: { bottom: 0 },
            xAxis: { type: 'category', data: months },
            yAxis: { type: 'value' },
            series
        });
    };

    // ✅ 5. 스택 바 차트 렌더링
    renderStackChart(expenseStackChart, window.monthlyExpenseStackData, '월별 지출 내역');
    renderStackChart(incomeStackChart, window.monthlyIncomeStackData, '월별 수입 내역');

    // ✅ 6. PDF 다운로드 버튼
    // PDF 저장
    const downloadBtn = document.getElementById('downloadBtn');
    downloadBtn?.addEventListener('click', () => {
        const element = document.getElementById('pdfWrapper');

        window.scrollTo(0, 0); // ✅ 스크롤 최상단으로 이동

        [expenseDonut, incomeDonut, barChart, expenseStackChart, incomeStackChart].forEach(chart => chart.resize());

        setTimeout(() => {
            html2canvas(element, {
                scale: 3,
                scrollY: 0,
                useCORS: true,
                allowTaint: true
            }).then(canvas => {
                const imgData = canvas.toDataURL('image/jpeg', 1.0);
                const pdf = new jspdf.jsPDF('p', 'mm', 'a4');

                const pageWidth = 210;
                const pageHeight = 297;
                const imgWidth = pageWidth;
                const imgHeight = canvas.height * imgWidth / canvas.width;

                let heightLeft = imgHeight;
                let position = 0;

                // ✅ 첫 페이지
                pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
                heightLeft -= pageHeight;

                // ✅ 페이지 넘기기 (하단 잘림 방지)
                while (heightLeft > 0) {
                    position -= pageHeight;
                    pdf.addPage();
                    pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
                    heightLeft -= pageHeight;
                }

                pdf.save('금융_내역.pdf');
            });

        }, 300);
    });
});
