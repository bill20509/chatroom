
package Server;
import java.io.*;




import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import org.json.JSONObject;
public class Server{
 
 Vector output;//output
 Connection connection;
 Statement statement;
 
 public static void main (String args[]){
  new Server().go();     
 }
 
 public void go() {
  output = new Vector();          
  try{
   ServerSocket serverSock = new ServerSocket(8888); 
   
   // create a database connection
   Class.forName("org.sqlite.JDBC");
   connection = DriverManager.getConnection("jdbc:sqlite:history.db");
   statement = connection.createStatement();
   statement.setQueryTimeout(30);  // set timeout to 30 sec.
   
   
   statement.executeUpdate("drop table if exists history");
   statement.executeUpdate ("create table history (time string,name string,message string)");
  
  
   while(true){
    //等待連線的請求--串流
    Socket cSocket = serverSock.accept();    
    //建立I/O管道
    PrintStream writer = 
     //取得Socket的輸出資料流
     new PrintStream(cSocket.getOutputStream());  
    System.out.println(writer); 
    //元件加入Vector
    output.add(writer);         
    //傳入一個Runnable物件並分派一個新的執行緒
    //建立伺服器主執行緒
    Thread t = new Thread(new Process(cSocket)); 
    //啟動執行緒
    t.start();           
    //取得連線的ip       
    System.out.println(cSocket.getLocalSocketAddress()+ 
    //執行緒的在線次數
         "有"+(t.activeCount()-1)+  
    //顯示連線人次
         "個連接");               
   } 
  }catch(Exception ex){
	  ex.printStackTrace();
	  }
 }
 //--------------------------------------------------------------//
 //-3-Process處理程序
 //--------------------------------------------------------------//
 public class Process implements Runnable{   
  //暫存資料的Buffered
  BufferedReader reader;  
  //建立一個Socket變數  
  Socket sock;            
  
  public Process(Socket cSocket)
  {
   try{
    sock = cSocket;
    //取得Socket的輸入資料流
    InputStreamReader isReader =        
    new InputStreamReader(sock.getInputStream()); 
    //取得Socket的輸出資料流
    PrintStream writer = 
    new PrintStream(cSocket.getOutputStream());  
    
    // history 
    ResultSet rs = statement.executeQuery("select * from history");
    
    writer.println("----------History Start----------"); 
    //刷新該串流的緩衝。
    writer.flush();           
    
    while(rs.next())
    {
    	Map map = new HashMap();
		map.put("name", rs.getString("name"));
		map.put("time", rs.getString("time"));
		map.put("message", rs.getString("message"));
		JSONObject jsonmessage = new JSONObject(map);
		
		writer.println(jsonmessage.toString()); 
	    writer.flush();
		
    }      
    
    writer.println("----------History End----------"); 
    //刷新該串流的緩衝。
    writer.flush();           
    
    reader = new BufferedReader(isReader);
   }catch(Exception ex){
    System.out.println("連接失敗Process");
   } 
  }
  //--------------------------------------------------------------//
  //-3.2-執行執行緒
  //--------------------------------------------------------------//
  public void run(){
   String message;
   try{
       //讀取資料
    while ((message = reader.readLine())!=null){   
          
     JSONObject json = new JSONObject(message);
     statement.execute("insert into history values('" + json.getString("time") + "', '" + json.getString("name") + "' , '" + json.getString("message") + "')");
     tellApiece(message);
    }
   }catch(Exception ex){System.out.println("有一個連接離開");}
  }
  //--------------------------------------------------------------//
  //-3.3-告訴每人
  //--------------------------------------------------------------//
  public void tellApiece(String message){
   //產生iterator可以存取集合內的元素資料    
   Iterator it = output.iterator(); 
   //向下讀取元件   
   while(it.hasNext()){          
    try{
    //取集合內資料
    PrintStream writer = (PrintStream) it.next();  
    //印出
    writer.println(message); 
    //刷新該串流的緩衝。
    writer.flush();           
    }
    catch(Exception ex){
     System.out.println("連接失敗Process");
    }
   }
  }
 } 
}