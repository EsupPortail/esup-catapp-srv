package org.esupportail.catappsrvs.services;


import fj.data.Either;
import fj.data.Option;
import fj.data.Tree;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.CommonTypes.Code;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.User;

public interface IDomain extends ICrud<Domain> {
    Either<Exception, Tree<Option<Domain>>> findDomaines(Code code, User user);
}
