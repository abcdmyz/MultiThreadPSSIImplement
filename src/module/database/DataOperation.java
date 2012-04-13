package module.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

public class DataOperation
{
	public static int selectData( Connection connection, int[] kseqSet )
	{
		int average = 0;
		int kseq, kval;
		
		PreparedStatement pstmt;
		ResultSet resultset;
		
		try
		{
			pstmt = connection.prepareStatement ("SELECT kval FROM bench WHERE kseq= ? ;");
			
			for ( int i=0; i<kseqSet.length; i++ )
			{
				pstmt.setInt(1, kseqSet[i]);
				resultset = pstmt.executeQuery();
				
				if ( resultset.next() )
				{
					kval = resultset.getInt("kval");
					average += kval;
					
					//System.out.println("kseq " + kseqSet[i] + " kval " + kval);
				}
			}
			
			pstmt.close();
			
		}
		catch( SQLException ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return average/kseqSet.length;
	}
	
	public static void updataData( Connection connection, int[] kseqSet, double fraction )
	{
		PreparedStatement pstmt;
		
		ResultSet resultset;
		int kseq, kval;	
		
		try
		{	
			pstmt = connection.prepareStatement ("UPDATE bench set kval=kval+"+fraction+" WHERE kseq = ?");
			
			for ( int i=0; i<kseqSet.length; i++ )
			{
				pstmt.setInt(1, kseqSet[i]);
				pstmt.executeUpdate();
			}
			
			pstmt.close();		
		}
		
		catch (SQLException ex)
		{
			System.err.println(ex.getMessage());
		}
	}
	
	public static void updataARow( Connection connection, int kseq, double fraction )
	{
		PreparedStatement pstmt;
		
		ResultSet resultset;	
		
		try
		{	
			pstmt = connection.prepareStatement ("UPDATE bench set kval=kval+"+fraction+" WHERE kseq = ?");
		
			pstmt.setInt(1, kseq);
			pstmt.executeUpdate();
			
			pstmt.close();		
		}
		
		catch (SQLException ex)
		{
			System.err.println(ex.getMessage());
		}
	}
	
	public static int selectARow( Connection connection, int kseq)
	{
		PreparedStatement pstmt;
		ResultSet resultset;	
		int kval = 0;
		
		try
		{	
			pstmt = connection.prepareStatement ("SELECT kval FROM bench WHERE kseq = ? ;");
		
			pstmt.setInt(1, kseq);
			resultset = pstmt.executeQuery();
			
			if ( resultset.next() )
			{
				kval = resultset.getInt("kval");
			}
			
			pstmt.close();
			
			
		}
		
		
		
		catch (SQLException ex)
		{
			System.err.println(ex.getMessage());
		}
		
		return kval;
	}
}
