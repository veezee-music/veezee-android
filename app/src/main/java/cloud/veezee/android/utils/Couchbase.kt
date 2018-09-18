package cloud.veezee.android.utils

import android.content.Context
import com.couchbase.lite.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import cloud.veezee.android.models.PlayableItem

class Couchbase {

    private var myDatabase: Database? = null;
    var database: Database? = null
        get() = myDatabase;

    companion object {
        const val DATABASE_NAME = "veezee";

        private var instance: Couchbase? = null;

        fun getInstance(context: Context?): Couchbase? {

            if (instance != null)
                return instance!!;

            return try {

                instance = Couchbase();
                instance?.myDatabase = instance?.createDatabase(DATABASE_NAME, context!!);
                instance;

            } catch (e: Exception) {
                e.printStackTrace();
                null;
            }


        }

    }

    private fun databaseConfiguration(context: Context) = DatabaseConfiguration(context);

    private fun createDatabase(dataBaseName: String, context: Context): Database = Database(dataBaseName, databaseConfiguration(context));

    fun getAll(): ArrayList<PlayableItem> {
        val searchQuery = QueryBuilder.select(SelectResult.all()).from(DataSource.database(myDatabase));
        val playList = ArrayList<PlayableItem>();

        searchQuery.execute().mapTo(playList) { Gson().fromJson(Gson().toJson(it.toMap()["veezee"]), PlayableItem::class.java) };

        return playList;
    }

    fun getById(id: String): PlayableItem? {

        val doc: Document? = myDatabase?.getDocument(id);
        val j = Gson().toJson(doc?.toMap());

        return Gson().fromJson(j, PlayableItem::class.java);
    }

    fun save(result: LinkedTreeMap<String, Any>, id: String) {
        val mutableDoc = MutableDocument(id, result);
        myDatabase?.save(mutableDoc);
    }
}