package org.esupportail.catappsrvs.model;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;

public interface HasCode<T> {
    Code code();
    T withCode(Code code);
}
