package net.ariflaksito.riset.apriori;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class ConnectFireBird {
	
	Connection connection = null;
    Statement statement = null;
	
	public ConnectFireBird(){
		
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            connection = DriverManager
                    .getConnection(
                            "jdbc:firebirdsql://localhost:3050//Users/ariflaksito/Documents/DB-TEMP.FDB",
                            "sysdba", "masterkey");
            statement = connection.createStatement();
        }catch (Exception e) {
            e.printStackTrace();
        } 
            
	}
	
}

