/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package io.openliberty.sample.application;

import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.Pageable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/crew")
@ApplicationScoped
public class CrewService {

	@Inject
	CrewMembers crewMembers;

	@POST
	@Path("/{id}") 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON) 
	public String add(CrewMember crewMember) {

		try {
			crewMembers.save(crewMember);
		} catch (ConstraintViolationException x) {
			JsonArrayBuilder messages = Json.createArrayBuilder();
			for (ConstraintViolation<?> v : x.getConstraintViolations()) {
				messages.add(v.getMessage());
			}
			return messages.build().toString();
		}

		return "";
	}

	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") int id) {
		crewMembers.deleteByCrewID(id);
	}

	@GET
	public String retrieve() {
		JsonArrayBuilder jab = Json.createArrayBuilder();
		crewMembers.findAll().forEach( c -> {
			JsonObject json = Json.createObjectBuilder()
								.add("Name", c.name)
								.add("CrewID", c.crewID)
								.add("Rank",c.rank.toString()).build();
			jab.add(json);

	  	});
		return jab.build().toString();
	}

	@GET
	@Path("/rank/{rank}")
	public String retrieveByRank(@PathParam("rank") String rank) {
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for (CrewMember c : crewMembers.findByRank(Rank.fromString(rank))) {	
			JsonObject json = Json.createObjectBuilder()
								.add("Name", c.name)
								.add("CrewID", c.crewID).build();
			jab.add(json);
		}
		return jab.build().toString();
	}

	@GET
	@Path("/rank/{rank}/page/{pageNum}")
	public String retrieveByRank(@PathParam("rank") String rank,
								 @PathParam("pageNum") long pageNum) {
		JsonArrayBuilder jab = Json.createArrayBuilder();

		Pageable pageRequest = Pageable.ofSize(5)
									   .page(pageNum)
									   .sortBy(Sort.asc("name"), Sort.asc("id"));

		for (CrewMember c : crewMembers.findByRank(Rank.fromString(rank), pageRequest)) {
			JsonObject json = Json.createObjectBuilder()
								.add("Name", c.name)
								.add("CrewID", c.crewID).build();
			jab.add(json);
		}
		return jab.build().toString();
	}

	@DELETE
	public void remove() {
		crewMembers.deleteAll();
	}
}