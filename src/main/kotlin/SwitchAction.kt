class SwitchAction(private vararg val actions: () -> Unit)
{
    var currentIndex = 0
        private set

    fun switch()
    {
        actions[currentIndex]()

        currentIndex++
        currentIndex %= actions.size
    }
}
