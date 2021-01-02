package sst.common.file.loader;

import lombok.extern.log4j.Log4j2;
import sst.common.file.loader.interfaces.RecordFormatter;
import sst.common.file.loader.interfaces.RecordParser;
import sst.common.file.loader.interfaces.RecordSelector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Steph
 */

@Log4j2
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

    private ArrayList<RecordFormat> recordFormats = new ArrayList<>();

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
        recordFormats = new ArrayList<>();
    }

    /**
     * @param recordSelector  record selector
     * @param recordParser    record parser
     * @param recordFormatter record formatter
     */
    public void addRecordFormat(RecordSelector recordSelector, RecordParser recordParser,
                                RecordFormatter recordFormatter) {

        RecordFormat rf = new RecordFormat();

        rf.setRecordSelector(recordSelector);
        rf.setRecordParser(recordParser);
        rf.setRecordFormatter(recordFormatter);

        recordFormats.add(rf);
    }

    public void process(File file) throws IOException {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                try {
                    Collection<RecordFormat> c = recordFormats;
                    processLine(line, c);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    manageError(line, e);
                }
            }
        }
    }

    private void manageError(String line, RuntimeException e) {
        if (ignoreErrors) {
            log.error(" Record ignored : " + line);
            log.error(" Due to         : " + e.getMessage());
            Throwable t = e.getCause();
            while (null != t) {
                if (null != t.getMessage()) {
                    log.error(" Due to         : " + t.getMessage());
                }
                t = t.getCause();
            }
        } else {
            throw e;
        }
    }

    private void processLine(String line, Collection<RecordFormat> c) {
        for (RecordFormat rf : c) {
            if (rf.getRecordSelector() == null || rf.getRecordSelector().select(line)) {
                Object o = line;
                o = parse(line, rf, o);
                format(rf, o);
            }
        }
    }

    private Object parse(String line, RecordFormat rf, Object o) {
        if (rf.getRecordParser() != null) {

            o = rf.getRecordParser().parse(line);
        }
        return o;
    }

    private void format(RecordFormat rf, Object o) {
        if (rf.getRecordFormatter() != null) {

            rf.getRecordFormatter().format(o);
        }
    }

    /**
     * @return the ignoreErrors
     */
    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    /**
     * @param ignoreErrors the ignoreErrors to set
     */
    public FileLoader ignoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
        return this;
    }
}