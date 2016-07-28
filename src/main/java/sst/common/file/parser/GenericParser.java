package sst.common.file.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import sst.common.file.exceptions.ParserExceptions;
import sst.common.file.loader.interfaces.RecordParser;

/**
 * @author zt974
 *
 */
public class GenericParser implements RecordParser {

    public static final String DELIMITER = "\t";
    private Class<?> resultObject = null;
    private HashMap<Integer, Method> parsableMethod = new HashMap<Integer, Method>();
    private String delimiter = DELIMITER;
    private boolean removeQuotes = false;
    private boolean trim = false;

    /**
     * @param resultObject
     * @throws ParserExceptions
     */
    public GenericParser(Class<?> resultObject) throws ParserExceptions {
	super();
	this.resultObject = resultObject;
	searchForParsableFields(this.resultObject);
    }

    /**
     * @deprecated Please use new GenericParser(Class).setDelimiter(";")
     * @param resultObject
     * @param delimiter
     * @throws ParserExceptions
     */
    @Deprecated
    public GenericParser(Class<?> resultObject, String delimiter) throws ParserExceptions {
	super();
	this.resultObject = resultObject;
	searchForParsableFields(this.resultObject);
	this.delimiter = delimiter;
    }

    public GenericParser delimiter(String delimiter) {
	this.delimiter = delimiter;
	return this;
    }

    public GenericParser removeQuotes(boolean removeQuotes) {
	this.removeQuotes = removeQuotes;
	return this;
    }

    public GenericParser trim(boolean trim) {
	this.trim = trim;
	return this;
    }

    /**
     * @throws ParserExceptions
     */
    private void searchForParsableFields(Class<?> c) throws ParserExceptions {
	if (null != c.getSuperclass()) {
	    searchForParsableFields(c.getSuperclass());
	}

	for (Method method : c.getDeclaredMethods()) {
	    Parser annotation = method.getAnnotation(Parser.class);
	    if (annotation != null) {
		// System.out.println("Method " + method +
		// " - Annotation TODO : " + annotation.position());
		if (!Modifier.isPublic(method.getModifiers())) {
		    throw new ParserExceptions("Method " + method + " should be public !");
		}

		Class<?>[] param = method.getParameterTypes();
		if (1 != param.length || !param[0].equals(String.class)) {
		    throw new ParserExceptions("Method " + method + " should have only one String parameter !");
		}
		parsableMethod.put(new Integer(annotation.position()), method);
	    }
	}
    }

    /**
     * @deprecated
     * @param record
     * @return
     * @throws ParserExceptions
     */
    @Deprecated
    public Object oldParse(String record) throws ParserExceptions {
	Constructor<?> construct = null;
	Object o = null;
	try {
	    construct = resultObject.getConstructor();
	    o = construct.newInstance();

	    StringTokenizer st = new StringTokenizer(record, delimiter, true);
	    int token = 0;
	    while (st.hasMoreTokens()) {
		String tokenString = st.nextToken();
		Method m = parsableMethod.get(new Integer(token));
		if (m != null) {
		    if (!delimiter.equals(tokenString)) {
			m.invoke(o, tokenString);
		    } else {
			token++;
		    }
		}
		token++;
	    }
	} catch (Exception e1) {
	    throw new ParserExceptions("Cannot parsed objects from class " + resultObject, e1);
	}
	return o;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sst.common.file.loader.interfaces.RecordParser#parse(java.lang.String)
     */
    @Override
    public Object parse(String record) throws ParserExceptions {
	Constructor<?> construct = null;
	Object o = null;
	try {
	    construct = resultObject.getConstructor();
	    o = construct.newInstance();

	    Iterable<String> it = Splitter.on(delimiter).trimResults().split(record);
	    int token = 0;
	    for (Iterator<String> iterator = it.iterator(); iterator.hasNext();) {
		String tokenString = iterator.next();
		if (!Strings.isNullOrEmpty(tokenString)) {
		    tokenString = (removeQuotes) ? removeQuotes(tokenString) : tokenString;
		    tokenString = (trim) ? tokenString.trim() : tokenString;
		    Method m = parsableMethod.get(new Integer(token));
		    if (m != null) {
			if (!delimiter.equals(tokenString)) {
			    m.invoke(o, tokenString);
			} else {
			    token++;
			}
		    }
		}
		token++;
	    }

	} catch (Exception e1) {
	    throw new ParserExceptions("Cannot parsed objects from class " + resultObject, e1);
	}
	return o;
    }

    private String removeQuotes(String tokenString) {
	if (tokenString.startsWith("\"") && tokenString.endsWith("\"")) {
	    return tokenString.substring(1, tokenString.length() - 1);
	}
	return tokenString;
    }
}
