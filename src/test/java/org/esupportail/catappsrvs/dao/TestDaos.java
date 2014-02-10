package org.esupportail.catappsrvs.dao;

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
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static fj.data.Option.fromNull;
import static fj.data.Option.some;
import static org.dbunit.dataset.datatype.DataType.INTEGER;
import static org.dbunit.dataset.datatype.DataType.VARCHAR;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.code;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.libelle;
import static org.esupportail.catappsrvs.model.Domaine.domaine;
import static org.esupportail.catappsrvs.model.Versionned.Version.version;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConf.class)
@ActiveProfiles("JDBC")
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TestDaos {

    @Inject
    DataSource dataSource;

    @Inject
    IDomaineDao domaineDao;

    IDataSet dataSet;

    IDatabaseConnection dbunitConn;

    @Before
    public void setUp() throws DatabaseUnitException, SQLException {
        dataSet = new DefaultDataSet(new DefaultTable("domaine", new Column[]{
                new Column("version", INTEGER),
                new Column("code", VARCHAR),
                new Column("libelle", VARCHAR),
        }){{
            addRow(new Object[] {1, "DOM1", "Domaine 1"});
        }});
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
                        Option.<Domaine>none(),
                        List.<Domaine>nil(),
                        List.<Application>nil()));

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue("create ne devrait pas lever d'exception", false);
        }

        for (Domaine domaine : result.right()) {
            assertTrue(
                    "create doit se traduire par l'affectation d'une clé primaire",
                    fromNull(domaine.pk()).isSome());
            assertTrue(
                    "create doit se traduire par l'affectation d'une version égale à 1",
                    domaine.version().equals(version(1)));
        }
    }

    @Test
    public void testRead() {
        final Either<Exception, Domaine> result = domaineDao.read(code("DOM1"), some(version(1)));

        for (Exception e: result.left()) {
            e.printStackTrace();
            assertTrue(e instanceof NoSuchElementException
                    ? "read appliquée à des valeurs correctes doit retourner une entité"
                    : "read ne devrait pas lever d'exception", false);
        }

        for (Domaine domaine : result.right())
            assertTrue("read doit retourner la bonne entité", domaine.equals(domaine(
                    version(1),
                    code("DOM1"),
                    libelle("Domaine 1"),
                    Option.<Domaine>none(),
                    List.<Domaine>nil(),
                    List.<Application>nil())));

    }

    @Test
    public void testUpdate() {
        domaineDao.update(domaine(
                    version(1),
                    code("DOM1"),
                    libelle("Domaine 1"),
                    Option.<Domaine>none(),
                    List.<Domaine>nil(),
                    List.<Application>nil()));
    }
}
