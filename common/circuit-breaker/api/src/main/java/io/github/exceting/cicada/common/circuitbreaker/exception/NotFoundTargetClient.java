package io.github.exceting.cicada.common.circuitbreaker.exception;

public class NotFoundTargetClient extends IllegalAccessException{

    public NotFoundTargetClient() {
        super();
    }

    public NotFoundTargetClient(String s) {
        super(s);
    }
}
