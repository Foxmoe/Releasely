package top.foxmoe.releasely.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import top.foxmoe.releasely.entity.HealthReport
import org.apache.ibatis.annotations.Mapper

@Mapper
interface HealthReportMapper : BaseMapper<HealthReport>