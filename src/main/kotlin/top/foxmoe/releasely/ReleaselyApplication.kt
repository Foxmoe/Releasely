package top.foxmoe.releasely

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("top.foxmoe.releasely.mapper")
class ReleaselyApplication

fun main(args: Array<String>) {
    runApplication<ReleaselyApplication>(*args)
}
