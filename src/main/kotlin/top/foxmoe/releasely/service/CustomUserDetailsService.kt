package top.foxmoe.releasely.service

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
// import top.foxmoe.releasely.repository.UserRepository // Assuming you create a repository or use mapper
import top.foxmoe.releasely.mapper.UserMapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.Collections

@Service
class CustomUserDetailsService(private val userMapper: UserMapper) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val queryWrapper = QueryWrapper<top.foxmoe.releasely.entity.User>()
        queryWrapper.eq("username", username)
        val user = userMapper.selectOne(queryWrapper) ?: throw UsernameNotFoundException("User not found")

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.passwordHash,
            Collections.singletonList(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}

