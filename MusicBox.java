// Pranav Komarla

import java.lang.Runnable;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.util.Random;
import java.net.*;

public class MusicBox extends JFrame implements Runnable, ActionListener, AdjustmentListener
{
	JFrame frame;
	JToggleButton[][] board;

	JPanel boardPanel, menuButtonPanel, tempoPanel;

	JScrollPane buttonPane;

	JScrollBar tempoBar;
	int tempo;

	String[] notes;

	Clip[] clip;

	String[] instrumentNames = { "Bell", "Glockenspiel", "Marimba", "Oboe", "Oh_Ah", "Piano" };

	boolean dontStop = true;

	boolean currentlyPlaying = false;

	int row = 0, col = 0;

	JMenuBar menuBar;

	JButton playStopButton, clearButton;

	JMenu instrumentMenu, file;
	JMenuItem[] instrumentItems;

	JMenuItem save, load;

	Thread timing;

	JLabel tempoLabel;

	String currentDirectory;
	JFileChooser fileChooser;



	public MusicBox()
	{

		currentDirectory = System.getProperty("user.dir");
		fileChooser = new JFileChooser(currentDirectory);

		setSize(1000, 800); //might be able to replace this with 'this.setSize(board[0].length*50, board.length*50);' scroll down below to find the line
		frame = new JFrame("MusicBox");


		board = new JToggleButton[37][50];
		boardPanel = new JPanel();

		boardPanel.setLayout(new GridLayout(board.length, board[0].length, 2, 5));

		boardPanel.setLayout(new GridLayout(37, 50));
		notes = new String [] {"C4", "B4", "AS4", "A4", "GS3", "G3", "FS3", "F3", "E3", "DS3", "D3", "CS3", "C3", "B3", "AS3", "A3", "GS2", "G2", "FS2", "F2", "E2", "DS2", "D2", "CS2", "C2", "B2", "AS2", "A2", "GS1", "G1", "FS1", "F1", "E1", "DS1", "D1", "CS1", "C1"};


		clip = new Clip[notes.length];

		String initInstrument = instrumentNames[5];



		for(int r = 0; r < board.length; r++)
		{
			String name = notes[r].replaceAll("S", "#");
			for(int c = 0; c < board[0].length; c++)
			{
				board[r][c] = new JToggleButton();

				board[r][c].setText(name);
				board[r][c].setPreferredSize(new Dimension(30,30));
				board[r][c].setMargin(new Insets(0, 0, 0, 0));
				boardPanel.add(board[r][c]);

			}
		}

		tempoBar = new JScrollBar(JScrollBar.HORIZONTAL, 200, 0, 50, 350);
		tempoBar.addAdjustmentListener(this);
        tempo = tempoBar.getValue();

        tempoLabel = new JLabel(String.format("%s%6s", "Tempo: ", tempo));
        tempoPanel = new JPanel(new BorderLayout());
        tempoPanel.add(tempoLabel, BorderLayout.WEST);
        tempoPanel.add(tempoBar, BorderLayout.CENTER);

		loadTones(initInstrument);
		this.add(boardPanel);



		menuBar = new JMenuBar();
		menuBar.setLayout(new GridLayout(1, 1));

		instrumentMenu = new JMenu("Instruments");
		instrumentItems = new JMenuItem[instrumentNames.length];
		for(int x = 0; x < instrumentNames.length; x++)
		{
			instrumentItems[x] = new JMenuItem(instrumentNames[x]);
			instrumentItems[x].putClientProperty("instrument", instrumentNames[x]);
			instrumentItems[x].addActionListener(this);
			instrumentMenu.add(instrumentItems[x]);

		}

		menuButtonPanel = new JPanel();
		menuButtonPanel.setLayout(new GridLayout());


		file = new JMenu("File");

		load = new JMenuItem("Load");
		load.addActionListener(this);

		save = new JMenuItem("Save");
		save.addActionListener(this);


		file.add(load);
		file.add(save);

		menuBar.add(file);
		menuBar.add(instrumentMenu);

		playStopButton = new JButton("Play");
		playStopButton.addActionListener(this);
		menuButtonPanel.add(playStopButton);

		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		menuButtonPanel.add(clearButton);

		menuBar.add(menuButtonPanel);

		buttonPane = new JScrollPane(boardPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		this.add(buttonPane, BorderLayout.CENTER);
		this.add(menuBar, BorderLayout.NORTH);
		this.add(tempoPanel, BorderLayout.SOUTH);
		//this.add(boardPanel);


		//this.setSize(board[0].length*50, board.length*50);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		timing = new Thread(this);
        timing.start();

	}

	public void createBoard()
	{



	}
	public static void main(String[]args)
	{
		MusicBox mb = new MusicBox();

	}

	public void run()
	{
		while (true) {

			try {

				if(!currentlyPlaying)
				{
					timing.sleep(0);
				}

				else
				{
					for(int r = 0; r < board.length; r++)
					{
						if(board[r][col].isSelected()) {
							clip[r].start();
							//board[r][col].setForeground(Color.YELLOW);
						}
					}

					timing.sleep(tempo);

					for (int r = 0; r < board.length; r++) {
                        if (board[r][col].isSelected()) {
                            clip[r].stop();
                            clip[r].setFramePosition(0);
                            //board[r][col].setForeground(Color.BLACK);
                        }
                    }
                    col++;
                    if(col == board[0].length)
                    	col = 0;
				}



			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{

		if(e.getSource() == playStopButton)
		{
			currentlyPlaying = !currentlyPlaying;
			if(!currentlyPlaying)
				playStopButton.setText("Play");
			else
				playStopButton.setText("Stop");
		}

		else if(e.getSource() == clearButton)
		{
			for(int r = 0; r < board.length; r++)
			{
				for(int c = 0; c < board[0].length; c++)
				{
					board[r][c].setSelected(false);
				}
			}

			reset();

		}

		else if(e.getSource() == save)
		{

			saveSong();

		}

		else if(e.getSource() == load)
		{
			reset();
			loadFile();
		}

		else {

			String currentInstrument = (String)((JMenuItem)e.getSource()).getClientProperty("instrument");//(String) e.getSource().getClientProperty("instrument");
			loadTones(currentInstrument);
			reset();
		}//-> need to get the clientState of the button

	}


	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		tempo = tempoBar.getValue();
        tempoLabel.setText(String.format("%s%6s", "Tempo: ", tempo));
	}

	public void reset()
	{
		currentlyPlaying = false;
		col = 0;
		playStopButton.setText("Play");
	}

	public void loadTones(String initInstrument)
	{
		try {
			for(int x=0;x<notes.length;x++)
			{
				URL url = this.getClass().getClassLoader().getResource(
				initInstrument+"\\"+initInstrument+" - "+notes[x]+".wav");
				System.out.println(initInstrument+"\\"+initInstrument+" - "+notes[x]+".wav");
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
				clip[x] = AudioSystem.getClip();
				clip[x].open(audioIn);
				//clip[x].start();
			}
		} catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {}

	}

	public void saveSong()
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", ".txt");
		fileChooser.setFileFilter(filter);


		if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		{

			System.out.println("hello");

			File tempFile = fileChooser.getSelectedFile();


			try{
				String tempPath = tempFile.getAbsolutePath();

				if(tempPath.contains("txt"))
				{
					tempPath = tempPath.substring(0, tempPath.length()-4);
				}

				String currSong = "";

				String[] noteNames = { " ", "c ", "b ", "a-", "a ", "g-", "g ", "f-", "f ", "e ", "d-", "d ", "c-",
	                        "c ", "b ", "a-", "a ", "g-", "g ", "f-", "f ", "e ", "d-", "d ", "c-", "c ", "b ", "a-", "a ",
	                        "g-", "g ", "f-", "f ", "e ", "d-", "d ", "c-", "c " };

	            for(int r = 0; r < board.length + 1; r++)
	            {
					if(r == 0)
					{
						currSong += tempo;
						for(int i = 0; i < board[0].length; i++) // still happens within the row loop
						{
							currSong += " ";
						}
					}

					else
					{
						currSong += noteNames[r];
						for(int c = 0; c < board[0].length; c++)
						{
							if(board[r - 1][c].isSelected())
								currSong += "x";
							else
								currSong += "-";
						}
					}

					currSong += "\n";
				}

				BufferedWriter outputStream = new BufferedWriter(new FileWriter(tempPath + ".txt"));
				outputStream.write(currSong);
				outputStream.close();
			} catch (IOException e)
			{
				//e.printStackTrace();
			}

		}
	}

	public void loadFile()
	{
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try {
				File loadFile = fileChooser.getSelectedFile();
				BufferedReader input = new BufferedReader(new FileReader(loadFile));
				String temp;
				temp = input.readLine();
				tempo = Integer.parseInt(temp.substring(0, 3));
				tempoBar.setValue(tempo);
				Character[][] song = new Character[board.length][temp.length() - 2];

				int tempRow = 0;
				while ((temp = input.readLine()) != null) {
                	for (int c = 2; c < song[0].length; c++) {
                	    song[tempRow][c - 2] = temp.charAt(c);
                    }
                	tempRow++;
                }

                setNotes(song);


			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setNotes(Character[][] chars)
	{
		buttonPane.remove(boardPanel);

		boardPanel = new JPanel();

		board = new JToggleButton[37][chars[0].length];
		boardPanel.setLayout(new GridLayout(board.length, board[0].length));

		for(int r = 0; r < board.length; r++)
		{
			String name = notes[r].replaceAll("S", "#");
			for(int c = 0; c < board[0].length; c++)
			{
				board[r][c] = new JToggleButton();

				board[r][c].setText(name);
				board[r][c].setPreferredSize(new Dimension(30,30));
				board[r][c].setMargin(new Insets(0, 0, 0, 0));
				boardPanel.add(board[r][c]);

			}
		}
		this.remove(buttonPane);
		buttonPane = new JScrollPane(boardPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(buttonPane, BorderLayout.CENTER);

		for (int r = 0; r < board.length; r++) {

            for (int c = 0; c < board[0].length; c++) {
                try {
                    if (chars[r][c] == 'x')
                        board[r][c].setSelected(true);
                    else
                        board[r][c].setSelected(false);
                } catch (NullPointerException npe) {
                } catch (ArrayIndexOutOfBoundsException ae) {
                }
            }
        }

        this.revalidate();

	}
}

