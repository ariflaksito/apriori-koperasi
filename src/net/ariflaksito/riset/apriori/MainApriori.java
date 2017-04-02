package net.ariflaksito.riset.apriori;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainApriori {
	public static void main(String[] args) throws SQLException, InterruptedException {

		int minTransaksi = 5;
		int minConfidence = 10;
		int totalTransaksi = 0;
		boolean exit = false;
		final long startTime = System.nanoTime();

		ConnectFireBird fb = new ConnectFireBird();
		//ConnectMysql fb = new ConnectMysql();
		ResultSet rs = fb.statement
				.executeQuery("select count(kdnota) as jml from jual");

		while (rs.next()) {
			totalTransaksi = rs.getInt("jml");
		}
		rs.close();

		DatabaseMetaData dbm = fb.connection.getMetaData();
		ResultSet table = dbm.getTables(null, null, "c1", null);
		if (table.next()) {
			fb.statement.executeUpdate("drop table c1");
			System.out.println("drop table c1...[ok]");

		}
		table.close();

		fb.statement.executeUpdate("create table c1(item1 varchar(20), jml integer)");
		System.out.println("create table c1...[ok]");

		fb.statement.executeUpdate("insert into c1 (item1, jml) select i.kdbarang, count(kdnota) "
				+ "from itemjual i, jual j where i.kdnota = j.kdnota and j.tgljual between '2016-01-01 and '2016-01-31' "
				+ "group by i.kdbarang having count(kdnota) > "
				+ minTransaksi);
		System.out.println("insert data to table c1...[ok]");
		System.out.println("------------------------------");

		int c = 1;
		while (!exit) {
			c += 1;

			ResultSet tables = dbm.getTables(null, null, "c" + c, null);
			if (tables.next()) {
				fb.statement.executeUpdate("drop table c" + c);
				System.out.println("drop table c" + c + "...[ok]");
			}
			tables.close();

			String q = "create table c" + c + "(";
			for (int i = 1; i <= c; i++)
				q += "item" + i + " varchar(20),";
			q += "jml integer)";
			fb.statement.executeUpdate(q);

			System.out.println("create table c" + c + "...[ok]");

			/* ----- ambil item c2, dst ----- */

			String q1 = "insert into c" + c;
			q1 += " select distinct ";
			for (int i = 1; i <= (c - 1); i++)
				q1 += "p.item" + i + ",";

			q1 += " q.item" + (c - 1) + ",0";
			q1 += " from c" + (c - 1) + " p, c" + (c - 1) + " q";
			q1 += " where q.item" + (c - 1) + " > p.item" + (c - 1);

			for (int i = 2; i <= (c - 1); i++)
				q1 += " and p.item" + i + " > p.item" + (i - 1);

			q1 += " order by ";
			for (int i = 1; i <= (c - 1); i++)
				q1 += "p.item" + i + ",";

			q1 += "q.item" + (c - 1);

			fb.statement.executeUpdate(q1);
			System.out.println("insert data to table c" + c + "...[ok]");

			String qz = "select * from c" + c;
			ResultSet rsc = fb.statement.executeQuery(qz);

			List<String> queryList = new ArrayList<String>();
			while (rsc.next()) {
				String in = "";
				for (int i = 1; i <= c; i++) {
					in += (i > 1) ? "," : "";
					in += "'" + rsc.getString("item" + i) + "'";
				}

				String q2 = "update c" + c + " set jml = (select count(*) from jual j, itemjual i "
						+ "where j.kdnota = i.kdnota and i.kdbarang in (" + in + ")) "
						+ " where ";

				for (int i = 1; i <= c; i++) {
					q2 += (i > 1) ? "and " : "";
					q2 += "item" + i + " ='" + rsc.getString("item" + i) + "'";
				}

				queryList.add(q2);
			}

			rsc.close();

			int x = 1;
			int total = queryList.size();
			long start = System.currentTimeMillis();
			System.out.println("Update table c "+c+"...");
			
			for (String query : queryList) {
				
				//System.out.println(query);
				
				fb.statement.executeUpdate(query);
				ProgressBar.printProgress(start, total, x);
				x++;
			}
			
			System.out.println("");
			System.out.println("");
			fb.statement.executeUpdate("delete from c" + c + " where jml <=" + minTransaksi);

			System.out.println("delete row where <= min transaksi...[ok]");
			System.out.println("");

			ResultSet rs1 = fb.statement.executeQuery("select count(*) as jml from c" + c);
			while (rs1.next()) {
				if (rs1.getInt("jml") == 0)
					exit = true;
			}

			rs1.close();

		}

		System.out.println("-------------------------");
		System.out.println("Exit looping c1,c2,dst...");

		fb.statement.close();

		final long duration = System.nanoTime() - startTime;

		System.out.println("Execution time : " + duration / 1000000000 + " sec");

	}
}