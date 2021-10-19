package be.vdab.luigi.exceptions;

import be.vdab.luigi.restclients.KoersClient;

/*Exceptie wanneer koers niet gelezen kan worden*/
public class KoersClientException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    public KoersClientException(String message) {
        super(message);
    }
    public KoersClientException(String message, Exception origineleFout) {
        super(message, origineleFout);
    }
}
