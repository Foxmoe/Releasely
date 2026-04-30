package top.foxmoe.releasely.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuditLog(
    val action: String,
    val resourceType: String = ""
)
