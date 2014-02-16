package org.esupportail.catappsrvs.web.dto;


public interface IDTO<T> {
    <D extends IDTO<T>> D withCode(String code);
}
