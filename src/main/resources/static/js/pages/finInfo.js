document.addEventListener('DOMContentLoaded', function () {
    const expenseDonut = echarts.init(document.getElementById('expenseDonut'));
    const incomeDonut = echarts.init(document.getElementById('incomeDonut'));
    // const barChart = echarts.init(document.getElementById('barChart'));
    const stackChart = echarts.init(document.getElementById('stackChart'));

    // if (!Array.isArray(window.stackData) || !Array.isArray(window.barData)) {

    if (!Array.isArray(window.stackData)) {
        console.error("차트 데이터가 올바르게 전달되지 않았습니다.");
        return;
    }

    // 수입 / 지출 데이터를 분리하여 카테고리별 합계 계산
    const incomeData = {};
    const expenseData = {};

    window.stackData.forEach(d => {
        const target = d.catType === "수입" ? incomeData : expenseData;
        if (!target[d.catNm]) target[d.catNm] = 0;
        target[d.catNm] += d.total;
    });

    const toChartData = (obj) =>
        Object.entries(obj).map(([name, value]) => ({ name, value }));

    // 수입 도넛 차트
    incomeDonut.setOption({
        title: { text: '월간 수입 총합', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { right: 0, top: 'middle', orient: 'vertical' },
        series: [{
            name: '수입',
            type: 'pie',
            radius: ['40%', '70%'],
            data: toChartData(incomeData)
        }]
    });

    // 지출 도넛 차트
    expenseDonut.setOption({
        title: { text: '월간 지출 총합', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { right: 0, top: 'middle', orient: 'vertical' },
        series: [{
            name: '지출',
            type: 'pie',
            radius: ['40%', '70%'],
            data: toChartData(expenseData)
        }]
    });
    //
    // // 수입/지출 막대 비교 차트
    // const barMonths = window.barData.map(d => d.yrMon);
    // const incomeSeries = window.barData.map(d => d.income || 0);
    // const expenseSeries = window.barData.map(d => d.expense || 0);
    //
    // barChart.setOption({
    //     title: { text: '월별 수입 vs 지출', left: 'center' },
    //     tooltip: { trigger: 'axis' },
    //     legend: { top: '10%' },
    //     xAxis: { type: 'category', data: barMonths },
    //     yAxis: { type: 'value' },
    //     series: [
    //         { name: '수입', type: 'bar', data: incomeSeries },
    //         { name: '지출', type: 'bar', data: expenseSeries }
    //     ]
    // });

    // 스택 바 차트 (월별 카테고리)
    const months = [...new Set(window.stackData.map(d => d.yrMon))].sort();
    const categories = [...new Set(window.stackData.map(d => d.catNm))];

    const grouped = {};
    window.stackData.forEach(d => {
        if (!grouped[d.catNm]) grouped[d.catNm] = {};
        grouped[d.catNm][d.yrMon] = d.total;
    });

    const stackSeries = categories.map(cat => ({
        name: cat,
        type: 'bar',
        stack: 'total',
        data: months.map(m => grouped[cat][m] || 0)
    }));

    stackChart.setOption({
        title: { text: '월별 카테고리별 수입/지출', left: 'center' },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { top: '10%' },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value' },
        series: stackSeries
    });
});