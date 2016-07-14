
package org.mangocube.corenut.commons.exception;

import au.com.bytecode.opencsv.CSVReader;
import org.mangocube.corenut.commons.io.resource.Resource;
import org.mangocube.corenut.commons.io.resource.ResourcePatternResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read error message.
 */
public class ErrorMessageRepository {

    private static final Log log = LogFactory.getLog(ErrorMessageRepository.class);

    private Map<String, ErrorMessageModel> errorMessages;

    public ErrorMessageRepository(String url) {
        if (url != null && url.trim().length() == 0) return;

        try {
            ResourcePatternResolver resolver = ResourcePatternResolver.getInstance();
            Resource res = resolver.getResource(url);
            InputStream in = res.getInputStream();

            CSVReader csvReader = new CSVReader(new InputStreamReader(in));
            errorMessages = Collections.unmodifiableMap(loadMessagesFromCSV(csvReader));
        } catch (Exception e) {
            log.debug("Read error message resource " + url + " fail!");
            log.debug(e.getCause());
        }
    }

    private static final Map<String, ErrorMessageModel> EMPTY_MAP = new HashMap<String, ErrorMessageModel>(0);

    // load exist file content to errorMessages
    @SuppressWarnings("unchecked")
    private Map<String, ErrorMessageModel> loadMessagesFromCSV(CSVReader csvReader) throws IOException {
        List list = csvReader.readAll();
        csvReader.close();

        if (list == null || list.size() <= 1) return EMPTY_MAP;

        HashMap<String, ErrorMessageModel> err_msgs = new HashMap<String, ErrorMessageModel>();

        //the first row is title, ignore and start with second row.
        for (int i = 1; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof String[] && ((String[]) obj).length == 5) {
                String[] array = (String[]) obj;
                ErrorMessageModel bean = new ErrorMessageModel(array[0], array[1], array[2], array[3], array[4]);
                err_msgs.put(bean.getUri(), bean);
            }
        }
        return err_msgs;
    }

    /**
     * set checked exception message
     *
     * @param exp CheckedException
     */
    public void setCheckedErrorMessage(CheckedException exp) {
        if (exp == null) return;
        String error_uri = exp.getErrorCode().getDeclaringClass().getName() + "." + exp.getErrorCode().toString();

        //get message from CSV file according to unique id.
        String message = this.translate(error_uri);
        if (message != null) {
            exp.setRawErrorMessage(message);
        }
    }

    /**
     * set unchecked exception message
     *
     * @param exp UncheckedException
     */
    public void setUncheckedErrorMessage(UncheckedException exp) {
        if (exp == null) return;
        String error_uri = exp.getErrorCode().getDeclaringClass().getName() + "." + exp.getErrorCode().toString();

        //get message from CSV file according to unique id.
        String message = this.translate(error_uri);
        if (message != null) {
            exp.setRawErrorMessage(message);
        }
    }

    //get message from CSV file according to unique id. 
    private String translate(String errorUri) {
        String uri = errorUri.replace("$", ".");// just for the inner class
        if (errorMessages.get(uri) == null) return null;
        return errorMessages.get(uri).getMessage();
    }

    /**
     * get message gallary.
     *
     * @return map
     */
    public Map<String, ErrorMessageModel> getErrorMessages() {
        return errorMessages;
    }
}
