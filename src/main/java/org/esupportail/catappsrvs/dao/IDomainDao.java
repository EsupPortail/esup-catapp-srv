package org.esupportail.catappsrvs.dao;

import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import fj.data.Tree;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domain;

import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

public interface IDomainDao extends ICrudDao<Domain> {
    Either<Exception, Tree<Option<Domain>>> findDomaines(CommonTypes.Code code, List<LdapGroup> groups);
}
