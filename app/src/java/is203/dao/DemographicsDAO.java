package is203.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DemographicsDAO {
    /**
     * Retrieves all email and password from SQL table demographics
     * @return a HashMap with username as key and password as value
     */
   public HashMap<String, String> retrieveAllUserPW() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<String, String> toReturn = new HashMap<String, String>();

        try {
            conn = ConnectionManager.getConnection();
            String query = "select email, password from demographics;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                //take every row and add username to key, and password to value.
                //email = marcus.ong.2014@sis.smu.edu.sg
                String email = rs.getString(1);
                int positionAt = email.indexOf('@');
                String username = email.substring(0, positionAt);
                String password = rs.getString(2);
                toReturn.put(username, password);

            }
            return toReturn;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return null;
    }

   /**
    * Remove everything from table demographics
    */
    public static void removeAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            String query = "truncate table demographics";

            // Prepares the query for entering into SQL
            stmt = conn.prepareStatement(query);

            // Execute query
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
    }

    /**
     * Insert into SQL table demographics with values stored inside ArrayLIst<String[]>
     * @param demo contains String [] containing values to add into demographics
     */
    public static void add(ArrayList<String[]> demo) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionManager.getConnection();
            ConnectionManager.disableCommit(conn);
            String query = "insert into demographics values(?,?,?,?,?,?)";

            String[] headers = demo.get(0);
            int mac = 0;
            int n = 0;
            int gen = 0;
            int e = 0;
            int c = 0;
            int p = 0;
            for (int i = 0; i < headers.length; i++) {
                String column = headers[i];
                switch (column.toLowerCase().trim()) {
                    case "mac-address":
                        mac = i;
                        break;
                    case "name":
                        n = i;
                        break;
                    case "gender":
                        gen = i;
                        break;
                    case "email":
                        e = i;
                        break;
                    case "password":
                        p = i;
                        break;
                    case "cca":
                        c = i;
                        break;
                }
            }

            stmt = conn.prepareStatement(query);

            for (int i = 1; i < demo.size(); i++) {
                String[] str = demo.get(i);
                stmt.setString(1, str[mac]);
                stmt.setString(2, str[n]);
                stmt.setString(3, str[p]);
                stmt.setString(4, str[e]);
                stmt.setString(5, str[gen]);
                stmt.setString(6, str[c]);

                stmt.addBatch();

                if ((i + 1) % 1000 == 0) {
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
     * Retrieves all CCA from SQL
     * @return  an ArrayList of String containing CCAs
     */
    public static ArrayList<String> retrieveAllCCA() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> cca = new ArrayList<>();
        try {
            conn = ConnectionManager.getConnection();
            String query = "select cca from demographics group by cca";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String s = rs.getString(1);
                cca.add(s);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return cca;
    }
    
    /**
     * Retrieve number of rows/entries in demographics
     * @return number of rows/ table size
     */
    public static int retrieveTableSize() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = ConnectionManager.getConnection();
            String query = "select count(*) from demographics";
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
     * Retrieve all mac-address from SQL table demographics
     * @return  a HashMap with mac address as key and 0 as value
     */
    public static HashMap<String, Integer> retrieveAllMacAdd() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        try {
            conn = ConnectionManager.getConnection();
            String query = "select mac_address from demographics";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String mac = rs.getString(1);
                map.put(mac, 0);
            }
        } catch (SQLException e) {
            
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return map;
    }
}
