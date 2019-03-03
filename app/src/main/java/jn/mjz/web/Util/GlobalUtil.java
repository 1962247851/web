package jn.mjz.web.Util;

import android.content.Context;
import android.content.SharedPreferences;

import jn.mjz.web.DateBase.CollectionDataBaseHelper;
import jn.mjz.web.DateBase.HistoryDataBaseHelper;

public class GlobalUtil {
    private static GlobalUtil instance;
    public CollectionDataBaseHelper collectionDataBaseHelper;
    public HistoryDataBaseHelper historyDataBaseHelper;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public static GlobalUtil getInstance() {

        if (instance == null) {
            instance = new GlobalUtil();
        }
        return instance;
    }

    public void setContext(Context context) {
        collectionDataBaseHelper = new CollectionDataBaseHelper(context, CollectionDataBaseHelper.TABLE_NAME, null, 1);
        historyDataBaseHelper = new HistoryDataBaseHelper(context,HistoryDataBaseHelper.TABLE_NAME,null,1);
        sharedPreferences = context.getSharedPreferences("user",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

}
