package jn.mjz.web.DateBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;

import jn.mjz.web.Util.DateUtil;

public class CollectionDataBaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "Collection";
    public static final String CREATE_COLLECTION_DB = "create table Collection ("
            + "id integer primary key autoincrement,"
            + "uuid text, "
            + "name text, "
            + "url text, "
            + "date date, "
            + "time text, "
            + "cnt integer"
            + ")";

    public CollectionDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COLLECTION_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addCollection(Collection collection) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uuid", collection.getUuid());
        values.put("name", collection.getName());
        values.put("url", collection.getUrl());
        values.put("date", DateUtil.getFormattedDate(System.currentTimeMillis()));
        values.put("time", DateUtil.getFormattedTime(System.currentTimeMillis()));
        values.put("cnt", 0);
        database.insert(TABLE_NAME, null, values);
        database.close();
    }

    public void removeCollection(String uuid) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, "uuid = ?", new String[]{String.valueOf(uuid)});
        database.close();
    }

    public int editCollection(String uuid, Collection newCollection) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newCollection.getName());
        values.put("url", newCollection.getUrl());
        int number = database.update(TABLE_NAME, values, "uuid = ?", new String[]{uuid});
        database.close();
        return number;
    }

    public LinkedList<Collection> readCollections() {

        LinkedList<Collection> collections = new LinkedList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("select DISTINCT * from Collection where cnt = ? order by time asc", new String[]{String.valueOf(0)});
        if (cursor.moveToFirst()) {
            do {
                Collection collection = new Collection();

                collection.setName(cursor.getString(cursor.getColumnIndex("name")));
                collection.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                collection.setDate(cursor.getString(cursor.getColumnIndex("date")));
                collection.setTime(cursor.getString(cursor.getColumnIndex("time")));
                collection.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                collection.setId(cursor.getInt(cursor.getColumnIndex("id")));

                collections.add(collection);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return collections;
    }

    public Collection haveCollection(String url) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT * from Collection where url = ?", new String[]{url});
        Collection collection = new Collection();
        if (cursor.getCount() == 1) {
            if (cursor.moveToFirst()) {

                collection.setName(cursor.getString(cursor.getColumnIndex("name")));
                collection.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                collection.setDate(cursor.getString(cursor.getColumnIndex("date")));
                collection.setTime(cursor.getString(cursor.getColumnIndex("time")));
                collection.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                collection.setId(cursor.getInt(cursor.getColumnIndex("id")));
            }
        } else {
            collection = null;
        }
        return collection;
    }

    public int getCount() {
        int cnt = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT * from Collection where cnt = ?", new String[]{String.valueOf(0)});
        cnt = cursor.getCount();
        database.close();
        cursor.close();
        return cnt;
    }
}
