/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class AppDAO {

    /**
     * Remove all existing data inside SQL table App
     */
    public static void removeAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            String query = "truncate table app";
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Adds data into SQL table app
     * @param app An ArrayList of String [] to add into SQL table App
     */
    public static void add(ArrayList<String[]> app) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            String query = "insert into app values(?,?,?,?)";
            stmt = conn.prepareStatement(query);
            String[] headers = app.get(0);
            int ts = 0;
            int mac = 0;
            int a_id = 0;
            for (int i = 0; i < headers.length; i++) {
                String column = headers[i];
                switch (column.toLowerCase().trim()) {
                    case "app-id":
                        a_id = i;
                        break;
                    case "timestamp":
                        ts = i;
                        break;
                    case "mac-address":
                        mac = i;
                        break;
                }
            }
            for (int i = 1; i < app.size(); i++) {
                String[] str = app.get(i);
                stmt.setString(1, str[0]);
                stmt.setString(2, str[ts]);
                stmt.setString(3, str[mac]);
                stmt.setString(4, str[a_id]);

                stmt.addBatch();

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /** 
     * Insert a column called sn into SQL table App
     */
    public static void insertSn() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            stmt = conn.prepareStatement("Alter table app ADD column sn integer default null first");
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Delete column sn from SQL table app
     */
    public static void deleteSn() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            stmt = conn.prepareStatement("Alter table app drop column sn");
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Drop primary key from table App
     */
    public static void dropPrimaryKey() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            stmt = conn.prepareStatement("Alter table app drop PRIMARY KEY");
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Insert primary key to table App
     */
    public static void insertPrimaryKey() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            stmt = conn.prepareStatement("Alter table app add constraint PK PRIMARY KEY (timestamp, mac_address)");
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt);
        }
    }

    /**
     * Checks for duplicate rows within table by returning duplicate rows
     * Duplicate rows will be added into error list and returned
     * @param appErrors contains row number as key and error message as String
     * @return a TreeMap with row number as key and error message as String
     */
    public static TreeMap<Integer, String> checkDuplicateRows(TreeMap<Integer, String> appErrors) {
        String deleteThese = "(";
        ArrayList<String> duplicateList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select timestamp, mac_address, count(*) count, group_concat(sn order by sn asc) as line from app group by 1,2 having count > 1;");
            rs = stmt.executeQuery();

            while (rs.next()) {
                String lines = rs.getString("line");
                duplicateList.add(lines);
            }
            if (duplicateList.size() > 0) {
                Iterator<String> duplicates = duplicateList.iterator();
                while (duplicates.hasNext()) {
                    String line = duplicates.next();
                    String[] duplicateRows = line.split(",");
                    for (int i = 0; i < duplicateRows.length; i++) {
                        if (i != duplicateRows.length - 1) {
                            deleteThese += duplicateRows[i] + ",";
                            int row = Integer.parseInt(duplicateRows[i]);
                            appErrors.put(row, "Duplicate Row");
                        }
                    }
                }
                deleteThese = deleteThese.substring(0, deleteThese.length() - 1);
                deleteThese += ")";
            }
            if (deleteThese.length() > 2) {
                deleteDuplicates(deleteThese);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return appErrors;
    }

    /**
     * Delete duplicate rows from SQL table app
     * @param line row number of duplicate row
     */
    public static void deleteDuplicates(String line) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            stmt = conn.prepareStatement("delete from app where sn in " + line);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Given additional file upload, check in SQL whether the lines exist already
     * If it is a duplicate, add into TreeMap of errors
     * @param check contains data to check in SQL before adding
     * @return a TreeMap of errors with row number as key and error message as value
     */
    public static TreeMap<Integer, String> checkUploadAddDuplicates(TreeMap<Integer, String[]> check) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        TreeMap<Integer, String> errors = new TreeMap<Integer, String>();
        try {
            conn = ConnectionManager.getConnection();
            String query = "select timestamp, mac_address from app";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            ArrayList<String> database = new ArrayList<>();

            while (rs.next()) {
                String time = rs.getString(1).trim();
                String mac = rs.getString(2).trim();
                String s = time + ", " + mac;
                database.add(s);
            }

            Iterator<Integer> iter = check.keySet().iterator();
            String[] headers = new String[4];
            if (iter.hasNext()) {
                int key = iter.next();
                headers = check.get(key);
            }
            
            int ts = 0;
            int mac = 0;
            int a_id = 0;
            for (int i = 0; i < headers.length; i++) {
                String column = headers[i];
                switch (column.toLowerCase().trim()) {
                    case "app-id":
                        a_id = i;
                        break;
                    case "timestamp":
                        ts = i;
                        break;
                    case "mac-address":
                        mac = i;
                        break;
                }
            }
            while (iter.hasNext()) {
                int key = iter.next();
                String[] toCheck = check.get(key);
                String checkThis = toCheck[ts] + ", " + toCheck[mac];
                if (database.contains(checkThis)) {
                    errors.put(key, "Duplicate row");
                    iter.remove();
                }
            }
            TreeMap<Integer, String> moreErrors = handleAppUpload(check);

            if (moreErrors != null && moreErrors.size() != 0) {
                TreeMap<Integer, String> temp = new TreeMap<Integer, String>();
                temp.putAll(moreErrors);
                temp.putAll(errors);
                errors = temp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return errors;
        
    }

    /**
     * Adds data from file into SQL table and returns error messages should there be any
     * @param check TreeMap of data to be added into SQL table app
     * @return a TreeMap of rows as key and error message as value
     */
    public static TreeMap<Integer, String> handleAppUpload(TreeMap<Integer, String[]> check) {
        TreeMap<Integer, String> errorMsg = new TreeMap<Integer, String>();
        ArrayList<String[]> addList = new ArrayList<>();

        Iterator<Integer> iter = check.keySet().iterator();
        if (iter.hasNext()) {
            // Adds the header row
            String[] header = check.get(iter.next());
            String[] newHeader = new String[] {" ", header[0], header[1], header[2]};
            addList.add(newHeader);
        }
        while (iter.hasNext()) {
            int row = iter.next();
            String[] value = check.get(row);
            String[] newValue = new String[] {row + "", value[0], value[1], value[2]};
            addList.add(newValue);
        }

        insertSn();
        dropPrimaryKey();
        add(addList);

        errorMsg = checkDuplicateRows(errorMsg);
        insertPrimaryKey();
        deleteSn();

        return errorMsg;
    }
    
    /**
     * Counts table size/ number of data inside SQL table app
     * @return table size/ number of data
     */
    public static int retrieveTableSize() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = ConnectionManager.getConnection();
            String query = "select count(*) from app";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return count;
    }
}
