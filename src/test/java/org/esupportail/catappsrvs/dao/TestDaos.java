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
import org.esupportail.catappsrvs.model.Domaine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import static org.dbunit.dataset.datatype.DataType.*;
import static org.esupportail.catappsrvs.model.Application.Accessibilite.Accessible;
import static org.esupportail.catappsrvs.model.Application.Version;
import static org.esupportail.catappsrvs.model.Application.application;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.code;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.description;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.ldapGroup;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.libelle;
import static org.esupportail.catappsrvs.model.CommonTypes.Titre.titre;
import static org.esupportail.catappsrvs.model.Domaine.domaine;
import static org.esupportail.catappsrvs.model.Versionned.Version.version;
import static org.esupportail.catappsrvs.model.utils.Equals.domaineCompleteEq;
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
    IDomaineDao domaineDao;

    IDatabaseConnection dbunitConn;

    private final Application firstApp =
            application(
                    version(1),
                    code("APP1"),
                    titre("Application 1"),
                    libelle("L'appli 1"),
                    description(""),
                    buildUrl("http://toto.fr"),
                    Accessible,
                    ldapGroup("UR1:57SI"),
                    single(this.firstDom));

    private final Domaine firstDom =
            domaine(version(1),
                    code("DOM1"),
                    libelle("Domaine 1"),
                    Option.<Domaine>some(null),
                    List.<Domaine>nil(),
                    single(firstApp));

    @Before
    public void setUp() throws DatabaseUnitException, SQLException {
        final IDataSet dataSet = new DefaultDataSet(new DefaultTable[] {
                new DefaultTable("domaine", new Column[]{
                        new Column("pk", BIGINT),
                        new Column("version", INTEGER),
                        new Column("code", VARCHAR),
                        new Column("libelle", VARCHAR),
                        new Column("parent_pk", BIGINT)
                }){{
                    addRow(domaineToRow(1L, firstDom));
                }},
                new DefaultTable("application", new Column[]{
                        new Column("pk", BIGINT),
                        new Column("version", INTEGER),
                        new Column("code", VARCHAR),
                        new Column("titre", VARCHAR),
                        new Column("libelle", VARCHAR),
                        new Column("description", VARCHAR),
                        new Column("url", VARCHAR),
                        new Column("accessibilite", VARCHAR),
                        new Column("groupe", VARCHAR),
                }){{
                    addRow(applicationToRow(1L, firstApp));
                }},
                new DefaultTable("domaine_application", new Column[]{
                        new Column("domaine_pk", BIGINT),
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
        final Either<Exception, Domaine> result = domaineDao.create(
                domaine(version(-1),
                        code("DOM2"),
                        libelle("Domaine 2"),
                        some(firstDom),
                        List.<Domaine>nil(),
                        list(firstApp)));

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue("create ne devrait pas lever d'exception", false);
        }

        for (Domaine domaine : result.right()) {
            assertTrue(
                    "create doit se traduire par l'affectation d'une clé primaire",
                    domaine.pk().isSome());
            assertTrue(
                    "create doit se traduire par l'affectation d'une version égale à 1",
                    domaine.version().equals(version(1)));
        }
    }

    @Test
    public void testRead() {
        final Either<Exception, Domaine> result = domaineDao.read(firstDom.code(), some(firstDom.version()));

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue(e instanceof NoSuchElementException
                    ? "read appliquée à des valeurs correctes doit retourner une entité"
                    : "read ne devrait pas lever d'exception", false);
        }

        for (Domaine domaine : result.right())
            assertTrue("read doit retourner la bonne entité", domaineCompleteEq.eq(firstDom, domaine));
    }

    @Test @Transactional
    public void testUpdate() {
        final Domaine domToCreate = domaine(version(-1),
                code("DOM2"),
                libelle("Domaine 2"),
                Option.<Domaine>none(),
                List.<Domaine>nil(),
                List.<Application>nil());
        domaineDao.create(domToCreate);

        final Domaine domToUpdate = firstDom
                .withLibelle(libelle("UPDATED Domaine 1"))
                .withDomaines(single(domToCreate))
                .withApplications(single(firstApp));

        final Either<Exception, Domaine> dom = domaineDao.update(domToUpdate);

        final Either<Exception, Domaine> readDom = domaineDao.read(firstDom.code(), Option.<Version>none());

        for (Exception e : dom.left().toList().append(readDom.left().toList()))
            throw new AssertionError(e);

        for (P2<Domaine, Domaine> pair : dom.right().toList().zip(readDom.right().toList())) {
            final Domaine refDom = domToUpdate
                    .withDomaines(single(domToCreate.withVersion(version(2)))) // une mise à jour parent se traduit par une mise à jour enfant
                    .withApplications(single(firstApp.withVersion(firstApp.version().plus(1))))
                    .withVersion(domToUpdate.version().plus(1));
            assertTrue("update doit mettre à jour les données",
                    domaineCompleteEq.eq(refDom, pair._2())
                            && domaineCompleteEq.eq(pair._1(), pair._2()));
            assertTrue("update doit se traduire par une incrémentation de la version",
                    pair._1().version().equals(version(2)) && pair._2().version().equals(version(2)));
        }
    }

    @Test
    public void testList() {
        Either<Exception, List<Domaine>> listDomaines = domaineDao.list();
        assertTrue("list ne devrait pas lever d'exception", listDomaines.isRight());
    }

    @Test @Transactional
    public void testDelete() {
        assertTrue("delete ne doit pas lever d'exception", domaineDao.delete(firstDom.code()).isRight());
    }

    private Object[] domaineToRow(Long pk, Domaine domaine) {
        return new Object[] {
                pk,
                domaine.version().value(),
                domaine.code().value(),
                domaine.libelle().value(),
                getParentPk(domaine)
        };
    }

    private Object[] applicationToRow(Long pk, Application application) {
        return new Object[] {
                pk,
                application.version().value(),
                application.code().value(),
                application.titre().value(),
                application.libelle().value(),
                application.description().value(),
                application.url(),
                application.accessibilite().toString(),
                application.groupe().value()
        };
    }

    private Long getParentPk(Domaine domaine) {
        return domaine
                .parent()
                .map(new F<Domaine, Long>() {
                    public Long f(Domaine parent) {
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
