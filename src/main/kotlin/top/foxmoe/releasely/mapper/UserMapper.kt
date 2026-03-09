package top.foxmoe.releasely.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper
import top.foxmoe.releasely.entity.User

@Mapper
interface UserMapper : BaseMapper<User>

