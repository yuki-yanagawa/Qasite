package qaservice.WebServer.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.h2.command.query.SelectListColumnResolver;

import qaservice.DBServer.main.DBServerMain;
import qaservice.WebServer.dbconnect.DBConnectionOperation;
import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.ServerOperator;
import qaservice.WebServer.mainserver.taskhandle.http.FileReadCasheClear;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class GuiMainFrame extends JFrame implements ActionListener{
	private static final long serialVersionUID = -6490896595137909354L;

	private static final String START_BUTTON = "Server Start";
	private static final String STOP_BUTTON = "Server Stop";
	private static final String TASK_ACTION = "TaskAction";
	private static final String TASK_STOP_ACTION = "TaskStopAction";
	private static final String DISPLAY_TIMER = "DisplayTimer";
	private static final String SERVER_LAMP = "●";
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
	private JPanel progressBarPanel_;
	private JPanel serverStatePanel_;
	private JLabel label_;
	private JLabel serverState_;
	private JTextField txtField_;
	private JButton startButton_;
	private JButton stopButton_;
	private JCheckBox openBrowserCheck_;
	private JTextArea txtArea_;
	private JProgressBar progressBar_;
	
	private JButton hiddenTaskButton_;
	private JButton hiddenStopTaskButton_;
	private Timer serverStateDisplayTimer_;
	

	public GuiMainFrame() {
		super("hello statistics world = Server");
		buttonPanel_ = new JPanel();
		labelPanel_ = new JPanel();
		progressBarPanel_ = new JPanel();
		serverStatePanel_ = new JPanel();
		label_ = new JLabel("+++ Local Server Action Button +++ => prot No : ");
		serverState_ = new JLabel(SERVER_LAMP);
		serverState_.setForeground(SERVER_OFF);
		txtField_ = new JTextField(5);
		txtField_.setText("9090");
		labelPanel_.add(label_);
		labelPanel_.add(txtField_);
		serverStatePanel_.add(serverState_);
		startButton_ = new JButton(START_BUTTON);
		stopButton_ = new JButton(STOP_BUTTON);
		settingButtonAction(startButton_);
		settingButtonAction(stopButton_);
		stopButton_.setEnabled(false);
		progressBar_ = new JProgressBar(JProgressBar.HORIZONTAL);
		progressBar_.setForeground(Color.green);
		progressBarPanel_.add(progressBar_);
		progressBarPanel_.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		buttonPanel_.add(startButton_);
		buttonPanel_.add(stopButton_);
		buttonPanel_.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		openBrowserCheck_ = new JCheckBox("with open browser");
		openBrowserCheck_.doClick();
		
		txtArea_ = new JTextArea(10,30);
		GuiConsoleOperation.setConsoleArea(txtArea_);
		
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
		this.getContentPane().add(openBrowserCheck_);
		this.getContentPane().add(txtArea_);
		this.getContentPane().add(progressBarPanel_);
		this.getContentPane().add(serverStatePanel_);
		this.pack();
		
		Path path = Paths.get("conf/img/animal_hamster.png");
		if(Files.exists(path)) {
			ImageIcon icon = new ImageIcon(path.toString());
			setIconImage(icon.getImage());
		}
		
		serverStateDisplayTimer_ = new Timer(2000, this);
		serverStateDisplayTimer_.setActionCommand(DISPLAY_TIMER);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout());
		setSize(400, 400);
	}
	
	public void start() {
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			case TASK_ACTION :{
				switch(CURRENT_TASK_STAGE) {
					case FIRST_STAGE : {
						startButton_.setEnabled(false);
						serverLoggerSettingOn();
						progressBar_.setValue(20);
						CURRENT_TASK_STAGE++;
						nextTask();
						break;
					}
					case SECOND_STAGE : {
						timeSleep(1000);
						String portNoStr = txtField_.getText();
						PORT = -1;
						try {
							PORT = Integer.parseInt(portNoStr);
						} catch(NumberFormatException ne) {
							PORT = -1;
							resetExecTask();
						}
						if(PORT == -1) {
							JLabel label = new JLabel("You shoud input number in [port No] area. [port No] area changed 9090.");
						    label.setForeground(Color.BLACK);
						    JOptionPane.showMessageDialog(this, label);
						    txtField_.setText("9090");
						    resetExecTask();
						    return;
						}
						GuiConsoleOperation.writeConsoleArea("http server setting start.");
						boolean serverStartResult = ServerOperator.mainServerStart(PORT);
						if(!serverStartResult) {
							GuiConsoleOperation.writeConsoleArea("http server start error. You should check log file.");
						}
						GuiConsoleOperation.writeConsoleArea("http server start.port no = " + String.valueOf(PORT));
						progressBar_.setValue(60);
						CURRENT_TASK_STAGE++;
						nextTask();
						break;
					}
					case THIRD_STAGE : {
						//DB Connection Create
						String dbPath = ServerPropReader.getProperties(ServerPropKey.DBServerConnetionPath.getKey()).toString();
						DBConnectionOperation.getInstance().createConnectionPool(dbPath);
						timeSleep(1000);
						if(openBrowserCheck_.isSelected()) {
							timeSleep(2000);
							boolean openBrowserResult = OpenBrowzerOperation.openWebBrowser(PORT);
							if(!openBrowserResult) {
								GuiConsoleOperation.writeConsoleArea("openBrowser failed.....");
								GuiConsoleOperation.writeConsoleArea("http://localhost:" + String.valueOf(PORT));
							}
						}
						progressBar_.setValue(80);
						//GuiConsoleOperation.writeConsoleArea("http://localhost:9090/");
						stopButton_.setEnabled(true);
						serverState_.setForeground(SERVER_ON);
						CURRENT_TASK_STAGE++;
						nextTask();
						//serverStateDisplayTimer_.start();
						break;
					}
					case DBSERVER_STAGE : {
						boolean withStart = Boolean.parseBoolean(ServerPropReader.getProperties(ServerPropKey.WithDBServerStart.getKey()).toString());
						if(!withStart) {
							progressBar_.setValue(100);
							return;
						}
						if(System.getenv("JAVA_HOME") == null) {
							progressBar_.setValue(100);
							ServerLogger.getInstance().warn("DBServer cannot start. beacuse java path is not enable.");
							return;
						}
//						ProcessBuilder processBuilder = new ProcessBuilder("java", "datasheet.DBServer.main.DBServerMain");
//						processBuilder.start();
//						DBServerMain.main(null);
						break;
					}
				}
				break;
			}
			case START_BUTTON: {
				CURRENT_TASK_STAGE = 0;
				hiddenTaskButton_.doClick();
				break;
			}
//			case START_BUTTON : {
//				serverLoggerSettingOn();
//				timeSleep(1000);
//				String portNoStr = txtField_.getText();
//				int portNo = -1;
//				try {
//					portNo = Integer.parseInt(portNoStr);
//				} catch(NumberFormatException ne) {
//					portNo = -1;
//				}
//				if(portNo == -1) {
//					JLabel label = new JLabel("You shoud input number in [port No] area. [port No] area changed 9090.");
//				    label.setForeground(Color.BLACK);
//				    JOptionPane.showMessageDialog(this, label);
//				    txtField_.setText("9090");
//				    return;
//				}
//
//				startButton_.setEnabled(false);
//				GuiConsoleOperation.writeConsoleArea("http server setting start.");
//				boolean serverStartResult = ServerOperator.mainServerStart(portNo);
//				if(!serverStartResult) {
//					GuiConsoleOperation.writeConsoleArea("http server start error. You should check log file.");
//				}
//				GuiConsoleOperation.writeConsoleArea("http server start.port no = " + String.valueOf(portNo));
				//GuiConsoleOperation.writeConsoleArea("http://localhost:9090/");
//				stopButton_.setEnabled(true);
//				//progressBar_.setIndeterminate(true);
//				
//				if(openBrowserCheck_.isSelected()) {
//					timeSleep(2000);
//					boolean openBrowserResult = OpenBrowzerOperation.openWebBrowser(portNo);
//					if(!openBrowserResult) {
//						GuiConsoleOperation.writeConsoleArea("openBrowser failed.....");
//						GuiConsoleOperation.writeConsoleArea("http://localhost:" + String.valueOf(portNo));
//					}
//				}
//				break;
//			}
			case TASK_STOP_ACTION : {
				switch(CURRENT_TASK_STAGE) {
					case FIRST_STAGE : {
						stopButton_.setEnabled(false);
						ServerOperator.mainServerStop();
						timeSleep(1000);
						progressBar_.setValue(70);
						CURRENT_TASK_STAGE++;
						nextStopTask();
						break;
					}
					case SECOND_STAGE : {
						serverLoggerSettingOff();
						serverState_.setForeground(SERVER_OFF);
						GuiConsoleOperation.writeConsoleArea("http server is stoped");
						timeSleep(1000);
						FileReadCasheClear.crearFileReadCashe();
						progressBar_.setValue(40);
						CURRENT_TASK_STAGE++;
						nextStopTask();
						break;
					}
					case THIRD_STAGE : {
						// DB Connection Stop
						DBConnectionOperation.getInstance().connectionPoolClose();
						// GC Start
						System.gc();
						timeSleep(1000);
						startButton_.setEnabled(true);
						CURRENT_TASK_STAGE++;
						progressBar_.setValue(20);
						nextStopTask();
						break;
					}
					case DBSERVER_STAGE : {
						boolean withStart = Boolean.parseBoolean(ServerPropReader.getProperties(ServerPropKey.WithDBServerStart.getKey()).toString());
						if(!withStart) {
							progressBar_.setValue(0);
							return;
						}
						progressBar_.setValue(0);
						break;
					}
				}
				break;
			}
			case STOP_BUTTON : {
				CURRENT_TASK_STAGE = 0;
				hiddenStopTaskButton_.doClick();
				break;
			}
//			case STOP_BUTTON : {
//				stopButton_.setEnabled(false);
//				ServerOperator.mainServerStop();
//				timeSleep(1000);
//				serverLoggerSettingOff();
//				progressBar_.setIndeterminate(false);
//				startButton_.setEnabled(true);
//				progressBar_.setValue(0);
//				//serverStateDisplayTimer_.stop();
//				serverState_.setForeground(SERVER_OFF);
//				GuiConsoleOperation.writeConsoleArea("http server is stoped");
//				timeSleep(1000);
//				// GC Start
//				System.gc();
//				timeSleep(1000);
//				break;
//			}
			
			case DISPLAY_TIMER: {
//				if(SERVER_OFF.equals(serverState_.getText().trim())) {
//					serverState_.setText(SERVER_ON);
//				} else {
//					serverState_.setText(SERVER_OFF);
//				}
				break;
			}
		}
	}
	
	private void nextTask() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				hiddenTaskButton_.doClick();
				
			}
		}).start();
	}
	
	private void nextStopTask() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				hiddenStopTaskButton_.doClick();
				
			}
		}).start();
	}
	
	private void resetExecTask() {
		CURRENT_TASK_STAGE = 0;
		startButton_.setEnabled(false);
	}
	
	private void settingButtonAction(JButton bt) {
		bt.addActionListener(this);
	}

	private void timeSleep(int miltimes) {
		try {
			Thread.sleep(miltimes);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void serverLoggerSettingOn() {
		//Server logger create Instance
		ServerLogger.getInstance().appLog("+++++++++ Hello Statistics World ++++++++++");
		ServerLogger.getInstance().appLog("     *   *                 *    *          ");
		ServerLogger.getInstance().appLog("     *****                 ******          ");
		ServerLogger.getInstance().appLog("                                           ");
		ServerLogger.getInstance().appLog("           ***************                 ");
	}
	
	private static void serverLoggerSettingOff() {
		//Server logger create Instance
		ServerLogger.killLogSetting();
	}

}
