package fpt.edu.vn.assigment_travelapp.ui.explore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ChatHistory.db";
    private static final int DATABASE_VERSION = 1;

    public static final class ChatHistoryEntry {
        public static final String TABLE_NAME = "chat_history";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_IS_USER = "is_user";
    }

    public ChatHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CHAT_HISTORY_TABLE = "CREATE TABLE " +
                ChatHistoryEntry.TABLE_NAME + " (" +
                ChatHistoryEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChatHistoryEntry.COLUMN_MESSAGE + " TEXT NOT NULL, " +
                ChatHistoryEntry.COLUMN_IS_USER + " INTEGER NOT NULL);";
        db.execSQL(SQL_CREATE_CHAT_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ChatHistoryEntry.TABLE_NAME);
        onCreate(db);
    }

    public void addMessage(ChatMessage message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ChatHistoryEntry.COLUMN_MESSAGE, message.getMessage());
        values.put(ChatHistoryEntry.COLUMN_IS_USER, message.isUser() ? 1 : 0);
        db.insert(ChatHistoryEntry.TABLE_NAME, null, values);
        db.close();
    }

    public List<ChatMessage> getAllMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ChatHistoryEntry.TABLE_NAME + " ORDER BY " + ChatHistoryEntry.COLUMN_ID + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(cursor.getColumnIndexOrThrow(ChatHistoryEntry.COLUMN_MESSAGE));
                boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow(ChatHistoryEntry.COLUMN_IS_USER)) == 1;
                messages.add(new ChatMessage(message, isUser));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ChatHistoryEntry.TABLE_NAME, null, null);
        db.close();
    }
}
