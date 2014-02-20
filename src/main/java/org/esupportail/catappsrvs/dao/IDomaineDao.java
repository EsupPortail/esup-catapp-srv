package org.esupportail.catappsrvs.dao;

import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import fj.data.Tree;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domaine;

import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

public interface IDomaineDao extends ICrudDao<Domaine> {
    Either<Exception, Tree<Option<Domaine>>> findDomaines(CommonTypes.Code code, List<LdapGroup> groups);
}
