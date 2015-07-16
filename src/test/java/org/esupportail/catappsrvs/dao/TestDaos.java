package org.esupportail.catappsrvs.dao;

import fj.F;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes.*;
import org.esupportail.catappsrvs.model.Domain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static fj.Bottom.error;
import static fj.data.List.list;
import static fj.data.List.single;
import static fj.data.Option.some;
import static org.dbunit.dataset.datatype.DataType.BIGINT;
import static org.dbunit.dataset.datatype.DataType.VARCHAR;
import static org.esupportail.catappsrvs.dao.utils.Equals.domaineCompleteEq;
import static org.esupportail.catappsrvs.model.Application.Activation.Activated;
import static org.esupportail.catappsrvs.model.Application.of;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConf.class)
@ActiveProfiles({"TEST", "JDBC"})
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TestDaos {

    @Inject
    DataSource dataSource;

    @Inject
    IDomainDao domaineDao;

    @Inject
    PlatformTransactionManager transactionManager;

    IDatabaseConnection dbunitConn;

    private final Application firstApp =
            of(Code.of("APP1"),
               Title.of("Application 1"),
               Caption.of("L'appli 1"),
               Description.of(""),
               buildUrl("http://toto.fr"),
               Activated,
               LdapGroup.of("UR1:57SI"),
               single(this.firstDom));

    private final Domain firstDom =
            Domain.of(Code.of("DOM1"),
                      Caption.of("Domain 1"),
                      Option.<Domain>some(null),
                      List.<Domain>nil(),
                      single(firstApp));

    @Before
    public void setUp() throws DatabaseUnitException, SQLException {
        final IDataSet dataSet = new DefaultDataSet(new DefaultTable[] {
                new DefaultTable("DOMAIN", new Column[]{
                        new Column("pk", BIGINT),
                        new Column("code", VARCHAR),
                        new Column("caption", VARCHAR),
                        new Column("parent_pk", BIGINT)
                }){{
                    addRow(domaineToRow(1L, firstDom));
                }},
                new DefaultTable("APPLICATION", new Column[]{
                        new Column("pk", BIGINT),
                        new Column("code", VARCHAR),
                        new Column("title", VARCHAR),
                        new Column("caption", VARCHAR),
                        new Column("description", VARCHAR),
                        new Column("url", VARCHAR),
                        new Column("activation", VARCHAR),
                        new Column("ldapgroup", VARCHAR),
                }){{
                    addRow(applicationToRow(1L, firstApp));
                }},
                new DefaultTable("DOMAIN_APPLICATION", new Column[]{
                        new Column("domain_pk", BIGINT),
                        new Column("application_pk", BIGINT)
                }){{
                    addRow(new Object[] {1L, 1L});
                }}
        });
        dbunitConn = new DatabaseConnection(dataSource.getConnection());
        DatabaseOperation.CLEAN_INSERT.execute(dbunitConn, dataSet);
    }

    @After
    public void cleanUp() throws SQLException {
        dbunitConn.close();
    }

    @Test @Transactional
    public void testCreate() {
        final Either<Exception, Domain> result = domaineDao.create(
                Domain.of(Code.of("DOM2"),
                          Caption.of("Domain 2"),
                          some(firstDom),
                          List.<Domain>nil(),
                          list(firstApp)));

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue("create ne devrait pas lever d'exception", false);
        }

        for (Domain domain : result.right())
            assertTrue("create doit se traduire par l'affectation d'une clé primaire",
                       domain.pk().isSome());
    }

    @Test
    public void testRead() {
        final Either<Exception, Domain> result = domaineDao.read(firstDom.code());

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue(e instanceof NoSuchElementException
                    ? "read appliquée à des valeurs correctes doit retourner une entité"
                    : "read ne devrait pas lever d'exception", false);
        }

        for (Domain domain : result.right())
            assertTrue("read doit retourner la bonne entité", domaineCompleteEq.eq(firstDom, domain));
    }

    @Test
    public void testUpdate() throws SQLException {
        final Domain domToUpdate = firstDom
                .withCaption(Caption.of("UPDATED Domain 1"))
                .withApplications(single(firstApp));

        final Either<Exception, Domain> dom =
                new TransactionTemplate(transactionManager).execute(new TransactionCallback<Either<Exception, Domain>>() {
            public Either<Exception, Domain> doInTransaction(TransactionStatus status) {
                return domaineDao.update(domToUpdate);
            }
        });

        final Either<Exception, Domain> readDom =
                new TransactionTemplate(transactionManager).execute(new TransactionCallback<Either<Exception, Domain>>() {
                    public Either<Exception, Domain> doInTransaction(TransactionStatus status) {
                        return domaineDao.read(firstDom.code());
                    }
                });

        for (Exception e : dom.left().toList().append(readDom.left().toList()))
            throw new AssertionError(e);

        for (P2<Domain, Domain> pair : dom.right().toList().zip(readDom.right().toList())) {
            assertTrue("update doit mettre à jour les données",
                    domaineCompleteEq.eq(domToUpdate, pair._2())
                            && domaineCompleteEq.eq(pair._1(), pair._2()));
        }
    }

    @Test
    public void testList() {
        Either<Exception, List<Domain>> listDomaines = domaineDao.list();
        assertTrue("list ne devrait pas lever d'exception", listDomaines.isRight());
    }

    @Test @Transactional
    public void testDelete() {
        assertTrue("delete ne doit pas lever d'exception", domaineDao.delete(firstDom.code()).isRight());
    }

    private Object[] domaineToRow(Long pk, Domain domain) {
        return new Object[] {
                pk,
                domain.code().value(),
                domain.caption().value(),
                getParentPk(domain)
        };
    }

    private Object[] applicationToRow(Long pk, Application application) {
        return new Object[] {
                pk,
                application.code().value(),
                application.title().value(),
                application.caption().value(),
                application.description().value(),
                application.url(),
                application.activation().toString(),
                application.group().value()
        };
    }

    private Long getParentPk(Domain domain) {
        return domain
                .parent()
                .map(new F<Domain, Long>() {
                    public Long f(Domain parent) {
                        return parent.pk().toNull();
                    }
                })
                .toNull();
    }

    private URL buildUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw error(e.getMessage());
        }
    }

}
