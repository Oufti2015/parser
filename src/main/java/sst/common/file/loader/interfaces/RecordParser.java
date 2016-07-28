package sst.common.file.loader.interfaces;

import sst.common.file.exceptions.ParserExceptions;

public interface RecordParser {

    public Object parse(String record) throws ParserExceptions;
}
