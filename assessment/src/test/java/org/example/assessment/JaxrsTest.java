package org.example.assessment;

import com.jayway.restassured.specification.RequestSpecification;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.RepositoryService;
import org.onehippo.repository.jaxrs.RepositoryJaxrsServlet;
import org.onehippo.repository.testutils.PortUtil;
import org.onehippo.repository.testutils.RepositoryTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class JaxrsTest extends RepositoryTestCase {

    private Tomcat tomcat;
    private int portNumber;

    @Rule
    public TemporaryFolder tmpTomcatFolder = new TemporaryFolder();

    private String getTmpTomcatFolderName() {
        return tmpTomcatFolder.getRoot().getAbsolutePath();
    }

    @Before
    public void setupTomcat() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setBaseDir(getTmpTomcatFolderName());
        portNumber = PortUtil.getPortNumber(getClass());
        tomcat.setPort(portNumber);
        Context context = tomcat.addContext("/cms", getTmpTomcatFolderName());
        Tomcat.addServlet(context, "RepositoryJaxrsServlet", new RepositoryJaxrsServlet());
        context.addServletMappingDecoded("/ws/*", "RepositoryJaxrsServlet");
        tomcat.start();

        if (HippoServiceRegistry.getService(RepositoryService.class) == null) {
            HippoServiceRegistry.registerService(server.getRepository(), RepositoryService.class);
        }
    }

    @After
    public void tearDownTomcat() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    private void expectOK(String path, String message) {
        RequestSpecification client =
                given().auth().preemptive().basic("admin", String.valueOf("admin"));
        String url = "http://localhost:" + portNumber + "/cms/ws" + path;
        client.get(url).then().statusCode(200).content(equalTo(message));
    }

    @Test
    public void test_hello() {
        expectOK("/hello", "Hello system!");
    }
}
