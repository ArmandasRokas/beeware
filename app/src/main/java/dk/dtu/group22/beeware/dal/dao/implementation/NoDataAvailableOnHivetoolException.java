package dk.dtu.group22.beeware.dal.dao.implementation;

/**
 * Exception is thrown, when hive data is not available on HiveTool in a selected time interval.
 */
public class NoDataAvailableOnHivetoolException extends Exception{
    public NoDataAvailableOnHivetoolException(String errorMessage) {
        super(errorMessage);
    }
}
