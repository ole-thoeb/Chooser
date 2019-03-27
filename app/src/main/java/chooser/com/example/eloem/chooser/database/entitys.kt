package chooser.com.example.eloem.chooser.database

import androidx.room.*

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