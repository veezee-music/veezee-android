package cloud.veezee.android.models

class SettingModel(val viewType: ViewType) {

    enum class ViewType {
        TEXT,
        SWITCH
    }

    enum class ItemType {
        THEME,
        COLORED_PLAYER,
        MEMORY_OPTIMIZATION,
        OFFLINE_ACCESS
    }

    companion object {
        const val THEME_CHANGED = 0;
        const val COLORED_PLAYER_CHANGED = 1;
    }

    var itemType: ItemType? = null;
    var title: String = ""
    var icon: Int = 0;
}