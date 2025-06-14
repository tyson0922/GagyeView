package kopo.gagyeview.persistence.mapper;

import kopo.gagyeview.dto.BarChartDTO;
import kopo.gagyeview.dto.DonutChartDTO;
import kopo.gagyeview.dto.MonTrnsDTO;
import kopo.gagyeview.dto.StackBarDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ISumMapper {

    // MON_SUM (total income/expense/balance for each user per month)
    int upsertMonSum(MonTrnsDTO pDTO);          // Insert or update total
    int decrementMonSum(MonTrnsDTO pDTO);       // Subtract values (on update/delete)

    // MON_CAT_SUM (per-category totals)
    int upsertMonCatSum(MonTrnsDTO pDTO);       // Insert or update category total
    int decrementMonCatSum(MonTrnsDTO pDTO);    // Subtract category value

    void recalculateCatPerc(MonTrnsDTO pDTO);

    List<DonutChartDTO> getDonutByCatType(@Param("userId") String userId,
                                          @Param("catType") String catType,
                                          @Param("yrMon") String yrMon);

    List<BarChartDTO> getMonthlyIncomeExpense(@Param("userId") String userId);

    List<StackBarDTO> getMonthlyStack(@Param("userId") String userId,
                                      @Param("catType") String catType);

    BigDecimal getTotalAmountByType(@Param("userId") String userId,
                                    @Param("catType") String catType);

    BigDecimal getMonthlyTotal(@Param("userId") String userId,
                               @Param("catType") String catType,
                               @Param("yrMon") String yrMon);

}
