package org.example.assessment;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.example.assessment.resource.BookResources;
import org.onehippo.repository.jaxrs.RepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.modules.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookModule implements DaemonModule{

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void initialize(Session session) throws RepositoryException {
        RepositoryJaxrsService.addEndpoint(
                new RepositoryJaxrsEndpoint(BookResources.SERVICE_PATH)
                        .singleton(new JacksonJsonProvider())
                        .singleton(new BookResources(session)));
        log.info("{} endpoint added",BookResources.SERVICE_PATH);
    }

    @Override
    public void shutdown() {
        RepositoryJaxrsService.removeEndpoint(BookResources.SERVICE_PATH);
        log.info("{} endpoint removed", BookResources.SERVICE_PATH);
    }
}
