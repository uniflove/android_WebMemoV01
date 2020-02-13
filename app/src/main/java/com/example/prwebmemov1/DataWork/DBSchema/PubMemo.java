package com.example.prwebmemov1.DataWork.DBSchema;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PubMemo implements Serializable {
    public static final String TABLE_NAME = "PubMemo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MEMO = "memo";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CATEGORY1 = "category1";
    public static final String COLUMN_CATEGORY2 = "category2";
    public static final String COLUMN_CATEGORY3 = "category3";
    public static final int INDEX_ID = 1;
    public static final int INDEX_MEMO = 2;
    public static final int INDEX_TITLE = 3;
    public static final int INDEX_CATEGORY1 = 4;
    public static final int INDEX_CATEGORY2 = 5;
    public static final int INDEX_CATEGORY3 = 6;

    public int id;
    public String memo;
    public String title;
    public String category1;
    public String category2;
    public String category3;

    public PubMemo(int id, String memo, String title,
                   String category1, String category2, String category3) {
        this.id = id;
        this.memo = memo;
        this.title = title;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
    }

    public PubMemo(ResultSet rs) throws SQLException {
        this.id = rs.getInt(INDEX_ID);
        this.memo = rs.getString(INDEX_MEMO);
        this.title = rs.getString(INDEX_TITLE);
        this.category1 = rs.getString(INDEX_CATEGORY1);
        this. category2 = rs.getString(INDEX_CATEGORY2);
        this.category3 = rs.getString(INDEX_CATEGORY3);
    }
}
