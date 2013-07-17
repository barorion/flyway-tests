package com.ravellosystems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ExampleEntity {

	@Id
	private long id;

	private Date creationTime;

	private String name;

	// so need to supply a migration script to create the column in DB
	// edit V1.0.0.2013.06.13.17__add_description_to_example_entity.sql to adjust the migration script
	private String description;

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
