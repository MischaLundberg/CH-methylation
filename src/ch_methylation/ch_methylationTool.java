package ch_methylation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import ch_methylation.FileCellRenderer;
import ch_methylation.ListTransferHandler;

public class ch_methylationTool extends JPanel implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JFileChooser fc;
    JButton clear;
    JButton revcmplBtn;
    JFormattedTextField minPercentage;
    JFormattedTextField outputfn;
    JRadioButton fastaNameGiven;
    JRadioButton fastaNameNotGiven;
    JTextArea console;
    boolean FastaNameInFile;
    String black = "000000";
    String red = "F20513";
    String blue = "3A86FF";
    String green = "00D60A";
    String borderTitle = "Selected Alignment files";
    
    int[][] emptyOutput = new int[][] {{}};
	int[] emptyOutputShort = new int[] {};

    JList dropZone;
    DefaultListModel listModel;
    JSplitPane childSplitPane, parentSplitPane;
    PrintStream ps;

  public ch_methylationTool() {
    super(new BorderLayout());

    fc = new JFileChooser();;
    fc.setMultiSelectionEnabled(true);
    fc.setDragEnabled(true);
    fc.setControlButtonsAreShown(false);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    JPanel fcPanel = new JPanel(new BorderLayout());
    fcPanel.add(fc, BorderLayout.CENTER);
    
    JLabel label = new JLabel("Min percentage of Methylation (0-100): ");
    minPercentage = new JFormattedTextField("0");
    minPercentage.setValue("0");
    JLabel label2 = new JLabel("Outputfilename : ");
    String standardOutputName = "ch_ratio"; //dataFolder+"\\revcmpl.seq";
	outputfn = new JFormattedTextField(standardOutputName);
	outputfn.setValue(standardOutputName);
	outputfn.setToolTipText("The files (one with the suffix .txt and one with .docx) " + 
			"will be stored in the folder of your input files.");
    JLabel label5 = new JLabel("Input all your files that you want to ");
    label5.setHorizontalAlignment(SwingConstants.RIGHT);
    
	GridLayout experimentLayout = new GridLayout(0,2);
	final JPanel compsToExperiment = new JPanel();
    compsToExperiment.setLayout(experimentLayout);
    JPanel controls = new JPanel();
    controls.setLayout(new GridLayout(2,3));
    compsToExperiment.add(label);
    compsToExperiment.add(minPercentage);
    compsToExperiment.add(label2);
    compsToExperiment.add(outputfn);

    clear = new JButton("Clear All");
    clear.addActionListener(this);
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    buttonPanel.add(clear, BorderLayout.LINE_END);
    
    revcmplBtn = new JButton("Calculate CH-Methylation ratio");
    revcmplBtn.addActionListener(new ActionListener() { 
    	
        public void actionPerformed(ActionEvent e) { 
        	
        	if (listModel.isEmpty()) {
        		console.append("You didn't drop any files into the \"" + borderTitle + 
        				"\" window. \n Please drop your files from your Windows-Explorer. \n");
        	}
        	else {
        		console.setText(null);
	        	console.append("Starting to calculate CH-Methylation ratio\n\nPELASE REMEMBER, " + 
	        			"THE POSITIONS GIVEN ARE 0 BASED (STARTS ALWAYS WITH 0 NOT 1)!\n");
	        	console.append("Running now: \n");
	        	
	        	int percentage = Integer.parseInt(minPercentage.getText()); 
	        	String outputString = "";
				ArrayList<int[][]> outputCompilation = new ArrayList<int[][]>();
				int runsWithoutProblems = 0;
				int runsWithProblems = 0;
				
				for (int i = 0; i < listModel.getSize(); i++) {
					
					File tmpFile = new File(listModel.get(i).toString());
					console.append(FilenameUtils.removeExtension(tmpFile.getName()) + "\n");				
					String[] seq = openFile(listModel.get(i).toString());
					
					//TODO
					//surround with try catch. in case one of the files is malformatted
					
					System.out.println(seq.length/2 + " sequences including Genomic region , first two rows: " + 
							seq[0] + "\n" + seq[1]);
					int[][] seqCount = new int[7][seq[1].length()];
					String[] genomic;
					
					if (seq[0].toLowerCase().contains("genomic")) {
						genomic = new String[] {">" + listModel.get(i).toString(), seq[1].toString()};
					}
					else {
						genomic = null;
					}
					
					//System.out.println("seq[0]: " + seq[0]);
					//System.out.println("GENOMIC: " + genomic);
					
					for (int y = 0; y < seq.length; y++) {
						if (!seq[y].startsWith(">") && !seq[y].toLowerCase().contains("genomic") && 
								!seq[y-1].toLowerCase().contains("genomic")) {
							seqCount = getCNotGRatio(seq[y], seqCount);
							//System.out.println(seq[y]);
						}		
					}
					
					int[][] output = getSignificantPositions(seqCount[4], ((seq.length / 2) - 1), 
							seq[1].length(), percentage);
					int problemWithCurrentRun = 0;
					//System.out.println("seqCount[4]:" + Arrays.toString(seqCount[4]) + ", seq[1].length():" + seq[1].length());
					//System.out.println("output.size(): " + output);
					
					for (int x = 0; x < output.length; x++) {

						//System.out.println("could be empty: " + Arrays.toString(output[x]) + ", should be the same: " + Arrays.toString(emptyOutputShort));
						if (Arrays.equals(output[x],emptyOutputShort) || Arrays.equals(output,emptyOutput)) {
							console.append("\nNo Output for " + genomic[0] + 
									", a reason may be, that the presettings are to stringent!\n\n");
							problemWithCurrentRun ++;
						}
						else if (output[x][0] == 0 && output[x][1] == 0 && output[x][2] == 0) {
							console.append("\nWith the threshold of " + output[x][3] + 
									", we could not find any CH.\n");
							problemWithCurrentRun ++;
						}
						else{
							//console.append(Arrays.toString(output[x]));
							break;
						}						
					}
					if (problemWithCurrentRun > 0) {
						runsWithProblems  ++ ;
					}
					else {
						runsWithoutProblems ++ ;
					}
					outputString += genomic[0] + "\n" + genomic[1];
					//System.out.println("OutputString: \n"+outputString);
					//System.out.println("output.length: " + output.length + ", output:" + Arrays.toString(output));
					outputCompilation.add(output);
					
					
				}
				console.append("\nRuns with Problems: " + runsWithProblems + "/" + listModel.getSize() 
					+ "\nRuns without Problems: " + runsWithoutProblems + "/" + listModel.getSize());

				/*
				 * Call saveToDocx and saveToXLS
				 */
				try {
					saveToDOCX (outputString, outputCompilation, listModel.get(0).toString());
				} catch (IOException e2) {
					console.append("Error while saving the .docx file! Please ensure that the file is closed!");
					e2.printStackTrace();
				}
				try {
					saveToXLS (outputString, outputCompilation, listModel.get(0).toString());
				} catch (IOException e1) {
					console.append("Error while saving the .xls file! Please ensure that the file is closed!");
					e1.printStackTrace();
				}

				console.append("\n\n\nFINISHED with the current job!\n\n Buy me a cookie :)");
        	}

        } 
    });
    
    JPanel leftUpperPanel = new JPanel(new BorderLayout());
    JScrollPane leftLowerPanel = new javax.swing.JScrollPane();
    listModel = new DefaultListModel();
    dropZone = new JList(listModel);
    dropZone.setCellRenderer(new FileCellRenderer());
    dropZone.setTransferHandler(new ListTransferHandler(dropZone));
    dropZone.setDragEnabled(true);
    dropZone.setDropMode(javax.swing.DropMode.INSERT);
    dropZone.setBorder(new TitledBorder(borderTitle));
    console = new JTextArea();
    console.setColumns(40);
    console.setLineWrap(true);
    console.setBorder(new TitledBorder("Console - what happens right now"));
    
    buttonPanel.add(revcmplBtn, BorderLayout.LINE_START);
    leftUpperPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    leftUpperPanel.add(buttonPanel, BorderLayout.CENTER);//PAGE_END);
    leftUpperPanel.add(compsToExperiment, BorderLayout.BEFORE_FIRST_LINE);
    leftLowerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    leftLowerPanel.setViewportView(new JScrollPane(dropZone));

    childSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            leftUpperPanel, leftLowerPanel);
    childSplitPane.setDividerLocation(150);
    childSplitPane.setPreferredSize(new Dimension(480, 850));

    parentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                    childSplitPane, console);
    parentSplitPane.setDividerLocation(480);
    parentSplitPane.setPreferredSize(new Dimension(800, 850));
    parentSplitPane.revalidate();//scrollRectToVisible(getVisibleRect());
    add(parentSplitPane, BorderLayout.CENTER);
    
    console.append("This tool calculates the CH-Methylation ratio of given Alignments. "
    		+ "\n\n Drop your files into the \"" + borderTitle + "\" window and press \""
    				+ "Calculate CH-Methylation ratio\" as soon as you are done\n\n");
    parentSplitPane.validate();
    
}
  
  
public void saveToDOCX (String outputString, ArrayList<int[][]> outputCompilation, String filename) throws IOException {
	
	File tmpOutFile = new File(filename);
	String outFNTMP = tmpOutFile.getParentFile() + "\\" + outputfn.getText();
		
	
	System.out.println("outputString: " + outputString + ", " + outputCompilation.get(0).length);
	System.out.println("outputCompilation: ");
	for (int k = 0; k < outputCompilation.size(); k++) {
		for (int p = 0; p < outputCompilation.get(k).length ; p ++) {
			System.out.print(Arrays.toString(outputCompilation.get(k)[p]));
		}
		System.out.println("k = " + k);
	}

	// Blank Document
	XWPFDocument document = new XWPFDocument();
	// Write the Document in file system
	FileOutputStream out;
	//try {
		out = new FileOutputStream(new File(outFNTMP+".docx"));
	
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun paragraphOneRunOne = paragraph.createRun();
		paragraphOneRunOne.setText(outputString);
		if (outputString.contains("\n")) {
			
	        
	        
	        paragraphOneRunOne.setText("<5%", 0); // set first line into XWPFRun
	        paragraphOneRunOne.setColor(green);
 			paragraphOneRunOne.setBold(true);
 			paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        paragraphOneRunOne.setText(" , "); 
	        paragraphOneRunOne.setColor(black);
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        paragraphOneRunOne.setText("5-10%"); 
	        paragraphOneRunOne.setColor(blue);
 			paragraphOneRunOne.setBold(true);
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        paragraphOneRunOne.setText(" , "); 
	        paragraphOneRunOne.setColor(black);
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        paragraphOneRunOne.setText(">10%");
	        paragraphOneRunOne.setColor(red);
 			paragraphOneRunOne.setBold(true);
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        paragraphOneRunOne.setText(" of CH-Methylation"); 
	        paragraphOneRunOne.setColor(black);
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        
			paragraphOneRunOne.addBreak();
			paragraphOneRunOne.addBreak();
			
			String[] lines = outputString.split("\n");
	        paragraphOneRunOne.setText(lines[0]); 
	        paragraph.addRun(paragraphOneRunOne);
	        paragraphOneRunOne = paragraph.createRun();
	        int compilationCounter = 0;
	        int[][] output;
 			int firstCPos = 1000;
	        
	        for(int t = 1; t < lines.length; t++){

	        	if (((t-1) % 2) == 0) {
	        		output = outputCompilation.get(compilationCounter);
	        		compilationCounter ++;
	        	}
	        	else {
	        		output = new int[][] {,};
	        	}
	         	paragraphOneRunOne.addBreak();
	         	
	         	if (output.length != 0 && !lines[t].contains(">") && !Arrays.equals(output,emptyOutput)) {
	         		//System.out.println("output.length: " + output.length);
	         		for (int y = 0; y < output.length ; y++) {
	         			
	         			/*
	         			 * output[y][0] returns the current position for easy substitution 
	         			 * of a substring from the input file
	         			 */
	         			
	         			
	         			//System.out.println("lines.length:" + lines.length + ", t:" + t + ", outputCompilation.size():" + outputCompilation.size());
	         			if (!Arrays.equals(output[y],emptyOutputShort)) {
	         				double ratio = ((double) output[y][2] / output[y][4]);
		         			if (output[y][0] == output[y][1] && output[y][0] > 0 && firstCPos > 0) {
		         				//System.out.println("should add now: " + lines[t].substring(0, output[y][0]));
		         				paragraphOneRunOne.setText(lines[t].substring(0, output[y][0]));
		         				paragraphOneRunOne.setColor(black);
		         				paragraph.addRun(paragraphOneRunOne);
		         				paragraphOneRunOne = paragraph.createRun();
		         			}
		         			else if(output[y][0] == output[y][1] && output[y][0] > 0 && firstCPos == 0) {
		         				//System.out.println("should add now: " + lines[t].substring(0, output[y][0]));
		         				paragraphOneRunOne.setText(lines[t].substring(1, output[y][0]));
		         				paragraphOneRunOne.setColor(black);
		         				paragraph.addRun(paragraphOneRunOne);
		         				paragraphOneRunOne = paragraph.createRun();
		         			}
		         			else if (output[y][0] > 0 ) {//&& (y != 1 && )){
		         				//System.out.println("should add now: " + lines[t].substring(((output[y][0] - output[y][1])+1), output[y][0]));
		         				paragraphOneRunOne.setText(lines[t].substring(((output[y][0] - output[y][1])+1), output[y][0]));
		         				paragraphOneRunOne.setColor(black);
		         				paragraph.addRun(paragraphOneRunOne);
		         				paragraphOneRunOne = paragraph.createRun();
		         			}
		         			
		         			paragraphOneRunOne.setText(lines[t].substring(output[y][0], (output[y][0] + 1)));
		         			firstCPos = output[y][0];
		         			System.out.println(Arrays.toString(output[y]));
		         			
		         			if (output[y][0] == 0 || y == 0) {
	         					System.out.println("y: " + y + ", should add at position: " + output[y][0] + "-" + 
	         							(output[y][0] + 1) + ", the following: " + 
	         							lines[t].substring(output[y][0], output[y][0]+1));
	         				}
		         			//System.out.println("should be between 0 and 1: " + ratio + ", output[y][2] / output[y][4]: " + output[y][2] + "/" + output[y][4]);
		         			if (ratio >= 0.10){
		         				paragraphOneRunOne.setColor(red);
		         			}
		         			else if(ratio >= 0.05) {
		         				paragraphOneRunOne.setColor(blue);
		         			}
		         			else {
		         				paragraphOneRunOne.setColor(green);
		         			}
		         			paragraphOneRunOne.setBold(true);
		         			paragraphOneRunOne.setFontSize(15);
		     				paragraph.addRun(paragraphOneRunOne);
		     				paragraphOneRunOne = paragraph.createRun();
		     				
		     				if (( y + 1 ) >= output.length) {
		     					int start = (output[y][0] + 2);
		     					int end = lines[t].length();
		     					
		     					if (start > end) {
		     						start = end;
		     					}
		     					
		     					paragraphOneRunOne.setText(lines[t].substring(start ,end));
		         				paragraphOneRunOne.setColor(black);
		         				paragraph.addRun(paragraphOneRunOne);
		         				paragraphOneRunOne = paragraph.createRun();
		     				}
	         			}
	         		}
	         		
	         	}
	         	else {
	         		System.out.println("adding now in else state " + lines[t]);
	         		paragraphOneRunOne.addBreak();
	         		paragraphOneRunOne.setText(lines[t]);
	         		paragraphOneRunOne.setColor(black);
     				paragraph.addRun(paragraphOneRunOne);
     				paragraphOneRunOne = paragraph.createRun();
	         	}
	        }
	    } 
		else {
			System.out.println("adding now the full outputString " + outputString);
	      	paragraphOneRunOne.setText(outputString, 0);
	    }
		
		document.write(out);
		out.close();
		document.close();
}
  
/*
 * TODO
 */
public void saveToXLS(String outputString, ArrayList<int[][]> outputCompilation, String filename) throws IOException {
	
	File tmpOutFile = new File(filename);
	String outFNTMP = tmpOutFile.getParentFile() + "\\" + outputfn.getText() + ".xls";
	File outXLSX = new File(outFNTMP);
	
	String sheetName = "Sheet1";

	HSSFWorkbook wb = new HSSFWorkbook();
	HSSFSheet sheet = wb.createSheet(sheetName) ;
	
	String[] Header = new String[] {"Alignment Name", "Position (0-Based!!!)", 
				"Distance to last significant", "Position +/- 4 nucleotides", "cut-off", 
				"Count of Cs", "Number of Sequences for Alignment", "%" };
	//System.out.println("output: " + outputString);
	String[] lines = outputString.split("\n");
	//System.out.println("outputString.length: " + lines.length + ", lines[0]: " + lines[0]);
	String fn = "";
	int compilationCounter = 0;
    int[][] output;
    int offset = 4;
    int rowCount = 1;
    HSSFPalette palette = wb.getCustomPalette();
    HSSFFont greenFont = wb.createFont();
    greenFont.setColor(palette.findSimilarColor(Color.decode("#"+green).getRed(), 
    			Color.decode("#"+green).getGreen(), Color.decode("#"+green).getBlue()).getIndex());
    HSSFFont redFont = wb.createFont();
    redFont.setColor(palette.findSimilarColor(Color.decode("#"+red).getRed(), 
    		Color.decode("#"+red).getGreen(), Color.decode("#"+red).getBlue()).getIndex());
    HSSFFont blueFont = wb.createFont();
    blueFont.setColor(palette.findSimilarColor(Color.decode("#"+blue).getRed(), 
    		Color.decode("#"+blue).getGreen(), Color.decode("#"+blue).getBlue()).getIndex());

	//iterating r number of rows
	for (int r = 1; r < (lines.length + 1); r++){ //int r=0;r < ((lines.length / 2) + 1); r++ )
		
		fn = "";

        //Getting the Filename
        if (((r-1) % 2) == 0) {
        	output = outputCompilation.get(compilationCounter);
        	fn = lines[r-1].toString();
        	compilationCounter ++;
        }
        else {
        	output = new int[][] {,};
        }
        
        HSSFRow row; // = sheet.createRow(0);
        HSSFCell cell;// = row.createCell(0);
        
		if (r == 1) {
			row = sheet.createRow(0);
			for (int h = 0; h < Header.length; h++ ) {
				
				cell = row.createCell(h);
				cell.setCellValue(Header[h].toString());
				System.out.println("Header[h]: "+ Header[h].toString());
			}
		}
		
		//System.out.println(lines[r-1].contains(">") + ", output.length:" + output.length + "output: " + 
		//		Arrays.toString(output));
		
		if (output.length != 0 && lines[r-1].contains(">")) {
			//System.out.println(lines[r-1]);
     		//System.out.println("output.length: " + output.length);
     		for (int y = 0; y < output.length ; y++) {
     			row = sheet.createRow(rowCount);
				//iterating c number of columns
				for (int c = 0 ;c < Header.length; c++ ) 
				{
					cell = row.createCell(c);
					if (Arrays.equals(output,emptyOutput) || Arrays.equals(output[y],emptyOutputShort)) {
						rowCount --;
						break;
					}
					else {
						double percentage = (((double)output[y][2] / output[y][4]) * 100);
						switch (c){
							case 0:	cell.setCellValue(fn);
									//System.out.println(fn.toString());
									break;
							case 1:	cell.setCellValue(output[y][0]);
									break;
							case 2:	cell.setCellValue(output[y][1]);
									break;
							case 3:	int start = (output[y][0] - offset); 
									int end = (output[y][0] + (offset + 1));
									if (start < 0) {
										start = 0;
									}
									if (end > (lines[r].length() - 1)) {
										end = lines[r].length() - 1;
									}
									//System.out.println("Start > End: " + start + " > " + end);
									if (start == ( end - offset )) {
										//System.out.println("Start > End: " + start + " > " + end);
										start = end - offset - 1;
									}

									//System.out.println("start: " + start + ", end: " + end + ", " + lines[r].toString().substring(start , end));
									HSSFRichTextString richString = new HSSFRichTextString(
											lines[r].toString().substring(start , end));
									//System.out.println("Start > End: " + start + " > " + end + 
									//		", output[y][0]: " + output[y][0]);
									
									if (start == 0) {
										start = 0;
										end = offset + 1;
									}
									else {
										start = offset;
									}
									
									if (percentage >= 10) {
										richString.applyFont(start, (start + 1), redFont);
									}
									else if (percentage >= 5) {
										richString.applyFont(start, (start + 1), blueFont);
									}
									else{
										richString.applyFont(start, (start + 1), greenFont);
									}
									cell.setCellValue(richString); // lines[r]
									break;
							case 4:	cell.setCellValue(output[y][3]);
									break;
							case 5:	cell.setCellValue(output[y][2]);
									break;
							case 6:	cell.setCellValue(output[y][4]);
									break;
							case 7:	cell.setCellValue(percentage);
									break;
							default: break;
						}	
					}
				}
				rowCount++;
     		}
		}
	}
	sheet.setAutoFilter(new CellRangeAddress(0, rowCount, 0, 7));
	
	FileOutputStream fileOut = new FileOutputStream(outXLSX);
	
	//write this workbook to an Outputstream.
	wb.write(outXLSX);
	fileOut.flush();
	fileOut.close();
	wb.close();
}

/*
 * TODO
 */
public float getMean(int[] seqCount) {
	
	float sum = 0 ;
	for (int i : seqCount) {       
	    sum += i;
	}
	float mean = sum / seqCount.length;
	return mean;
}
  
/*
 * TODO
 */
public float getStandardDeviation(int[] seqCount, int seqInnerLength) {
	
	float standard_deviation = 0;
	float mean = getMean(seqCount);

	for(int i : seqCount) {       
	    standard_deviation += Math.pow((i-mean), 2);
	}  
	System.out.println("how about this?:" + (Math.sqrt(standard_deviation / seqCount.length)) + ", possible?: " + (Math.sqrt(standard_deviation / seqInnerLength) - getMean(seqCount)) + ", standard_deviation: " + Math.sqrt(standard_deviation / (seqInnerLength-1)) + " Math.sqrt(" + standard_deviation + "/" + seqInnerLength + ")"  + ", (inkorrekt)standard_deviation: " + standard_deviation / seqInnerLength + " (" + standard_deviation + "/" + seqInnerLength + ")" + ", mean: " + mean);
	standard_deviation = standard_deviation / seqInnerLength;// (float) Math.sqrt(standard_deviation / seqInnerLength);
	return (float) Math.sqrt(standard_deviation);
}

/*
public float getStandardDeviationOLD(int[] seqCount, int seqInnerLength) {
	
	float standard_deviation = 0;
	float mean = 0;//getMean(seqCount);

	for(int i : seqCount) {       
	    //sum += i;
	    standard_deviation += Math.pow((i-mean), 2);
	}  
	System.out.println("how about this?:" + (Math.sqrt(standard_deviation / seqCount.length)) + ", possible?: " + (Math.sqrt(standard_deviation / seqInnerLength) - getMean(seqCount)) + ", standard_deviation: " + Math.sqrt(standard_deviation / (seqInnerLength-1)) + " Math.sqrt(" + standard_deviation + "/" + seqInnerLength + ")"  + ", (inkorrekt)standard_deviation: " + standard_deviation / seqInnerLength + " (" + standard_deviation + "/" + seqInnerLength + ")" + ", mean: " + mean);
	standard_deviation = standard_deviation / seqInnerLength;// (float) Math.sqrt(standard_deviation / seqInnerLength);
	return standard_deviation;
}
*/


/*
 * takes seqCount[4] as input
 * returns significant[][]
 * 		[][0] = Position of significant point
 * 		[][1] = Difference to last significant point
 * 		[][2] = Occurence of this CH	
 * 		[][3] = standard deviation
 * 		[][4] = Number of Sequences within Alignment (excluding Genomic)
 */
public int[][] getSignificantPositions(int[] seqCount, int seqLength, int seqInnerLength, int percentage) {
	

	float standard_deviation = getStandardDeviation(seqCount, seqInnerLength);	
	int lastSignificantPos = 0;
	int tmpCount = 0;
	int min = 100;
	int max = 0;
	
	for (int i = 0; i < seqCount.length; i++) {
		//System.out.println("seqCount[i] > ((percentage * seqLength) / 100): " + seqCount[i] + ">=" + ((percentage * seqLength) / 100));
		if (seqCount[i] > standard_deviation && seqCount[i] >= ((percentage * seqLength) / 100)) {			
			tmpCount ++;
			if (seqCount[i] > max) { 
				max = seqCount[i];
			}
			
			if (seqCount[i] < min) { 
				min = seqCount[i];
			}
		}
	}

	int[][] output = new int[tmpCount][5];
	int[] tmpOutput = new int[tmpCount]; 
	tmpCount = 0;
	
	for (int i = 0; i < seqCount.length; i++) {
		
		if (seqCount[i] > standard_deviation && seqCount[i] >= ((percentage * seqLength) / 100)) {
			
			//System.out.println("Is significant position: " + i + ", count: " + seqCount[i] + 
			//	", difference to las Significant: " + (i-lastSignificantPos) + ", cut-off (standard deviation) is: " + standard_deviation);
			output[tmpCount] = new int[] {i,(i-lastSignificantPos),seqCount[i],(int) standard_deviation, seqLength};
			tmpOutput[tmpCount] = seqCount[i];
			lastSignificantPos = i;
			tmpCount ++;
		}
	}
	
	float new_standard_deviation = 0;
	if (tmpCount > 3 && standard_deviation < (seqCount.length*0.1)) {
		if (min == Math.round(standard_deviation)) {
			System.out.println("tmpOutput.length: " + tmpOutput.length + ", tmpOutput: " + Arrays.toString(tmpOutput) + ", lowest: " + min + ", highest: " + max);
			new_standard_deviation = getStandardDeviation(tmpOutput, tmpOutput.length);
			System.out.println("old standard_deviation: " + standard_deviation + ", new standard_deviation: " + new_standard_deviation);
		}
	}
	
	if (new_standard_deviation > 0 && new_standard_deviation < max){
		
		int diffCount = 0;
		int lastCount = 0;
		int diff = 0;
		int outputCounter = 0;
		
		for (int y = 0 ; y < output.length; y ++) {
			
			if (output[y][2] > new_standard_deviation) {
				outputCounter ++;
			}
		}
		
		int[][] newOutput = new int[outputCounter][5];
		outputCounter = 0;
		
		for (int y = 0 ; y < output.length; y ++) {
			
			if (output[y][2] > new_standard_deviation) {
				
				if ((output[y][1] + diffCount) > output[y][0]) {
					diff = output[y][0];
				}
				else {
					diff = (output[y][1] + diffCount);
				}
				newOutput[outputCounter] = new int[] {output[y][0],diff,output[y][2],output[y][3],output[y][4],};
				//System.out.println(Arrays.toString(newOutput[outputCounter]));
				//System.out.println("Is significant position: " + output[y][0] + ", count: " + output[y][2] + ", difference to last Significant: " + diff + ", cut-off (standard deviation) is: " + new_standard_deviation);
				lastCount = output[y][0];
				outputCounter ++;
			}
			else {
				diffCount = (output[y][0] - lastCount);
			}
			
			output[y][3] = (int) new_standard_deviation;
			output[y][4] = seqLength;
			
		}
		
		if (newOutput.length == 0) {
			newOutput = new int[][] {{}};//0,0,0,(int) new_standard_deviation, seqLength}};
			System.out.println("significant: " + Arrays.toString(output[4]));
			return newOutput;
		}
		else{
			System.out.println("significant: " + Arrays.toString(output[4]));
			return newOutput;
		}
	}
	
	else {
		if (output.length == 0) {
			output = new int[][] {{}};//0,0,0,(int) standard_deviation, seqLength}};
			System.out.println("significant: " + Arrays.toString(output[0]));
			return output;
		}
		else{
			System.out.println("significant: " + Arrays.toString(output[0]));
			return output;
		}
	}
	//System.out.println("significant: " + Arrays.toString(output[3]));
	
}
  
  
  /*
   * is returning a list of numbers, either 0 if everything OK, or 1 if there is a C instead of a T
   */
public int[][] getCNotGRatio(String seq, int[][] totalSeqCount) {
	
	
	int[] seqCxorGcount = new int[seq.length()];
	seq = seq.toUpperCase().replace("\n", "");
	//System.out.println(seq.length());
	for (int key = 0; key < seq.length(); key++) {
//		  System.out.println("key:" + key + " , seq[key]:" + seq.charAt(key));
		seqCxorGcount[key] = 0;
		switch (seq.charAt(key)) {
			case 'A': totalSeqCount[0][key]++;	
					  break;
			case 'G': totalSeqCount[1][key]++;
					  break;
			case 'C': totalSeqCount[2][key]++;
					  seqCxorGcount[key] = 1;
					  //System.out.println("key: " + key + ", seq.length: " + seq.length());
					  if(key == seq.length()-1){
							totalSeqCount[4][key]++;
							//seqCxorGcount[key] = 1;
							break;
					  }
					  else if (key+2 <= seq.length()-1) {
						  if(seq.charAt(key) == 'C' && seq.charAt(key+1) != 'G' && (seq.charAt(key+1) != '-' || (seq.charAt(key+1) == '-' && seq.charAt(key+2) != 'G'))) {
								totalSeqCount[4][key]++;
								//System.out.println("found a C at pos " + key);
								break;
						  }
					  }
					  else if(seq.charAt(key) == 'C' && seq.charAt(key+1) != 'G' ) {
							totalSeqCount[4][key]++;
							//System.out.println("found a C at pos " + key);
							break;
					  }
						
					  break;
			case 'T': totalSeqCount[3][key]++;
					  //System.out.println(totalSeqCount[3][key]);
					  break;
			default: totalSeqCount[5][key]++;
					 break;
		}
	}
	
	//System.out.println("totalSeqCount: " + Arrays.toString(totalSeqCount[4]) + ", Seq:" + seq);
	totalSeqCount[6] = seqCxorGcount;
	return totalSeqCount;
}


public boolean isFastaFormat(String seq) {
	
	return seq.contains(">");
	
}


public String[] openFile(String fileName) {//, String separator) {
	
	
	//[dna sequence, name of sequence used for Fasta output (e.g. >NA12878)]
	Scanner sc = null;
	File FN = new File(fileName);
	//System.out.println("Filename: " + fileName);
	
	try {
		sc = new Scanner(FN);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		console.append("Could not access the file: " + fileName);
		System.out.println("Exception ");
	    System.out.println(e.getMessage());
	}
	
	List<String> lines = new ArrayList<String>();
	
	while (sc.hasNextLine()) {
	  lines.add(sc.nextLine());
	}
	
	String[] SeqArr = lines.toArray(new String[0]);
	int occurence = 0;
	
	for (String key : SeqArr) {
		//System.out.println(key);
		//System.out.println(key.toCharArray());
		for (char innerKey : key.toCharArray()) {
			if (innerKey == '>') {
				occurence++;
			}
		}
	}
	
	//System.out.println(occurence);
	//System.out.println(SeqArr.length);
	boolean isFasta = isFastaFormat(SeqArr[0]);
	String data[] = new String[occurence*2];
	
	if (isFasta) {
		Integer outputArrayCount = -1;
		
		for (int i = 0; i < SeqArr.length; i++) {
			
			if (SeqArr.length == 1) {
				//throws exception
				System.out.println("ERROR, no Fasta file given");
				//break;
				return null;
			}
			else if (SeqArr[i].startsWith(">")) {
				if (i > 0) {
					//System.out.println("adding a new line before writing new sequence name");
					//data[outputArrayCount].replace(null, "");
					data[outputArrayCount] += "\n";
				}
				
				outputArrayCount++;
				data[outputArrayCount] = SeqArr[i].replace("\n", "").replace("\r", "");
				
				//System.out.println("String at pos i=" + i + ", is: " + SeqArr[i].replace("\n", "").replace("\r", ""));
				if (SeqArr[i].replace("\n", "").replace("\r", "") == null) {
					System.out.println("BOOOOOM - Your meth lab expoded... :( \n\nERROR,  SeqArr[i].replace(\"\\n\", \"\").replace(\"\\r\", \"\") is null!");
				}
				outputArrayCount++;
			}
			else {
				if (SeqArr[i].replace("\n", "").replace("\r", "") == null || SeqArr[i].length() == 0) {
					System.out.println("BOOOOOM - Your meth lab expoded... :( \n\nERROR,  SeqArr[i].replace(\"\\n\", \"\").replace(\"\\r\", \"\") is null or empty!");
				}
				else if (SeqArr[i-1].startsWith(">")) {
					//System.out.println("SeqArr.length=" + SeqArr.length + ",String at pos i=" + i + ", is: " + SeqArr[i].replace("\n", "").replace("\r", ""));
					data[outputArrayCount] = SeqArr[i].replace("\n", "").replace("\r", "");
				}
				else {
					//System.out.println("SeqArr.length=" + SeqArr.length + ",String at pos i=" + i + ", is: " + SeqArr[i].replace("\n", "").replace("\r", ""));
					data[outputArrayCount] += SeqArr[i].replace("\n", "").replace("\r", "");
				}
			}
		}
		//System.out.println("occurence=" + occurence + " should be the same as data[].length/2=" + data.length/2);
		return data;
	}
	else {
		
		//throws exception
		System.out.println("ERROR, no Fasta file given");
		return null;
	}
	
}


  
public void setDefaultButton() {
    getRootPane().setDefaultButton(clear);
    getRootPane().setDefaultButton(revcmplBtn);
}

public void actionPerformed(ActionEvent e) {
    if (e.getSource() == clear) {
        listModel.clear();
        console.setText(null);
        console.append("Drop your files into the \"" + borderTitle + "\" window and press \"Calculate CH-Methylation ratio\" as soon as you are done\n\n");
    }
}

/**
 * Create the GUI and show it. For thread safety,
 * this method should be invoked from the
 * event-dispatching thread.
 */
private static void createAndShowGUI() {
    //Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);
    try {
      //UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlackStarLookAndFeel");
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    }catch (Exception e){
      e.printStackTrace();
    }

    //Create and set up the window.
    JFrame frame = new JFrame("Calculate CH-Methylation ratio");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    //Create and set up the menu bar and content pane.
    ch_methylationTool demo = new ch_methylationTool();
    demo.setOpaque(true); //content panes must be opaque
    frame.setContentPane(demo);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
    demo.setDefaultButton();
}

public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            createAndShowGUI();
        }
    });
}
}

class FileCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(
            list,value,index,isSelected,cellHasFocus);

        if (c instanceof JLabel && value instanceof File) {
            JLabel l = (JLabel)c;
            File f = (File)value;
            l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
            l.setText(f.getName());
            l.setToolTipText(f.getAbsolutePath());
        }

        return c;
    }
}


class ListTransferHandler extends TransferHandler {

    private JList list;

    ListTransferHandler(JList list) {
        this.list = list;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import FileList
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        // Check for FileList flavor
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            displayDropLocation("List doesn't accept a drop of this type.");
            return false;
        }

        // Get the fileList that is being dropped.
        Transferable t = info.getTransferable();
        List<File> data;
        try {
            data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
        }
        catch (Exception e) { return false; }
        DefaultListModel model = (DefaultListModel) list.getModel();
        for (Object file : data) {
            model.addElement((File)file);
        }
        return true;
    }

    private void displayDropLocation(String string) {
        System.out.println(string);
    }
    
}
