package org.esupportail.catappsrvs.web.dto;


public interface JsHasCode<T> {
    <D extends JsHasCode<T>> D withCode(String code);
}
