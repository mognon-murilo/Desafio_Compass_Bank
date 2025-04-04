package br.com.compass.DB;


import java.io.Serial;

public class dbException extends RuntimeException{
    @Serial
    private static final long serialVersionUID =1L;
    public dbException(String msg){
        super(msg);
    }
}
