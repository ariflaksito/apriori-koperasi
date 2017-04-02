package net.ariflaksito.riset.apriori;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectMysql {
	
	Connection connection = null;
    Statement statement = null;
    
    public ConnectMysql(){
    	try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager
                    .getConnection(
                            "jdbc:mysql://localhost:3306/dbpos",
                            "root", "");
            statement = connection.createStatement();
        }catch (Exception e) {
            e.printStackTrace();
        } 
    }

}
