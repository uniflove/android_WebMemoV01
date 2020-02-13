package com.example.prwebmemov1.Feature;

import android.os.Handler;

import com.example.prwebmemov1.DataWork.DBSchema.PubMemo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YimSB-i5 on 2020-02-04
 * work/2016/03_EnglishHelper/android/PrEnglishHelperV1 에 있는 SQLTask 클래스가 베이스
 *
 *   getConncetion() = 기본 DB 연결
 *   private Methods = 하나의 SQL 문으로 수행되는거..
 *   public Methods = 여러개의 micro methods 로 하나의 실제 작업을 수행함.
 */
public class SQLTask {
    // Server Info
    private static String SERVER_IP;
    private static String DB_NAME;
    private static String USER;
    private static String PASSWORD;

    // enum 으로 할수도..
    public static final int MSG_ERROR = -1;
    public static final int MSG_GET_ALL = 1;
    public static final int MSG_INSERT_MEMO = 2;
    public static final int MSG_UPDATE_MEMO = 3;
    public static final int MSG_DELETE_MEMO = 4;
    public static final int MSG_SEARCH_MEMO = 5;

    public static void setLoginInfo(String server, String db, String user, String password) {
        SERVER_IP = server;
        DB_NAME = db;
        USER = user;
        PASSWORD = password;
    }

    /**
     * 하나의 작업을 할때 보통 .
     * getConnection, close(), try{} catch{} finally{}, handler for result message
     * 이런 것들이 공통적으로 들어가서.. 코드 좀 간결하게 하려고 통합시킨것..
     */
    private static abstract class SQLRunnable implements Runnable {
        private Handler handler;
        private int msg;

        public SQLRunnable(Handler handler, int msg) {
            this.handler = handler;
            this.msg = msg;
        }

        @Override
        public void run() {
            Connection conn = null;
            try {
                conn = getConnection();
                Object obj = work(conn);
                handler.obtainMessage(msg, obj).sendToTarget();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                handler.obtainMessage(MSG_ERROR).sendToTarget();
            } finally {
                if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
            }
        }

        public abstract Object work(Connection conn) throws SQLException;
    }

    // private  methods...................................
    /**
     * SQL Server 로그인
     * @return  연결된 커넥션
     */
    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        String connectionURL = String.format("jdbc:jtds:sqlserver://%s;databaseName=%s;user=%s;password=%s;",
                SERVER_IP, DB_NAME, USER, PASSWORD);

        return DriverManager.getConnection(connectionURL);
    }

    private static List<PubMemo> selectAllRecord(Connection conn) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            String sql = String.format("SELECT * FROM %s", PubMemo.TABLE_NAME);
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            List<PubMemo> list = new ArrayList<>();
            while(rs.next()) { list.add(new PubMemo(rs)); }

            return list;
        } finally {
            if(rs != null) { try { rs.close(); } catch (SQLException e) { e.printStackTrace(); } }
            if(st != null) { try { st.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private static void insertMemo(Connection conn, PubMemo src) throws SQLException {
        PreparedStatement pst = null;
        try {
            String sql = String.format("INSERT INTO %s(%s,%s,%s,%s,%s) values(?, ?, ?, ?, ?)",
                    PubMemo.TABLE_NAME, PubMemo.COLUMN_MEMO, PubMemo.COLUMN_TITLE, PubMemo.COLUMN_CATEGORY1, PubMemo.COLUMN_CATEGORY2, PubMemo.COLUMN_CATEGORY3);
            pst = conn.prepareStatement(sql);
            // 여기 들어가는 숫자가 테이블에서의 인덱스가 아니고 그냥 저 위에 sql 문에서의 index 라고 봐야함
            // 그래서 테이블 인덱스랑 맞지가 않음.. ID 때문에.
            pst.setString(1, src.memo);
            pst.setString(2, src.title);
            pst.setString(3, src.category1);
            pst.setString(4, src.category2);
            pst.setString(5, src.category3);

            pst.executeUpdate();
        } finally {
            if (pst != null) { try {pst.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private static int updateMemo(Connection conn, PubMemo src) throws SQLException {
        PreparedStatement pst = null;
        try {
            String sql = String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?",
                    PubMemo.TABLE_NAME, PubMemo.COLUMN_MEMO, PubMemo.COLUMN_TITLE, PubMemo.COLUMN_CATEGORY1, PubMemo.COLUMN_CATEGORY2, PubMemo.COLUMN_CATEGORY3, PubMemo.COLUMN_ID);
            pst = conn.prepareStatement(sql);
            pst.setString(1, src.memo);
            pst.setString(2, src.title);
            pst.setString(3, src.category1);
            pst.setString(4, src.category2);
            pst.setString(5, src.category3);
            pst.setInt(6, src.id);

            return pst.executeUpdate();
        } finally {
            if (pst != null) { try {pst.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private static int deleteMemo(Connection conn, PubMemo src) throws SQLException {
        PreparedStatement pst = null;
        try {
            String sql = String.format("DELETE FROM %s WHERE %s = ?", PubMemo.TABLE_NAME,
                    PubMemo.COLUMN_ID);
            pst = conn.prepareStatement(sql);
            pst.setInt(1, src.id);

            return pst.executeUpdate();
        } finally {
            if (pst != null) { try {pst.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private static List<PubMemo> search(Connection conn, String keyword, boolean[] option) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            int count = 0;
            String sqlKeyword = String.format("%%%s%%", keyword);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT TOP 100 * FROM %s WHERE ", PubMemo.TABLE_NAME));
            if(option[1]) { // search for all
                sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_MEMO));
                sb.append(String.format("OR (%s LIKE ?) ", PubMemo.COLUMN_TITLE));
                sb.append(String.format("OR (%s LIKE ?) ", PubMemo.COLUMN_CATEGORY1));
                sb.append(String.format("OR (%s LIKE ?) ", PubMemo.COLUMN_CATEGORY2));
                sb.append(String.format("OR (%s LIKE ?) ", PubMemo.COLUMN_CATEGORY3));
                String sql = sb.toString();
                pst = conn.prepareStatement(sql);
                pst.setString(1, sqlKeyword);
                pst.setString(2, sqlKeyword);
                pst.setString(3, sqlKeyword);
                pst.setString(4, sqlKeyword);
                pst.setString(5, sqlKeyword);
            } else {
                if (option[PubMemo.INDEX_MEMO]){
                    if(count++ > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_MEMO));
                }
                if (option[PubMemo.INDEX_TITLE]){
                    if(count++ > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_TITLE));
                }
                if (option[PubMemo.INDEX_CATEGORY1]) {
                    if (count++ > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_CATEGORY1));
                }
                if (option[PubMemo.INDEX_CATEGORY2]) {
                    if (count++ > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_CATEGORY2));
                }
                if (option[PubMemo.INDEX_CATEGORY3]) {
                    if (count++ > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(String.format("(%s LIKE ?) ", PubMemo.COLUMN_CATEGORY3));
                }
                String sql = sb.toString();
                pst = conn.prepareStatement(sql);
                for(; count>0; count--) {
                    pst.setString(count, sqlKeyword);
                }
            }

            rs = pst.executeQuery();
            List<PubMemo> list = new ArrayList<>();
            while(rs.next()) { list.add(new PubMemo(rs)); }

            return list;
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException e) { e.printStackTrace(); } }
            if (pst != null) { try {pst.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    // public methods...................................
    public static void GetAllRecord(Handler handler) {
        new Thread(new SQLRunnable(handler, MSG_GET_ALL) {
            @Override
            public Object work(Connection conn) throws SQLException {
                return selectAllRecord(conn);
            }
        }).start();
    }

    public static void InsertMemo(Handler handler, final PubMemo src) {
        new Thread(new SQLRunnable(handler, MSG_INSERT_MEMO) {
            @Override
            public Object work(Connection conn) throws SQLException {
                insertMemo(conn, src);
                return null;
            }
        }).start();
    }

    public static void UpdateMemo(Handler handler, final PubMemo src) {
        new Thread(new SQLRunnable(handler, MSG_UPDATE_MEMO) {
            @Override
            public Object work(Connection conn) throws SQLException {
                updateMemo(conn, src);
                return null;
            }
        }).start();
    }

    public static void DeleteMemo(Handler handler, final PubMemo src) {
        new Thread(new SQLRunnable(handler, MSG_DELETE_MEMO) {
            @Override
            public Object work(Connection conn) throws SQLException {
                deleteMemo(conn, src);
                return null;
            }
        }).start();
    }

    public static void Search(Handler handler, final String keyword, final boolean[] option) {
        new Thread(new SQLRunnable(handler, MSG_SEARCH_MEMO) {
            @Override
            public Object work(Connection conn) throws SQLException {
                return search(conn, keyword, option);
            }
        }).start();
    }

}
