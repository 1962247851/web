package jn.mjz.web.DateBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;

import jn.mjz.web.Util.DateUtil;

public class HistoryDataBaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "History";
    public static final String CREATE_COLLECTION_DB = "create table History ("
            + "id integer primary key autoincrement,"
            + "uuid text, "
            + "name text, "
            + "date date, "
            + "time text, "
            + "cnt integer,"
            + "times integer"
            + ")";

    public HistoryDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COLLECTION_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addHistory(History history) {
        History newHistory = haveHistory(history.getName());
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cnt", 0);
        if (newHistory == null) {
            values.put("uuid",history.getUuid());
            values.put("name",history.getName());
            values.put("date", DateUtil.getFormattedDate(System.currentTimeMillis()));
            values.put("time", DateUtil.getFormattedTime(System.currentTimeMillis()));
            values.put("times", 1);
            database.insert(TABLE_NAME, null, values);
        }else{
            editHistory(newHistory.getUuid(),newHistory);
        }
        database.close();
    }

    public void removeHistory(String uuid) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, "uuid = ?", new String[]{String.valueOf(uuid)});
        database.close();
    }

    public int editHistory(String uuid, History newHistory) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("times", newHistory.getTimes()+1);
        int number = database.update(TABLE_NAME, values, "uuid = ?", new String[]{uuid});
        database.close();
        return number;
    }

    public LinkedList<History> readHistory() {

        LinkedList<History> histories = new LinkedList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        //desc降序 asc升序
        Cursor cursor = database.rawQuery("select DISTINCT * from History where cnt = ? order by times desc", new String[]{String.valueOf(0)});
        if (cursor.moveToFirst()) {
            do {
                History history = new History();

                history.setName(cursor.getString(cursor.getColumnIndex("name")));
                history.setDate(cursor.getString(cursor.getColumnIndex("date")));
                history.setTime(cursor.getString(cursor.getColumnIndex("time")));
                history.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                history.setId(cursor.getInt(cursor.getColumnIndex("id")));

                histories.add(history);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return histories;
    }

    public History haveHistory(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT * from History where name = ?", new String[]{name});
        History history = new History();
        if (cursor.getCount() == 1) {
            if (cursor.moveToFirst()) {

                history.setName(cursor.getString(cursor.getColumnIndex("name")));
                history.setDate(cursor.getString(cursor.getColumnIndex("date")));
                history.setTime(cursor.getString(cursor.getColumnIndex("time")));
                history.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                history.setTimes(cursor.getInt(cursor.getColumnIndex("times")));
                history.setId(cursor.getInt(cursor.getColumnIndex("id")));
            }
        } else {
            history = null;
        }
        return history;
    }

    public int getCount() {
        int cnt = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT * from History where cnt = ?", new String[]{String.valueOf(0)});
        cnt = cursor.getCount();
        database.close();
        cursor.close();
        return cnt;
    }
}
