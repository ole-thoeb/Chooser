package chooser.com.example.eloem.chooser.database

import androidx.room.*
import org.jetbrains.anko.db.FOREIGN_KEY

@Fts4
@Entity
class ChooserItemChooserSql(@PrimaryKey val id: Int,
                                 val name: String,
                                 val currentPos: Int,
                                 val type: String)

@Fts4
@Entity(primaryKeys = ["chooserId", "originalPosition"])
class ChooserItemSql(val name: String,
                          @ForeignKey(entity = ChooserItemChooserSql::class, parentColumns = ["id"], childColumns = ["chooserId"]) val chooserId: Int,
                          val position: Int,
                          val originalPosition: Int,
                          val weight: Int)

class ChooserWithChooserItems {
    @Embedded var chooser: ChooserItemChooserSql? = null
    @Relation(parentColumn = "id", entityColumn = "chooserId")
    var items: MutableList<ChooserItemSql>? = null
}

@Fts4
@Entity
class MultiDiceListSql(
        @PrimaryKey val id: Int,
        val title: String
)

@Fts4
@Entity
@ForeignKey(entity = MultiDiceListSql::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE)
class MultiDiceSql(
        @PrimaryKey val diceId: Int,
        val listId: Int,
        val sides: Int,
        val times: Int,
        val color: Int
)

@Fts4
@Entity
@ForeignKey(entity = MultiDiceSql::class, parentColumns = ["diceId"], childColumns = ["multiDiceId"], onDelete = ForeignKey.CASCADE)
class MultiDiceCurrentSql(
        val multiDiceId: Int,
        val value: Int
) {
    
    @PrimaryKey(autoGenerate = true)
    var curId: Int? = null
}

/*class MultiDiceListWithDice {
    @Embedded
    var multiDiceList: MultiDiceListSql? = null
    @Relation(parentColumn = "id", entityColumn = "listId")
    var dices: List<MultiDiceWithCurrent>? = null
}*/

data class MultiDiceListWithDices(val multiDices: List<MultiDiceWithCurrent>, val lists: List<MultiDiceListSql>)

class MultiDiceWithCurrent {
    @Embedded
    var multiDice: MultiDiceSql? = null
    @Relation(parentColumn = "diceId", entityColumn = "multiDiceId")
    var current: List<MultiDiceCurrentSql>? = null
}