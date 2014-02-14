package org.esupportail.catappsrvs.web;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

@Slf4j @Value(staticConstructor = "applicationResource") @Getter(AccessLevel.NONE) // lombok
@Path("application") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public class ApplicationResource {
}
