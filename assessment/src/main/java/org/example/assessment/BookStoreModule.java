package org.example.assessment;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.example.assessment.resource.BookResources;
import org.example.assessment.resource.BookStoreResources;
import org.onehippo.repository.jaxrs.RepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.modules.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookStoreModule implements DaemonModule{

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final String address= "/bookstore";

    @Override
    public void initialize(Session session) throws RepositoryException {
        RepositoryJaxrsService.addEndpoint(
                new RepositoryJaxrsEndpoint(address)
                        .singleton(new JacksonJsonProvider())
                        .singleton(new BookStoreResources(session)));
        log.info("{} endpoint added",address);
    }

    @Override
    public void shutdown() {
        RepositoryJaxrsService.removeEndpoint(address);
        log.info("{} endpoint removed", address);
    }
}
