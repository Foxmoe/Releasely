package top.foxmoe.releasely.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper
import top.foxmoe.releasely.entity.SyncRecord

@Mapper
interface SyncRecordMapper : BaseMapper<SyncRecord>