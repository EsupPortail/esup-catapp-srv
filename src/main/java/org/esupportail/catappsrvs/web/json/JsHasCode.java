package org.esupportail.catappsrvs.web.json;


public interface JsHasCode<T> {
    <D extends JsHasCode<T>> D withCode(String code);
}
