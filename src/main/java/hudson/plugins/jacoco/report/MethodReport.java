package hudson.plugins.jacoco.report;

import hudson.plugins.jacoco.model.Coverage;
import hudson.plugins.jacoco.model.CoverageElement;
import hudson.plugins.jacoco.model.CoverageObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * @author Kohsuke Kawaguchi
 * @author David Carver
 */
//AggregatedReport<PackageReport,ClassReport,MethodReport>  -  AbstractReport<ClassReport,MethodReport>
public final class MethodReport extends AggregatedReport<ClassReport,MethodReport, SourceFileReport> {
	
	public String desc;
	
	public String lineNo;
	
	public String sourceFilePath;
	
	ArrayList<String> sourceLines;

	private IMethodCoverage methodCov;
	
	public String getSourceFilePath() {
		return sourceFilePath;
	}
	
	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getDesc(String desc) {
		return this.desc;
	}
	
	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}
	
	public void setLine(String line) {
		this.lineNo = line;
	}
	
	public String getLine() {
		return lineNo;
	}
	
	
	
	public void readFile(String filePath) throws java.io.FileNotFoundException,
    java.io.IOException {
		ArrayList<String> aList = new ArrayList<String>();
		
		BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = br.readLine()) != null) {
            	aList.add(line.replaceAll("\t", "    ").replaceAll("<", "&lt"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		
		this.sourceLines = aList;
	}
	
	
	@Override
	public String printFourCoverageColumns() {
        StringBuilder buf = new StringBuilder();
        instruction.setType(CoverageElement.Type.INSTRUCTION);
        complexity.setType(CoverageElement.Type.COMPLEXITY);
        branch.setType(CoverageElement.Type.BRANCH);
        line.setType(CoverageElement.Type.LINE);
        method.setType(CoverageElement.Type.METHOD);
		printRatioCell(isFailed(), this.instruction, buf);
		printRatioCell(isFailed(), this.branch, buf);
		printRatioCell(isFailed(), this.complexity, buf);
		printRatioCell(isFailed(), this.line, buf);
        printRatioCell(isFailed(), this.method, buf);
        logger.log(Level.INFO, "Printing Ratio cells within MethodReport.");
		return buf.toString();
	}
	
	public String writeOut() {
		StringBuilder buf = new StringBuilder();
		for (String line : sourceLines) {
			buf.append(line);
		}
		return buf.toString();
	}
	@Override
	public void add(SourceFileReport child) {
    	String newChildName = child.getName().replaceAll(this.getName() + ".", ""); 
    	child.setName(newChildName);
        getChildren().put(child.getName(), child);
        this.hasClassCoverage();
        logger.log(Level.INFO, "SourceFileReport");
    }

	public  void setCoverage(IMethodCoverage covReport) {
  	  Coverage tempCov = new Coverage();
		  tempCov.accumulate(covReport.getBranchCounter().getMissedCount(), covReport.getBranchCounter().getCoveredCount());
		  this.branch = tempCov;
		  
		  tempCov = new Coverage();
		  tempCov.accumulate(covReport.getLineCounter().getMissedCount(), covReport.getLineCounter().getCoveredCount());
		  this.line = tempCov;
		  
		  tempCov = new Coverage();
		  tempCov.accumulate(covReport.getInstructionCounter().getMissedCount(), covReport.getInstructionCounter().getCoveredCount());
		  this.instruction = tempCov;
		  
		  tempCov = new Coverage();
		  tempCov.accumulate(covReport.getMethodCounter().getMissedCount(), covReport.getMethodCounter().getCoveredCount());
		  this.method = tempCov;
		  
		  tempCov = new Coverage();
		  tempCov.accumulate(covReport.getComplexityCounter().getMissedCount(), covReport.getComplexityCounter().getCoveredCount());
		  this.complexity = tempCov;
  }
	private static final Logger logger = Logger.getLogger(CoverageObject.class.getName());

	public void setSrcFileInfo(IMethodCoverage methodCov, String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
		this.methodCov = methodCov;
		
	}
	
	public String printHighlightedSrcFile() {
		StringBuilder buf = new StringBuilder();
		try {
			
			readFile(sourceFilePath);
			buf.append(sourceFilePath+" lineSize:  "+this.sourceLines.size()).append("<br>");
			buf.append("<code>");
			for (int i=1;i<=this.sourceLines.size(); ++i) {
				if ((methodCov.getLine(i).getInstructionCounter().getStatus() == ICounter.FULLY_COVERED) || (methodCov.getLine(i).getInstructionCounter().getStatus() == ICounter.PARTLY_COVERED)) {
					buf.append(i + ": ").append("<SPAN style=\"BACKGROUND-COLOR: #ffff00\">").append(sourceLines.get(i-1)).append("</SPAN>").append("<br>");
				} else {
					buf.append(i + ": ").append(sourceLines.get(i-1)).append("<br>");
				}
			}
			buf.append("</code>");
		} catch (FileNotFoundException e) {
			buf.append("ERROR: Sourcefile does not exist!");
		} catch (IOException e) {
			buf.append("ERROR: Error while reading the sourcefile!");
		}
		return buf.toString();
	}
	
}
