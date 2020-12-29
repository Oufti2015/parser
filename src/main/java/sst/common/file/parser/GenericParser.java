package sst.common.file.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import sst.common.file.exceptions.ParserExceptions;
import sst.common.file.loader.interfaces.RecordParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * @author zt974
 */
public class GenericParser implements RecordParser {

    public static final String DEFAULT_DELIMITER = "\t";
    private final Class<?> resultObject;
    private final HashMap<Integer, Method> parsableMethod = new HashMap<>();
    private String delimiter = DEFAULT_DELIMITER;
    private boolean removeQuotes = false;
    private boolean trim = false;

    /**
     * @param resultObject
     * @throws ParserExceptions
     */
    public GenericParser(Class<?> resultObject) {
        super();
        this.resultObject = resultObject;
        searchForParsableFields(this.resultObject);
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
    private void searchForParsableFields(Class<?> c) {
        if (null != c.getSuperclass()) {
            searchForParsableFields(c.getSuperclass());
        }

        for (Method method : c.getDeclaredMethods()) {
            Parser annotation = method.getAnnotation(Parser.class);
            if (annotation != null) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new ParserExceptions("Method " + method + " should be public !");
                }

                Class<?>[] param = method.getParameterTypes();
                if (1 != param.length || !param[0].equals(String.class)) {
                    throw new ParserExceptions("Method " + method + " should have only one String parameter !");
                }
                parsableMethod.put(annotation.position(), method);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sst.common.file.loader.interfaces.RecordParser#parse(java.lang.String)
     */
    @Override
    public Object parse(String record) {
        Constructor<?> construct;
        Object o;
        try {
            construct = resultObject.getConstructor();
            o = construct.newInstance();

            Iterable<String> it = Splitter.on(delimiter).trimResults().split(record);
            int token = 0;
            for (String tokenString : it) {
                if (!Strings.isNullOrEmpty(tokenString)) {
                    tokenString = (removeQuotes) ? removeQuotes(tokenString) : tokenString;
                    tokenString = (trim) ? tokenString.trim() : tokenString;
                    token = invokeMethod(o, token, tokenString);
                }
                token++;
            }

        } catch (Exception e1) {
            throw new ParserExceptions("Cannot parsed objects from class " + resultObject, e1);
        }
        return o;
    }

    private int invokeMethod(Object o, int token, String tokenString) throws IllegalAccessException, InvocationTargetException {
        Method m = parsableMethod.get(token);
        if (m != null) {
            if (!delimiter.equals(tokenString)) {
                m.invoke(o, tokenString);
            } else {
                token++;
            }
        }
        return token;
    }

    private String removeQuotes(String tokenString) {
        if (tokenString.startsWith("\"") && tokenString.endsWith("\"")) {
            return tokenString.substring(1, tokenString.length() - 1);
        }
        return tokenString;
    }
}
