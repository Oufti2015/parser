package sst.common.file.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import sst.common.file.exceptions.ParserExceptions;
import sst.common.file.loader.interfaces.RecordFormatter;
import sst.common.file.loader.interfaces.RecordParser;
import sst.common.file.loader.interfaces.RecordSelector;

/**
 * @author Steph
 *
 */

public class FileLoader {

    private boolean ignoreErrors = true;

    public class RecordFormat {
	RecordSelector recordSelector = null;
	RecordParser recordParser = null;
	RecordFormatter recordFormatter = null;

	public RecordSelector getRecordSelector() {
	    return recordSelector;
	}

	public void setRecordSelector(RecordSelector recordSelector) {
	    this.recordSelector = recordSelector;
	}

	public RecordParser getRecordParser() {
	    return recordParser;
	}

	public void setRecordParser(RecordParser recordParser) {
	    this.recordParser = recordParser;
	}

	public RecordFormatter getRecordFormatter() {
	    return recordFormatter;
	}

	public void setRecordFormatter(RecordFormatter recordFormatter) {
	    this.recordFormatter = recordFormatter;
	}
    }

    private static FileLoader instance = null;

    private ArrayList<RecordFormat> recordFormats = new ArrayList<RecordFormat>();

    /**
     * @return instance of type FileLoader
     */
    public static FileLoader getInstance() {

	if (instance == null) {
	    instance = new FileLoader();
	}

	return instance;
    }

    /**
     * Constructor
     */
    private FileLoader() {

    }

    public void clearRecordFormats() {

	recordFormats = new ArrayList<RecordFormat>();
    }

    /**
     * @param recordSelector
     *            record selector
     * @param recordParser
     *            record parser
     * @param recordFormatter
     *            record formatter
     */
    public void addRecordFormat(RecordSelector recordSelector, RecordParser recordParser,
	    RecordFormatter recordFormatter) {

	RecordFormat rf = new RecordFormat();

	rf.setRecordSelector(recordSelector);
	rf.setRecordParser(recordParser);
	rf.setRecordFormatter(recordFormatter);

	recordFormats.add(rf);
    }

    public void process(File file) throws FileNotFoundException, IOException, ParserExceptions {

	String line = null;

	try (BufferedReader br = new BufferedReader(new FileReader(file))) {

	    while ((line = br.readLine()) != null) {
		try {

		    Collection<RecordFormat> c = recordFormats;

		    for (Iterator<RecordFormat> iter = c.iterator(); iter.hasNext();) {
			RecordFormat rf = iter.next();

			if (rf.getRecordSelector() == null || rf.getRecordSelector().select(line)) {

			    Object o = line;
			    if (rf.getRecordParser() != null) {

				o = rf.getRecordParser().parse(line);
			    }

			    if (rf.getRecordFormatter() != null) {

				rf.getRecordFormatter().format(o);
			    }

			    // next record
			    continue;
			}
		    }
		} catch (RuntimeException e) {
		    e.printStackTrace();
		    if (ignoreErrors) {
			System.err.println(" Record ignored : " + line);
			System.err.println(" Due to         : " + e.getMessage());
			Throwable t = e.getCause();
			while (null != t) {
			    if (null != t.getMessage()) {
				System.err.println(" Due to         : " + t.getMessage());
			    }
			    t = t.getCause();
			}
		    } else {
			throw e;
		    }
		}
	    }
	}
    }

    /**
     * @return the ignoreErrors
     */
    public boolean isIgnoreErrors() {
	return ignoreErrors;
    }

    /**
     * @param ignoreErrors
     *            the ignoreErrors to set
     */
    public FileLoader ignoreErrors(boolean ignoreErrors) {
	this.ignoreErrors = ignoreErrors;
	return this;
    }
}