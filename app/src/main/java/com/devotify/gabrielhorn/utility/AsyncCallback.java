package com.devotify.gabrielhorn.utility;

/**
 * Created by Usama on 9/24/14.
 */
public interface AsyncCallback<T> {
    public void onOperationCompleted(T result);
}
