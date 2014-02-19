package org.esupportail.catappsrvs.services;


import fj.data.Either;
import fj.data.Tree;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.User;

public interface IDomaine extends ICrud<Domaine> {
    Either<Exception, Tree<Domaine>> findDomaines(User user);
}
