package Client;

//--------------------------------------------------------------//
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;

public class Client extends JFrame implements ActionListener{  
 
 String    name,ip="";       
 BufferedReader  reader;           
 PrintStream  writer;
 Socket    sock;
 //顯示區域
 JTextArea   incoming = new JTextArea(15,50); 
 JTextField   outgoing = new JTextField(20);     
 JLabel    jlmane   = new JLabel("input your name：");   
 JTextField   jfname   = new JTextField("",10);  
 
 public static void main(String[] args){
	 Client client = new Client();
 }
 
 
 Client (){ 
        
  super("Client");          
  //用來放mane及ip--設定區域
  JPanel maneipPanel  = new JPanel();   
  //建來設定按鍵 
  JButton setmaneip = new JButton("連線");
  //按下設定
  setmaneip.addActionListener(this);
  //加入到JPanel
  maneipPanel.add(jlmane);
  maneipPanel.add(jfname);         
  maneipPanel.add(setmaneip); 
  //排版BorderLayout設定區域在上方----  
  getContentPane().add(BorderLayout.NORTH,maneipPanel);  
  //JButton("送出")
  JButton sendButton = new JButton("送出");
  //按下
  sendButton.addActionListener(this);       
  //對話區域-----
  //設置為 true，則當行的長度大於所分派的寬度時，將換行
  incoming.setLineWrap(true);         
  //設置為 true，則當行的長度大於所分派的寬度時，將在單詞邊界（空白）處換行
  incoming.setWrapStyleWord(true); 
  //不可編輯的  
  incoming.setEditable(false); 
  //JScrollPane  
  JScrollPane qScroller = new JScrollPane(incoming);
  //垂直滾動  
  qScroller.setVerticalScrollBarPolicy(
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
  //水平滾動
  qScroller.setHorizontalScrollBarPolicy(
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  JPanel mainPanel = new JPanel();       
  mainPanel.add(qScroller);
  mainPanel.add(outgoing);
  mainPanel.add(sendButton);
  //對話區域在中間------
  getContentPane().add(BorderLayout.CENTER,mainPanel);  
  setResizable(false); 
   setSize(600,450);
  setVisible(true);
  //離開 
  addWindowListener(new WindowAdapter()      
  {
   public void windowClosing(WindowEvent e){
    System.exit(0);
   }
  });
 }
 
 //--------------------------------------------------------------//
 //-3-建立連線
 //--------------------------------------------------------------//
 private void EstablishConnection(){
  try{
 
   sock = new Socket(ip,8888);      
   InputStreamReader streamReader =  
     //取得Socket的輸入資料流
      new InputStreamReader(sock.getInputStream());  
   //放入暫存區
   reader = new BufferedReader(streamReader);    
   //取得Socket的輸出資料流
   
   writer = new PrintStream(sock.getOutputStream());
  ;    
   
  }catch(IOException ex ){
	  ex.printStackTrace();
  }
 }
 //--------------------------------------------------------------//
 //-4-接收資料
 //--------------------------------------------------------------//
 public class IncomingReader implements Runnable{
  public void run(){
   String message = null;
   try {
	   while ((message = reader.readLine()) != null){
		   try{
	    	JSONObject jsonmessage = new JSONObject(message);
	    	incoming.append("[ " + jsonmessage.getString("time") + " ] "+ jsonmessage.getString("name")+ ": " +jsonmessage.getString("message")+'\n');
		   }catch(Exception ex ){
			   // history start and end
			   incoming.append(message + "\n");
	   }
	   }
   } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
   }
  }
 } 
 //--------------------------------------------------------------//
 //-5-按下之動作
 //--------------------------------------------------------------//
 public void actionPerformed(ActionEvent e){
  String str=e.getActionCommand();   
  if(str.equals("連線")){
   
   name = jfname.getText();
   ip = "127.0.0.1";
      //建立連線----
   EstablishConnection();          
   
   //send message
   	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String time = dateFormat.format(date);
	Map map = new HashMap();
	map.put("name", "Server");
	map.put("time", time);
	map.put("message", name + " " + "in");
	JSONObject message = new JSONObject(map);
 
   writer.println(message.toString()); 
   //建立接收資料執行緒----
   Thread readerThread = new Thread(new IncomingReader());  
   readerThread.start();
  //按下送出   
  }else if(str.equals("送出")){    
   //不可沒有ip及送出空白
   if((ip!=null)&&(outgoing.getText()!=""))    
   {
    try{//送出資料
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String time = dateFormat.format(date);
    	Map map = new HashMap();
		map.put("name", name);
		map.put("time", time);
		map.put("message", outgoing.getText());
		JSONObject message = new JSONObject(map);

        writer.println(message.toString()); 
     //刷新該串流的緩衝。
     writer.flush();         
    }catch(Exception ex ){
     System.out.println("送出資料失敗");
    }
    outgoing.setText("");        
   }
  }
 }
}