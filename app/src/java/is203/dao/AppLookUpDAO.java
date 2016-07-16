/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AppLookUpDAO {

    /**
     * Removes everythign inside applookup table in phpMyAdmin
     */
    public static void removeAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            String query = "truncate table applookup";
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Given an arraylist of string[] that contains everything from a result
     * set, insert everything into SQL table applookup
     * @param appLookUp 
     */
    public static void add(ArrayList<String[]> appLookUp) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            String query = "insert into applookup values(?,?,?)";
            stmt = conn.prepareStatement(query);
            String[] headers = appLookUp.get(0);
            int a_id = 0;
            int a_n = 0;
            int a_c = 0;
            for (int i = 0; i < headers.length; i++) {
                String column = headers[i];
                switch (column.toLowerCase().trim()) {
                    case "app-id":
                        a_id = i;
                        break;
                    case "app-name":
                        a_n = i;
                        break;
                    case "app-category":
                        a_c = i;
                        break;
                }
            }
            for (int i = 1; i < appLookUp.size(); i++) {
                String[] str = appLookUp.get(i);
                stmt.setString(1, str[a_id]);
                stmt.setString(2, str[a_n]);
                stmt.setString(3, str[a_c]);

                stmt.addBatch();

                if (i % 10000 == 0) {
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
     * Retrieve from SQL table applookup the number of apps/ table size
     * @return the number of apps in applookup
     */
    public static int retrieveTableSize() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = ConnectionManager.getConnection();
            String query = "select count(*) from applookup";
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
    
    /**
     * Get from SQL table applookup all the app ids
     * @return a hashmap with app-id as the key and 0 as the value
     */
    public static HashMap<Integer, Integer> retrieveAllAppId() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        try {
            conn = ConnectionManager.getConnection();
            String query = "select app_id from applookup";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                map.put(rs.getInt(1), 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return map;
    }
}
