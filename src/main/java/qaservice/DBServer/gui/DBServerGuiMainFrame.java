package qaservice.DBServer.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import qaservice.DBServer.keys.KeysOperation;
import qaservice.DBServer.main.DBServerMain;
import qaservice.DBServer.util.DBServerPropReader;

public class DBServerGuiMainFrame  extends JFrame implements ActionListener {
	private static final long serialVersionUID = 5204151284762323868L;
	private static final String START_BUTTON = "DB Server Start";
	private static final String STOP_BUTTON = "DB Server Stop";
	private static final String TASK_ACTION = "TaskAction";
	private static final String TASK_STOP_ACTION = "TaskStopAction";
	private static final String DISPLAY_TIMER = "DisplayTimer";
	private static final String LAMP_OFF = "○";
	private static final String LAMP_ON = "●";
	private static final Color SERVER_ON = Color.GREEN;
	private static final Color SERVER_OFF = Color.RED;
	private static final int FIRST_STAGE = 0;
	private static final int SECOND_STAGE = 1;
	private static final int THIRD_STAGE = 2;
	private static final int DBSERVER_STAGE = 3;
	private static int CURRENT_TASK_STAGE = 0;
	private static int PORT = 0;
	
	private JPanel labelPanel_;
	private JPanel buttonPanel_;
	private JPanel serverStatePanel_;
	private JLabel label_;
	private JLabel serverState_left_;
	private JLabel serverState_center_;
	private JLabel serverState_right_;
	private JButton startButton_;
	private JButton stopButton_;
	private JTextArea txtArea_;
	
	private JButton hiddenTaskButton_;
	private JButton hiddenStopTaskButton_;
	private Timer serverStateDisplayTimer_;
	
	private String lineSeparator_ = System.lineSeparator();

	public DBServerGuiMainFrame() {
		super("hello statistics world = Server");
		buttonPanel_ = new JPanel();
		labelPanel_ = new JPanel();
		serverStatePanel_ = new JPanel();
		label_ = new JLabel("+++ Local DB Server Action Button +++");
		serverState_left_ = new JLabel(LAMP_OFF);
		serverState_center_ = new JLabel(LAMP_OFF);
		serverState_right_ = new JLabel(LAMP_OFF);
		//serverState_.setForeground(SERVER_OFF);
		labelPanel_.add(label_);
		serverStatePanel_.add(serverState_left_);
		serverStatePanel_.add(serverState_center_);
		serverStatePanel_.add(serverState_right_);
		serverStatePanel_.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		startButton_ = new JButton(START_BUTTON);
		stopButton_ = new JButton(STOP_BUTTON);
		settingButtonAction(startButton_);
		settingButtonAction(stopButton_);
		stopButton_.setEnabled(false);
		
		buttonPanel_.add(startButton_);
		buttonPanel_.add(stopButton_);
		buttonPanel_.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		txtArea_ = new JTextArea(10,30);
//		GuiConsoleOperation.setConsoleArea(txtArea_);
		
		//timer_ = new Timer(100, this);
		//timer_.setActionCommand(TIMER_TASK);
		hiddenTaskButton_ = new JButton();
		hiddenTaskButton_.setActionCommand(TASK_ACTION);
		settingButtonAction(hiddenTaskButton_);
		
		hiddenStopTaskButton_ = new JButton();
		hiddenStopTaskButton_.setActionCommand(TASK_STOP_ACTION);
		settingButtonAction(hiddenStopTaskButton_);
		
		
		this.getContentPane().add(labelPanel_);
		this.getContentPane().add(buttonPanel_);
		this.getContentPane().add(txtArea_);
		this.getContentPane().add(serverStatePanel_);
		this.pack();
		
		Path path = Paths.get("conf/img/computer_server_chara1_normal.png");
		if(Files.exists(path)) {
			ImageIcon icon = new ImageIcon(path.toString());
			setIconImage(icon.getImage());
		}
		
		serverStateDisplayTimer_ = new Timer(2000, this);
		serverStateDisplayTimer_.setActionCommand(DISPLAY_TIMER);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout());
		setSize(400, 400);
		
		stopServerState();
	}

	public void start() {
		setVisible(true);
	}

	private void settingButtonAction(JButton bt) {
		bt.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		progressServerState();
		switch(e.getActionCommand()) {
			case START_BUTTON: {
				guiConsoleClear();
				startButton_.setEnabled(false);
				try {
					new DBServerMain().main(null);
				} catch(Exception exeception) {
					exeception.printStackTrace();
					stopServerState();
					startButton_.setEnabled(true);
					break;
				}
				startServerState();
				stopButton_.setEnabled(true);
				break;
			}
			case STOP_BUTTON: {
				stopButton_.setEnabled(false);
				serverStop();
				stopServerState();
				startButton_.setEnabled(true);
				break;
			}
		}
		
	}

	private void serverStop() {
		guiConsoleOut("DB server stop start...");
		PrivateKey privateKey = KeysOperation.createPrivateKeyFromByteData();
		byte[] signData = KeysOperation.sign(privateKey, "CLOSE".getBytes());
		int port = Integer.parseInt(DBServerPropReader.getProperties("serverStopOpratePort").toString());
		try(Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", port));
			try(OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream()) {
				os.write(signData);
				os.flush();
			}
			guiConsoleOut("DB server stopped!!!");
		} catch(IOException e) {
			guiConsoleOut(e.getMessage());
		}
	}

	public void guiConsoleClear() {
		txtArea_.setText("");
	}

	public void guiConsoleOut(String text) {
		txtArea_.append(text);
		txtArea_.append(lineSeparator_);
	}

	private void offServerState() {
		serverState_left_.setText(LAMP_OFF);
		serverState_left_.setForeground(Color.BLACK);
		serverState_center_.setText(LAMP_OFF);
		serverState_center_.setForeground(Color.BLACK);
		serverState_right_.setText(LAMP_OFF);
		serverState_right_.setForeground(Color.BLACK);
	}

	private void startServerState() {
		offServerState();
		serverState_left_.setText(LAMP_ON);
		serverState_left_.setForeground(Color.GREEN);
	}

	private void progressServerState() {
		offServerState();
		serverState_center_.setText(LAMP_ON);
		serverState_center_.setForeground(Color.YELLOW);
	}

	private void stopServerState() {
		offServerState();
		serverState_right_.setText(LAMP_ON);
		serverState_right_.setForeground(Color.RED);
	}
}
