package top.foxmoe.releasely.shared

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

fun getPlatform(): Platform {
    return AndroidPlatform()
}