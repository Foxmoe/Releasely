package top.foxmoe.releasely.shared

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}! \nCiallo!🎉"
    }
}