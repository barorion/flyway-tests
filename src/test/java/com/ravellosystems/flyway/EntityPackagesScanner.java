package com.ravellosystems.flyway;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;

import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class EntityPackagesScanner {

	private final static String BASE_PACKAGE = "com.ravellosystems";

	private final Set<String> entityPackages = Sets.newHashSet();

	@PostConstruct
	public void scanForEntityPackageList() {
		Reflections reflections = new Reflections(BASE_PACKAGE);
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
		for (Class<?> entity : entities) {
			entityPackages.add(entity.getPackage().getName());
		}
	}

	public Set<String> getEntityPackages() {
		return entityPackages;
	}

}
